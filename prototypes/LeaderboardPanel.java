import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import auth_logic.Player;
import auth_logic.PlayerData;
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

        gameSelector = new JComboBox<>(new String[] { "Go", "Chess" });
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
        model = new DefaultTableModel(new Object[] { "Rank", "Player", "Rating" }, 0) {
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
        String selectedGame = (String) gameSelector.getSelectedItem();
        if (selectedGame == null) selectedGame = "Go";
        final String gameLabel = selectedGame; // effectively final for lambda

        List<Player> players = PlayerData.getAllPlayers();

        var rows = players.stream()
                .map(p -> new Object[] { p, RankingAlgorithm.getPlayerRating(p, gameLabel) })
                .sorted(Comparator.<Object[]>comparingInt(o -> -((Integer) o[1])))
                .collect(Collectors.toList());

        model.setRowCount(0);
        int rank = 1;
        for (Object[] row : rows) {
            Player p = (Player) row[0];
            int rating = (Integer) row[1];
            model.addRow(new Object[] { rank++, p.getName(), rating });
        }
    }
}