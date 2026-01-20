import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.text.SimpleDateFormat; // Added for timestamp formatting
import java.util.Date;           // Added for timestamp

import chess_logic.*; 

// Imports for Chat & Matchmaking integration
import chat.*;
import matchmaking.*;
import leaderboard_logic.RankingAlgorithm;
import auth_logic.PlayerData;

public class ChessGamePanel extends JPanel {

    // Logic
    private ChessGame game;
    private final int BOARD_SIZE = 8;
    
    // State
    private int selectedFile = -1;
    private int selectedRank = -1;
    private List<ChessMove> currentLegalMoves = new ArrayList<>();
    private int consecutivePasses = 0;
    private boolean gameResultRecorded = false; // Prevent double-recording of game results

    // GUI components
    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel vsLabel;
    private Runnable onExitCallback;
    
    // Chat Components
    private JTextArea chatHistory;
    private JTextField chatInput;
    private ChatController chatController;

    private final String player1Name;
    private final String player2Name;

    // Colors
    private static final Color LIGHT_SQ = new Color(240, 217, 181); 
    private static final Color DARK_SQ  = new Color(181, 136, 99);  
    private static final Color HIGHLIGHT_COLOR = new Color(100, 255, 100, 150); 

    public ChessGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.player1Name = player1Name; // WHITE
        this.player2Name = player2Name; // BLACK
        this.onExitCallback = onExitCallback;

        // --- Chat & Session Setup ---
        setupChatSystem();

        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // 1. Top Status Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        vsLabel = new JLabel(player1Name + " (White) vs. " + player2Name + " (Black)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: WHITE");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel, BorderLayout.CENTER);
        topPanel.add(createStyledButton("Leave Match"), BorderLayout.EAST); // Moved Leave button to top for space
        add(topPanel, BorderLayout.NORTH);

        // Game board
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // Chat Panel (Replaced Stub)
        add(createChatPanel(), BorderLayout.EAST);

        // Pass button removed, not a legal option in Chess

