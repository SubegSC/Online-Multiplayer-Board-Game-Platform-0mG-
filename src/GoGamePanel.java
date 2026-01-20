import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import go_logic.*; 

// Imports for Chat & Matchmaking integration
import chat.*;
import matchmaking.*;
import leaderboard_logic.RankingAlgorithm;
import auth_logic.PlayerData;

public class GoGamePanel extends JPanel {

    private GoGame game;
    private final int boardSize = 9;
    private boolean gameResultRecorded = false; // Prevent double-recording of game results

    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel vsLabel;
    private JButton passBtn;
    
    // Chat components
    private JTextArea chatHistory;
    private JTextField chatInput;
    private ChatController chatController;

    private final String player1Name; // Plays Black
    private final String player2Name; // Plays White
    private Runnable onExitCallback;

    public GoGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.onExitCallback = onExitCallback;

        // --- Chat & Session Setup ---
        setupChatSystem();

        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // Top status bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- CHANGED: Determine label order based on current user ---
        String currentUser = MainGui.getCurrentUsername();
        String labelText;
        
        // If the logged-in user is Player 2 (White), put them on the left
        if (currentUser.equals(player2Name)) {
            labelText = player2Name + " (White) vs. " + player1Name + " (Black)";
        } else {
            // Default: Player 1 (Black) on left
            labelText = player1Name + " (Black) vs. " + player2Name + " (White)";
        }

