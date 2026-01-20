import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import TicTacToe_Logic.*;

// Imports for Chat & Matchmaking integration
import chat.*;
import matchmaking.*;
import leaderboard_logic.RankingAlgorithm;
import auth_logic.PlayerData;

public class TicTacToeGamePanel extends JPanel {

    private TicTacToeGame game;
    private boolean gameResultRecorded = false; // Prevent double-recording of game results
    private int consecutivePasses = 0;          // Track consecutive passes for game over

    // GUI components
    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel vsLabel;
    private Runnable onExitCallback;
    
    // Chat Components
    private JTextArea chatHistory;
    private JTextField chatInput;
    private ChatController chatController;

    private final String player1Name; // X player
    private final String player2Name; // O player

    public TicTacToeGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.player1Name = player1Name; // X
        this.player2Name = player2Name; // O
        this.onExitCallback = onExitCallback;

        // --- Chat & Session Setup (controller only, no listener yet) ---
        setupChatSystem();

        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // Top status bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        vsLabel = new JLabel(player1Name + " (X) vs. " + player2Name + " (O)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: X");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel, BorderLayout.CENTER);
        topPanel.add(createStyledButton("Leave Match"), BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Game board
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // Chat Panel (creates chatHistory/chatInput)
        add(createChatPanel(), BorderLayout.EAST);

        // ✅ Now that chatHistory exists, attach the listener
        this.chatController.addListener(this::onChatMessageReceived);

        // Bottom panel with Pass button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainGui.BG_COL);
        JButton passBtn = createStyledButton("Pass Turn");
        bottomPanel.add(passBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Pass button logic
        passBtn.addActionListener(e -> {
            if (game.getResult() != TicTacToeGameResult.IN_PROGRESS) return; // Don't allow pass if game is over
            consecutivePasses++;
            appendSystemMessage("Passed turn. Consecutive passes: " + consecutivePasses);
            checkGameOverFromPasses();
        });

        // Leave button logic
        Component eastComp = topPanel.getComponent(2);
        if (eastComp instanceof JButton) {
            ((JButton) eastComp).addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to resign and leave?", "Resign?", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    // Only record if not already recorded (prevents double-counting)
                    if (!gameResultRecorded) {
                        gameResultRecorded = true;
                        // The player who resigns loses - determine who is resigning
                        TicTacToePlayer resigningPlayer = game.getCurrentPlayer();
                        String resigningPlayerName = (resigningPlayer == TicTacToePlayer.X) ? player1Name : player2Name;
                        String winnerName = (resigningPlayer == TicTacToePlayer.X) ? player2Name : player1Name;
                        String loserName = resigningPlayerName; // Resigning player loses
                        
                        applyRatingForTicTacToeResult(winnerName, loserName);
                    }
                    onExitCallback.run();
                }
            });
        }

        startNewGame();
    }

    private void setupChatSystem() {
        // Construct a match ID based on players and game type
        String matchId = "TTT-" + player1Name + "-" + player2Name;
        
        // Use AppContext to get the shared ChatService
        ChatService service = AppContext.getChatService();
        ChatChannel channel = service.openChannel(matchId);

        // Reconstruct a Match object for the session (required by ChatController)
        matchmaking.Player p1 = new matchmaking.Player(player1Name, player1Name, 1200);
        matchmaking.Player p2 = new matchmaking.Player(player2Name, player2Name, 1200);
        Match match = new Match(GameType.TIC_TAC_TOE, p1, p2);

        GameSession session = new GameSession(match, channel);
        this.chatController = new ChatController(session, service);
        // Listener is attached later, after chatHistory exists
    }

    public void startNewGame() {
        this.game = new TicTacToeGame();
        this.gameResultRecorded = false; // Reset flag for new game
        this.consecutivePasses = 0;     // Reset pass counter
        updateUIState();

        if (chatHistory != null) {
            chatHistory.setText("");
            loadExistingChatHistory();
        }

        appendSystemMessage("Game Started. Good Luck!");
    }

    private void updateUIState() {
        // Repaint immediately so the last move appears on the board
        if (boardView != null) {
            boardView.repaint();
        }

        TicTacToeGameResult result = game.getResult();
        TicTacToePlayer current = game.getCurrentPlayer();

        String txt = "Turn: " + current;

        if (result != TicTacToeGameResult.IN_PROGRESS && !gameResultRecorded) {
            gameResultRecorded = true;

            String winnerName;
            String loserName;

            if (result == TicTacToeGameResult.X_WINS) {
                winnerName = player1Name;
                loserName  = player2Name;
                txt = "Game Over! X Wins!";
            } else if (result == TicTacToeGameResult.O_WINS) {
                winnerName = player2Name;
                loserName  = player1Name;
                txt = "Game Over! O Wins!";
            } else {
                // Draw: no rating change
                statusLabel.setText("Game Over! Draw!");
                appendSystemMessage("Game Over! It's a draw. No rating changes.");
                JOptionPane.showMessageDialog(this, "Game Over! It's a draw!");
                onExitCallback.run();
                return;
            }

            // Chat: announce result
            appendSystemMessage("Game Over! " + winnerName + " defeated " + loserName + ".");

            // Rating + leaderboard update
            System.out.println("Tic-Tac-Toe game result - Winner: " + winnerName + ", Loser: " + loserName);
            auth_logic.Player w = PlayerData.getOrCreateByName(winnerName);
            auth_logic.Player l = PlayerData.getOrCreateByName(loserName);
            System.out.println("Tic-Tac-Toe game result - Winner player: " + w.getUsername() + ", Loser player: " + l.getUsername());
            RankingAlgorithm.recordTicTacToeResult(w, l);

            appendSystemMessage("Game result saved: " + winnerName + " defeated " + loserName + ". Ratings and stats updated!");

            statusLabel.setText(txt);
            JOptionPane.showMessageDialog(this, txt);

            // Only after repaint + chat + dialog do we leave the match
            onExitCallback.run();
            return;
        }

        // Normal running state
        statusLabel.setText(txt);
    }

    private void checkGameOverFromPasses() {
        if (consecutivePasses >= 2 && !gameResultRecorded) {

            Object[] options = { player1Name + " (X)", player2Name + " (O)" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Both players have passed. Who won the game?",
                    "Game Over - Select Winner",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice != 0 && choice != 1) {
                // User cancelled – keep playing, reset pass counter
                consecutivePasses = 0;
                return;
            }

            gameResultRecorded = true;

            boolean p1Wins = (choice == 0);
            String winnerName = p1Wins ? player1Name : player2Name;
            String loserName  = p1Wins ? player2Name : player1Name;

            // Chat log
            appendSystemMessage("Game Over! " + winnerName
                    + " defeated " + loserName + " (decided after passes).");

            // Rating + leaderboard, logs Game Result Saved
            applyRatingForTicTacToeResult(winnerName, loserName);

            // Final popup
            JOptionPane.showMessageDialog(this, "Game Over! " + winnerName + " wins.");

            // Back to lobby
            onExitCallback.run();
        }
    }

    private void applyRatingForTicTacToeResult(String winnerName, String loserName) {
        // Note: gameResultRecorded should be checked by caller before calling this method
        try {
            System.out.println("Tic-Tac-Toe applyRatingForTicTacToeResult - Winner: " + winnerName + ", Loser: " + loserName);
            auth_logic.Player w = PlayerData.getOrCreateByName(winnerName);
            auth_logic.Player l = PlayerData.getOrCreateByName(loserName);
            System.out.println("Tic-Tac-Toe applyRatingForTicTacToeResult - Winner player: " + w.getUsername() + ", Loser player: " + l.getUsername());
            RankingAlgorithm.recordTicTacToeResult(w, l);
            appendSystemMessage("Game result saved: " + winnerName + " defeated " + loserName + ". Ratings and stats updated!");
        } catch (Throwable t) {
            t.printStackTrace();
            gameResultRecorded = false; // Reset on error
        }
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

        JLabel header = new JLabel("Match Chat", SwingConstants.CENTER);
        header.setForeground(MainGui.T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setBackground(new Color(30, 30, 30));
        chatHistory.setForeground(new Color(200, 200, 200));
        chatHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatHistory.setLineWrap(true);
        chatHistory.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(chatHistory);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(new Color(30, 30, 30));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(50, 50, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setCaretColor(Color.WHITE);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBackground(new Color(60, 60, 60));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        sendBtn.setPreferredSize(new Dimension(50, 25));

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // Wire events to Controller
        java.awt.event.ActionListener sendAction = e -> sendMessage();
        chatInput.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);

        return panel;
    }

    private void sendMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            String sender = MainGui.getCurrentUsername();

            // Send through the controller (so it’s persisted + delivered to the other player)
            chatController.send(sender, sender, msg);

            // Optimistically show it in this client immediately
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            if (chatHistory != null) {
                chatHistory.append("[" + time + "] " + sender + ": " + msg + "\n");
                chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
            }

            chatInput.setText("");
        }
    }

    private void onChatMessageReceived(ChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm").format(Date.from(msg.getTimestamp()));
            chatHistory.append("[" + time + "] " + msg.getSenderName() + ": " + msg.getContent() + "\n");
            chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
        });
    }

    private void loadExistingChatHistory() {
        if (chatController == null || chatHistory == null) {
            return;
        }

        List<ChatMessage> history = chatController.getHistory();
        if (history == null || history.isEmpty()) {
            return;
        }

        chatHistory.append("--- Earlier Messages ---\n");

        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        for (ChatMessage msg : history) {
            String time = fmt.format(Date.from(msg.getTimestamp()));
            chatHistory.append("[" + time + "] "
                    + msg.getSenderName() + ": "
                    + msg.getContent() + "\n");
        }

        chatHistory.append("\n");
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }

    private void appendSystemMessage(String msg) {
        if (chatHistory != null) {
            chatHistory.append(">>> " + msg + "\n");
            chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
        }
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Board drawing
    private class BoardView extends JPanel {
        private final int MARGIN = 50;
        private int cellSize;

        public BoardView() {
            setBackground(new Color(255, 255, 255));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.getResult() != TicTacToeGameResult.IN_PROGRESS) return;

                    int x = (e.getX() - MARGIN) / cellSize;
                    int y = (e.getY() - MARGIN) / cellSize;
                    
                    if (x < 0 || x >= 3 || y < 0 || y >= 3) return;

                    boolean moveMade = game.playMove(x, y);
                    if (moveMade) {
                        consecutivePasses = 0; // Reset pass counter when a valid move is made
                        updateUIState();
                    } else {
                        appendSystemMessage("Invalid move! That square is already taken.");
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / 3;

            int xOffset = MARGIN;
            int yOffset = MARGIN;

            // Draw grid lines
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            
            // Vertical lines
            g2.drawLine(xOffset + cellSize, yOffset, xOffset + cellSize, yOffset + 3 * cellSize);
            g2.drawLine(xOffset + 2 * cellSize, yOffset, xOffset + 2 * cellSize, yOffset + 3 * cellSize);
            
            // Horizontal lines
            g2.drawLine(xOffset, yOffset + cellSize, xOffset + 3 * cellSize, yOffset + cellSize);
            g2.drawLine(xOffset, yOffset + 2 * cellSize, xOffset + 3 * cellSize, yOffset + 2 * cellSize);

            // Draw X's and O's
            TicTacToeBoard board = game.getBoard();
            int padding = cellSize / 4;
            
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    TicTacToePlayer player = board.getPlayerAt(row, col);
                    if (player != null) {
                        int x = xOffset + col * cellSize;
                        int y = yOffset + row * cellSize;
                        
                        if (player == TicTacToePlayer.X) {
                            // Draw X
                            g2.setColor(Color.BLUE);
                            g2.setStroke(new BasicStroke(5));
                            g2.drawLine(x + padding, y + padding, 
                                       x + cellSize - padding, y + cellSize - padding);
                            g2.drawLine(x + cellSize - padding, y + padding, 
                                       x + padding, y + cellSize - padding);
                        } else {
                            // Draw O
                            g2.setColor(Color.RED);
                            g2.setStroke(new BasicStroke(5));
                            g2.drawOval(x + padding, y + padding, 
                                       cellSize - 2 * padding, cellSize - 2 * padding);
                        }
                    }
                }
            }
        }
    }
}
