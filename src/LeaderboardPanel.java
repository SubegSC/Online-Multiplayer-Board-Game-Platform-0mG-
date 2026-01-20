import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import auth_logic.Player;
import auth_logic.PlayerData;
import auth_logic.PlayerStats;
import leaderboard_logic.RankingAlgorithm;

// NOTE: no package declaration
public class LeaderboardPanel extends JPanel {

    private final JComboBox<String> gameSelector;
    private final JTable table;
    private final DefaultTableModel model;

    public LeaderboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(MainGui.BG_COL);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top bar
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);

        JLabel title = new JLabel("Leaderboard");
        title.setForeground(MainGui.T_COL);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        gameSelector = new JComboBox<>(new String[] { "Go", "Chess", "Tic-Tac-Toe" });
        styleCombo(gameSelector);

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn);

        JButton backBtn = new JButton("Back"); // Changed text slightly to be generic
        styleButton(backBtn);

        right.add(gameSelector);
        right.add(refreshBtn);
        right.add(backBtn);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Table (keep default Swing look)
        model = new DefaultTableModel(new Object[] { "Rank", "Player", "Rating", "Games", "W-L-T", "Win %" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setForeground(MainGui.T_COL);
        table.setBackground(MainGui.BG_COL);
        table.setSelectionBackground(new Color(60,60,60));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(MainGui.BG_COL);
        add(sp, BorderLayout.CENTER);

        // Hooks
        refreshBtn.addActionListener(e -> refreshTable());
        gameSelector.addActionListener(e -> refreshTable());
        
        // --- FIX: Check login status to determine where "Back" goes ---
        backBtn.addActionListener(e -> {
            if (MainGui.isLoggedIn()) {
                MainGui.showCard("LOBBY");
            } else {
                MainGui.showCard("LANDING");
            }
        });

        // Auto refresh when shown
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                refreshTable();
            }
        });

        refreshTable();
    }

    private void styleButton(JButton b) {
        b.setBackground(MainGui.BTN_COL);
        b.setForeground(MainGui.T_COL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(MainGui.BTN_COL);
        c.setForeground(MainGui.T_COL);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    /** Rebuilds the table from PlayerData using the selected game. */
    private void refreshTable() {
        // Refresh leaderboard data
        String selectedGame = (String) gameSelector.getSelectedItem();
        if (selectedGame == null) selectedGame = "Go";
        final String gameLabel = selectedGame.toLowerCase(); // effectively final for lambda

        List<Player> players = PlayerData.getAllPlayers();

        var rows = players.stream()
                .filter(p -> {
                    // Filter out bots from leaderboard
                    String name = p.getName().toLowerCase();
                    return !name.contains("bot") && !name.equals("autobot") && !name.startsWith("auto");
                })
                .map(p -> {
                    int rating = RankingAlgorithm.getPlayerRating(p, gameLabel);
                    PlayerStats stats = p.getPlayerStats();
                    
                    // Get win/loss/tie counts based on game type
                    int wins, losses, ties, games;
                    if (gameLabel.equals("go")) {
                        wins = stats.getWinsGo();
                        losses = stats.getLossGo();
                        ties = stats.getTieGo();
                    } else if (gameLabel.equals("chess")) {
                        wins = stats.getWinsChess();
                        losses = stats.getLossChess();
                        ties = stats.getTieChess();
                    } else if (gameLabel.contains("tic") || gameLabel.contains("toe")) {
                        // Tic-Tac-Toe
                        wins = stats.getWinsTTT();
                        losses = stats.getLossTTT();
                        ties = stats.getTieTTT();
                    } else {
                        // Default/fallback
                        wins = 0;
                        losses = 0;
                        ties = 0;
                    }
                    games = wins + losses + ties;
                    
                    // Calculate win percentage
                    double winPercent = games > 0 ? (wins * 100.0 / games) : 0.0;
                    
                    return new Object[] { p, rating, games, wins, losses, ties, winPercent };
                })
                .sorted(Comparator.<Object[]>comparingInt(o -> -((Integer) o[1]))) // Sort by rating descending
                .collect(Collectors.toList());

        model.setRowCount(0);
        int rank = 1;
        for (Object[] row : rows) {
            Player p = (Player) row[0];
            int rating = (Integer) row[1];
            int games = (Integer) row[2];
            int wins = (Integer) row[3];
            int losses = (Integer) row[4];
            int ties = (Integer) row[5];
            double winPercent = (Double) row[6];
            
            String winLossTie = wins + "-" + losses + "-" + ties;
            String winPercentStr = games > 0 ? String.format("%.1f%%", winPercent) : "0.0%";
            
            model.addRow(new Object[] { rank++, p.getName(), rating, games, winLossTie, winPercentStr });
        }
    }
}