        vsLabel = new JLabel(labelText, SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: BLACK");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        scoreLabel = new JLabel("Captured: B=0 | W=0");
        scoreLabel.setForeground(Color.GRAY);
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel, BorderLayout.CENTER);
        topPanel.add(scoreLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Board
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // Chat
        add(createChatPanel(), BorderLayout.EAST);

        // Bottom actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainGui.BG_COL);
        passBtn = createStyledButton("Pass Turn");
        JButton leaveBtn = createStyledButton("Leave Match");
        bottomPanel.add(passBtn);
        bottomPanel.add(leaveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        passBtn.addActionListener(e -> {
            if (game.isGameOver()) return;

            // Log which color passed (use the player whose turn it currently is)
            StoneColor current = game.getCurrentPlayer();
            appendSystemMessage(current + " passed.");

            game.pass();
            updateUIState();
            checkGameOver();
        });

        // Resignation Logic
        leaveBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to resign and leave?",
                    "Resign?", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                game.resign();

                if (!gameResultRecorded) {
                    gameResultRecorded = true;

                    // Determine winner after resignation
                    StoneColor winnerColor = game.getWinnerColor();
                    String winnerName = (winnerColor == StoneColor.BLACK) ? player1Name : player2Name;
                    String loserName  = (winnerColor == StoneColor.BLACK) ? player2Name : player1Name;

                    // Log in chat
                    appendSystemMessage("Game Over! " + winnerName + " defeated " + loserName + " by resignation.");

                    // Record result
                    auth_logic.Player w = PlayerData.getOrCreateByName(winnerName);
                    auth_logic.Player l = PlayerData.getOrCreateByName(loserName);
                    RankingAlgorithm.recordGoResult(w, l);

                    appendSystemMessage("Game result saved: " + winnerName
                            + " defeated " + loserName + ". Ratings and stats updated!");
                }

                onExitCallback.run();
            }
        });

        // Auto repaint on resize
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (boardView != null) boardView.repaint();
            }
        });

        startNewGame();
    }

    private void setupChatSystem() {
        String matchId = "GO-" + player1Name + "-" + player2Name;
        ChatService service = AppContext.getChatService();
        ChatChannel channel = service.openChannel(matchId);

        matchmaking.Player p1 = new matchmaking.Player(player1Name, player1Name, 1200);
        matchmaking.Player p2 = new matchmaking.Player(player2Name, player2Name, 1200);
        Match match = new Match(GameType.GO, p1, p2);

        GameSession session = new GameSession(match, channel);
        this.chatController = new ChatController(session, service);
        this.chatController.addListener(this::onChatMessageReceived);
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

    public void startNewGame() {
        this.game = new GoGame(boardSize);
        if (boardView != null) boardView.repaint();

        if (chatHistory != null) {
            chatHistory.setText("");
            loadExistingChatHistory();
        }
        appendSystemMessage("Game Started. Good luck!");
        
        // --- CHANGED: Add message stating who goes first ---
        appendSystemMessage("Black (" + player1Name + ") goes first.");

        updateUIState();
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

        java.awt.event.ActionListener sendAction = e -> sendMessage();
        chatInput.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);

        return panel;
    }

    private void sendMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            String sender = MainGui.getCurrentUsername();
            chatController.send(sender, sender, msg);
            chatInput.setText("");
        }
    }

    private void onChatMessageReceived(ChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm").format(Date.from(msg.getTimestamp()));
            chatHistory.append("[" + timestamp + "] " + msg.getSenderName() + ": " + msg.getContent() + "\n");
            chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
        });
    }

    private void appendSystemMessage(String msg) {
        if (chatHistory != null) {
            chatHistory.append(">>> " + msg + "\n");
        }
    }

    private void updateUIState() {
        StoneColor current = game.getCurrentPlayer();
        if (statusLabel != null) statusLabel.setText("Turn: " + current);

        int bCaps = game.getCapturedStones(StoneColor.BLACK);
        int wCaps = game.getCapturedStones(StoneColor.WHITE);
        if (scoreLabel != null) scoreLabel.setText("Captured: B=" + bCaps + " | W=" + wCaps);

        if (boardView != null) boardView.repaint();
    }

    private void checkGameOver() {
        if (game.isGameOver() && !gameResultRecorded) {
            gameResultRecorded = true; // Mark as recorded to prevent double-counting

            // Repaint so the final board is visible
            if (boardView != null) {
                boardView.repaint();
            }

            try {
                StoneColor winnerColor = game.getWinnerColor();
                String winnerName, loserName;

                if (winnerColor != null) {
                    boolean blackWins = (winnerColor == StoneColor.BLACK);
                    winnerName = blackWins ? player1Name : player2Name;
                    loserName  = blackWins ? player2Name : player1Name;
                } else {
                    Object[] options = { player1Name + " (Black)", player2Name + " (White)" };
                    int choice = JOptionPane.showOptionDialog(
                            this, "Both players have passed. Who won the game?", "Confirm Winner",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]
                    );
                    if (choice != 0 && choice != 1) {
                        // User cancelled; allow play to continue
                        gameResultRecorded = false;
                        return;
                    }
                    boolean p1Wins = (choice == 0);
                    winnerName = p1Wins ? player1Name : player2Name;
                    loserName  = p1Wins ? player2Name : player1Name;
                }

                // Clear, explicit messages in chat
                appendSystemMessage("Game Over! " + winnerName + " defeated " + loserName + ".");
                
                // Apply rating change and update win/loss stats (Go)
                System.out.println("Go game result - Winner: " + winnerName + ", Loser: " + loserName);
                auth_logic.Player w = PlayerData.getOrCreateByName(winnerName);
                auth_logic.Player l = PlayerData.getOrCreateByName(loserName);
                System.out.println("Go game result - Winner player: " + w.getUsername() + ", Loser player: " + l.getUsername());
                RankingAlgorithm.recordGoResult(w, l);

                appendSystemMessage("Game result saved: " + winnerName + " defeated " + loserName + ". Ratings and stats updated!");

                JOptionPane.showMessageDialog(this, "Game Over! " + winnerName + " wins.");
                
                // Return to lobby after recording result
                onExitCallback.run();
            } catch (Throwable t) {
                t.printStackTrace();
                gameResultRecorded = false; // Reset on error
            }
        }
    }
    
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class BoardView extends JPanel {
        private final int MARGIN = 30;
        private int cellSize;

        public BoardView() {
            setBackground(new Color(220, 179, 92));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.isGameOver()) return;

                    int r = Math.round((float)(e.getY() - MARGIN) / cellSize);
                    int c = Math.round((float)(e.getX() - MARGIN) / cellSize);

                    try {
                        game.playMove(r, c);
                        updateUIState();
                        checkGameOver();
                    } catch (IllegalMoveException ex) {
                        appendSystemMessage("Invalid move: " + ex.getMessage());
                        JOptionPane.showMessageDialog(BoardView.this,
                                ex.getMessage(), "Invalid Move", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ignore) { }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / (boardSize - 1);

            int xOffset = MARGIN;
            int yOffset = MARGIN;

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < boardSize; i++) {
                g2.drawLine(xOffset, yOffset + i * cellSize, xOffset + (boardSize - 1) * cellSize, yOffset + i * cellSize);
                g2.drawLine(xOffset + i * cellSize, yOffset, xOffset + i * cellSize, yOffset + (boardSize - 1) * cellSize);
            }

            GoBoard board = game.getBoard();
            int stoneRadius = (int)(cellSize * 0.4);

            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    try {
                        StoneColor color = board.getStone(r, c);
                        if (color != null) {
                            int x = xOffset + c * cellSize;
                            int y = yOffset + r * cellSize;

                            if (color == StoneColor.BLACK) {
                                g2.setColor(Color.BLACK);
                                g2.fillOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                            } else {
                                g2.setColor(Color.WHITE);
                                g2.fillOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                                g2.setColor(Color.BLACK);
                                g2.drawOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                            }
                        }
                    } catch (IndexOutOfBoundsException ignore) { }
                }
            }
        }
    }
}