        // Leave button logic (re-wiring since I moved it)
        Component eastComp = topPanel.getComponent(2); // The button we just added
        if(eastComp instanceof JButton) {
            ((JButton)eastComp).addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to resign and leave?", "Resign?", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    // Only record if not already recorded (prevents double-counting)
                    if (!gameResultRecorded) {
                        gameResultRecorded = true;
                        // The player who resigns loses - determine who is resigning
                        chess_logic.Color resigningSide = game.getSideToMove();
                        String resigningPlayerName = (resigningSide == chess_logic.Color.WHITE) ? player1Name : player2Name;
                        String winnerName = (resigningSide == chess_logic.Color.WHITE) ? player2Name : player1Name;
                        String loserName = resigningPlayerName; // Resigning player loses
                        
                        applyRatingForChessResult(winnerName, loserName);
                    }
                    onExitCallback.run();
                }
            });
        }

        startNewGame();
    }

    private void setupChatSystem() {
        // Construct a match ID based on players and game type
        String matchId = "CHESS-" + player1Name + "-" + player2Name;
        
        // Use AppContext to get the shared ChatService
        ChatService service = AppContext.getChatService();
        ChatChannel channel = service.openChannel(matchId);

        // Reconstruct a Match object for the session (required by ChatController)
        // Note: Ratings are placeholders here as we only passed names to the panel
        matchmaking.Player p1 = new matchmaking.Player(player1Name, player1Name, 1200);
        matchmaking.Player p2 = new matchmaking.Player(player2Name, player2Name, 1200);
        Match match = new Match(GameType.CHESS, p1, p2);

        GameSession session = new GameSession(match, channel);
        this.chatController = new ChatController(session, service);

        // Register listener to update UI
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

        // Divider for older messages
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
        this.game = new ChessGame();
        resetSelection();
        updateUIState();

        if (chatHistory != null) {
            chatHistory.setText("");
            loadExistingChatHistory();
        }
        appendSystemMessage("Game Started. Good luck!");
    }

    private void resetSelection() {
        selectedFile = -1;
        selectedRank = -1;
        currentLegalMoves.clear();
        repaint();
    }

    private void updateUIState() {
        GameStatus status = game.getStatus();
        chess_logic.Color side = game.getSideToMove();

        // Always repaint first so the last move is visible
        if (boardView != null) {
            boardView.repaint();
        }

        String txt = "Turn: " + side;

        if (status == GameStatus.CHECKMATE && !gameResultRecorded) {
            gameResultRecorded = true;

            chess_logic.Color winnerColor = side.opposite();
            String winnerName = (winnerColor == chess_logic.Color.WHITE) ? player1Name : player2Name;
            String loserName  = (winnerColor == chess_logic.Color.WHITE) ? player2Name : player1Name;

            txt = "CHECKMATE! Winner: " + winnerColor;

            // Game over message
            appendSystemMessage("Game Over! " + winnerName + " defeated " + loserName + ".");

            // Rating + leaderboard (this already logs a 'result saved' message)
            applyRatingForChessResult(winnerName, loserName);

            statusLabel.setText(txt);
            JOptionPane.showMessageDialog(this, txt);

            // Go back to lobby after everything is logged & drawn
            onExitCallback.run();
            return;   // Important: don't fall through
        } 
        else if (status == GameStatus.STALEMATE && !gameResultRecorded) {
            gameResultRecorded = true;

            txt = "Draw by Stalemate";

            appendSystemMessage("Game Over! Draw by stalemate. No rating changes.");

            statusLabel.setText(txt);
            JOptionPane.showMessageDialog(this, txt);

            // You can choose whether a stalemate should return to lobby or allow review
            onExitCallback.run();
            return;
        }

        // Normal running state
        statusLabel.setText(txt);
    }

    private void checkGameOverFromPasses() {
        if (consecutivePasses >= 2 && !gameResultRecorded) {

            // Ask who actually won
            Object[] options = { player1Name + " (White)", player2Name + " (Black)" };
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
            applyRatingForChessResult(winnerName, loserName);

            // Final popup
            JOptionPane.showMessageDialog(this, "Game Over! " + winnerName + " wins.");

            // Back to lobby
            onExitCallback.run();
        }
    }

    private void applyRatingForChessResult(String winnerName, String loserName) {
        // Note: gameResultRecorded should be checked by caller before calling this method
        try {
            System.out.println("Chess applyRatingForChessResult - Winner: " + winnerName + ", Loser: " + loserName);
            auth_logic.Player w = PlayerData.getOrCreateByName(winnerName);
            auth_logic.Player l = PlayerData.getOrCreateByName(loserName);
            System.out.println("Chess applyRatingForChessResult - Winner player: " + w.getUsername() + ", Loser player: " + l.getUsername());
            RankingAlgorithm.recordChessResult(w, l);
            appendSystemMessage("Game result saved: " + winnerName + " defeated " + loserName + ". Ratings and stats updated!");
        } catch (Throwable t) {
            t.printStackTrace();
            gameResultRecorded = false; // Reset on error
        }
    }

    // --- Chat UI Implementation ---
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
            // Send via controller using current logged-in user
            String sender = MainGui.getCurrentUsername();
            // In a real app we'd use a unique ID, here we use username as ID
            chatController.send(sender, sender, msg);
            chatInput.setText("");
        }
    }

    private void onChatMessageReceived(ChatMessage msg) {
        // Called by ChatController listener
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm").format(Date.from(msg.getTimestamp()));
            chatHistory.append("[" + time + "] " + msg.getSenderName() + ": " + msg.getContent() + "\n");
            chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
        });
    }

    private void appendSystemMessage(String msg) {
        if (chatHistory != null) {
            chatHistory.append(">>> " + msg + "\n");
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
        private final int MARGIN = 20;
        private int cellSize;

        public BoardView() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.getStatus() != GameStatus.RUNNING) return;
                    int c = (e.getX() - MARGIN) / cellSize;
                    int r = 7 - ((e.getY() - MARGIN) / cellSize); 
                    if (c < 0 || c > 7 || r < 0 || r > 7) return;
                    handleSquareClick(c, r);
                }
            });
        }

        private void handleSquareClick(int file, int rank) {
            if (selectedFile != -1) {
                boolean moveMade = game.makeMove(selectedFile, selectedRank, file, rank);
                if (moveMade) {
                    consecutivePasses = 0; // Reset pass counter when a real move is made
                    resetSelection();
                    updateUIState();
                    return;
                }
            }
            Piece p = game.getBoardSnapshot()[file][rank];
            if (p != null && p.getColor() == game.getSideToMove()) {
                selectedFile = file;
                selectedRank = rank;
                currentLegalMoves = game.getLegalMovesFrom(file, rank);
                repaint();
            } else {
                resetSelection();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / BOARD_SIZE;
            int xOffset = MARGIN;
            int yOffset = MARGIN;

            Piece[][] board = game.getBoardSnapshot();

            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    int logicRank = 7 - r; 
                    int logicFile = c;
                    int x = xOffset + c * cellSize;
                    int y = yOffset + r * cellSize;

                    if ((r + c) % 2 == 0) g2.setColor(LIGHT_SQ);
                    else g2.setColor(DARK_SQ);
                    g2.fillRect(x, y, cellSize, cellSize);

                    if (logicFile == selectedFile && logicRank == selectedRank) {
                        g2.setColor(HIGHLIGHT_COLOR);
                        g2.fillRect(x, y, cellSize, cellSize);
                    }

                    for (ChessMove m : currentLegalMoves) {
                        if (m.getToFile() == logicFile && m.getToRank() == logicRank) {
                            g2.setColor(HIGHLIGHT_COLOR);
                            g2.fillOval(x + cellSize/3, y + cellSize/3, cellSize/3, cellSize/3);
                        }
                    }

                    Piece p = board[logicFile][logicRank];
                    if (p != null) {
                        drawPiece(g2, p, x, y, cellSize);
                    }
                }
            }
        }

        private void drawPiece(Graphics2D g2, Piece p, int x, int y, int size) {
            String symbol = "";
            switch (p.getType()) {
                case KING:   symbol = "\u265A"; break;
                case QUEEN:  symbol = "\u265B"; break;
                case ROOK:   symbol = "\u265C"; break;
                case BISHOP: symbol = "\u265D"; break;
                case KNIGHT: symbol = "\u265E"; break;
                case PAWN:   symbol = "\u265F"; break;
            }
            g2.setFont(new Font("Serif", Font.PLAIN, size));
            g2.setColor(p.getColor() == chess_logic.Color.WHITE ? Color.WHITE : Color.BLACK);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(symbol)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(symbol, tx, ty);
        }
    }
}