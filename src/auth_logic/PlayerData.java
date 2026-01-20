package auth_logic;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    // Global in-memory registry of players (used by GUI/leaderboard)
    public static final List<Player> players = new ArrayList<>();

    // Optional seed so the leaderboard isn't empty on first run
    static {
        if (players.isEmpty()) {
            // Alice - Strong Chess player, weaker at Go
            Player a = new Player("Alice");
            a.getPlayerStats().setRankChess(1500);
            a.getPlayerStats().setWinsChess(8);
            a.getPlayerStats().setLossChess(3);
            a.getPlayerStats().setTieChess(1);
            a.getPlayerStats().setRankGo(1100);
            a.getPlayerStats().setWinsGo(5);
            a.getPlayerStats().setLossGo(7);
            a.getPlayerStats().setTieGo(0);
            a.getPlayerStats().setRankTTT(1150);
            a.getPlayerStats().setWinsTTT(3);
            a.getPlayerStats().setLossTTT(4);
            a.getPlayerStats().setTieTTT(1);
            
            // Bob - Strong Go player, weaker at Chess
            Player b = new Player("Bob");
            b.getPlayerStats().setRankChess(1200);
            b.getPlayerStats().setWinsChess(4);
            b.getPlayerStats().setLossChess(6);
            b.getPlayerStats().setTieChess(2);
            b.getPlayerStats().setRankGo(1350);
            b.getPlayerStats().setWinsGo(9);
            b.getPlayerStats().setLossGo(2);
            b.getPlayerStats().setTieGo(1);
            b.getPlayerStats().setRankTTT(1400);
            b.getPlayerStats().setWinsTTT(10);
            b.getPlayerStats().setLossTTT(1);
            b.getPlayerStats().setTieTTT(0);
            
            // Charlie - Balanced player
            Player c = new Player("Charlie");
            c.getPlayerStats().setRankChess(1300);
            c.getPlayerStats().setWinsChess(7);
            c.getPlayerStats().setLossChess(4);
            c.getPlayerStats().setTieChess(0);
            c.getPlayerStats().setRankGo(1250);
            c.getPlayerStats().setWinsGo(6);
            c.getPlayerStats().setLossGo(5);
            c.getPlayerStats().setTieGo(1);
            c.getPlayerStats().setRankTTT(1200);
            c.getPlayerStats().setWinsTTT(4);
            c.getPlayerStats().setLossTTT(3);
            c.getPlayerStats().setTieTTT(2);
            
            // David - New player, not in friend list
            Player d = new Player("David");
            d.getPlayerStats().setRankChess(1400);
            d.getPlayerStats().setWinsChess(10);
            d.getPlayerStats().setLossChess(5);
            d.getPlayerStats().setTieChess(1);
            d.getPlayerStats().setRankGo(1180);
            d.getPlayerStats().setWinsGo(4);
            d.getPlayerStats().setLossGo(8);
            d.getPlayerStats().setTieGo(0);
            d.getPlayerStats().setRankTTT(1300);
            d.getPlayerStats().setWinsTTT(6);
            d.getPlayerStats().setLossTTT(2);
            d.getPlayerStats().setTieTTT(1);
            
            // Emma - New player, not in friend list
            Player e = new Player("Emma");
            e.getPlayerStats().setRankChess(1150);
            e.getPlayerStats().setWinsChess(3);
            e.getPlayerStats().setLossChess(6);
            e.getPlayerStats().setTieChess(1);
            e.getPlayerStats().setRankGo(1420);
            e.getPlayerStats().setWinsGo(12);
            e.getPlayerStats().setLossGo(3);
            e.getPlayerStats().setTieGo(1);
            e.getPlayerStats().setRankTTT(1250);
            e.getPlayerStats().setWinsTTT(5);
            e.getPlayerStats().setLossTTT(4);
            e.getPlayerStats().setTieTTT(0);
            
            // Frank - New player, not in friend list
            Player f = new Player("Frank");
            f.getPlayerStats().setRankChess(1250);
            f.getPlayerStats().setWinsChess(6);
            f.getPlayerStats().setLossChess(5);
            f.getPlayerStats().setTieChess(1);
            f.getPlayerStats().setRankGo(1200);
            f.getPlayerStats().setWinsGo(5);
            f.getPlayerStats().setLossGo(6);
            f.getPlayerStats().setTieGo(1);
            f.getPlayerStats().setRankTTT(1350);
            f.getPlayerStats().setWinsTTT(8);
            f.getPlayerStats().setLossTTT(3);
            f.getPlayerStats().setTieTTT(1);
            
            // Grace - New player, not in friend list
            Player g = new Player("Grace");
            g.getPlayerStats().setRankChess(1450);
            g.getPlayerStats().setWinsChess(11);
            g.getPlayerStats().setLossChess(4);
            g.getPlayerStats().setTieChess(0);
            g.getPlayerStats().setRankGo(1280);
            g.getPlayerStats().setWinsGo(7);
            g.getPlayerStats().setLossGo(4);
            g.getPlayerStats().setTieGo(2);
            g.getPlayerStats().setRankTTT(1100);
            g.getPlayerStats().setWinsTTT(2);
            g.getPlayerStats().setLossTTT(5);
            g.getPlayerStats().setTieTTT(1);
            
            players.add(a); players.add(b); players.add(c);
            players.add(d); players.add(e); players.add(f); players.add(g);
        }
    }

    /** Check if a player exists (case-insensitive). */
    public static synchronized boolean playerExists(String name) {
        if (name == null) return false;
        String norm = name.trim();
        for (Player p : players) {
            if (p.getUsername().equalsIgnoreCase(norm)) return true;
        }
        return false;
    }

    /** Create-or-fetch by display name (case-insensitive). */
    public static synchronized Player getOrCreateByName(String name) {
        if (name == null) name = "Guest";
        String norm = name.trim();
        for (Player p : players) {
            if (p.getUsername().equalsIgnoreCase(norm)) return p;
        }
        Player np = new Player(norm);
        players.add(np);
        return np;
    }

    /** Update Go rating via PlayerStats (source of truth). */
    public static synchronized void updateGoRating(Player player, int newRating) {
        if (player == null) return;
        player.getPlayerStats().setRankGo(newRating);
    }

    /** Update Chess rating via PlayerStats (source of truth). */
    public static synchronized void updateChessRating(Player player, int newRating) {
        if (player == null) return;
        player.getPlayerStats().setRankChess(newRating);
    }

    /** Update Tic-Tac-Toe rating via PlayerStats (source of truth). */
    public static synchronized void updateTicTacToeRating(Player player, int newRating) {
        if (player == null) return;
        player.getPlayerStats().setRankTTT(newRating);
    }

    /** Ensure player is registered (no-op if already present). */
    public static synchronized void touch(Player p) {
        if (p == null) return;
        for (Player x : players) {
            if (x.getUsername().equalsIgnoreCase(p.getUsername())) return;
        }
        players.add(p);
    }

    /** Snapshot for leaderboard rendering. */
    public static synchronized List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }
}
