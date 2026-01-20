package leaderboard_logic;

import auth_logic.Player;
import auth_logic.PlayerData;

/** Minimal Elo-style rating helpers for Go & Chess driven by PlayerStats. */
public final class RankingAlgorithm {

    private static final int K_GO = 32;
    private static final int K_CHESS = 24;
    private static final int K_TTT = 30;

    private RankingAlgorithm() {}

    /** Return the player's rating for the given game label ("Go", "Chess", etc.). */
    public static int getPlayerRating(Player p, String gameLabel) {
        if (p == null) return 1200;
        String g = (gameLabel == null) ? "" : gameLabel.trim().toLowerCase();
        
        // FIX: Use strict equality to prevent cross-matching
        if (g.equals("go")) {
            return p.getPlayerStats().getRankGo();
        } else if (g.equals("chess")) {
            return p.getPlayerStats().getRankChess();
        } else if (g.contains("tic") || g.contains("toe")) {
            return p.getPlayerStats().getRankTTT();
        }
        return 1200;
    }

    /** Winner beats loser in Go. */
    public static void recordGoResult(Player winner, Player loser) {
        if (winner == null || loser == null) {
            System.err.println("Warning: Cannot record Go result - winner or loser is null");
            return;
        }
        // Prevent recording if winner and loser are the same player
        if (winner.getUsername().equalsIgnoreCase(loser.getUsername())) {
            System.err.println("Warning: Cannot record Go result - winner and loser are the same player: " + winner.getUsername());
            return;
        }
        System.out.println("Recording Go result: " + winner.getUsername() + " beats " + loser.getUsername());
        int wr = winner.getPlayerStats().getRankGo();
        int lr = loser.getPlayerStats().getRankGo();
        
        // Update Go ratings
        int[] updated = eloUpdate(wr, lr, true, K_GO);
        PlayerData.updateGoRating(winner, updated[0]);
        PlayerData.updateGoRating(loser,  updated[1]);
        
        // Update win/loss stats
        winner.getPlayerStats().setWinsGo(winner.getPlayerStats().getWinsGo() + 1);
        loser.getPlayerStats().setLossGo(loser.getPlayerStats().getLossGo() + 1);
        
        PlayerData.touch(winner);
        PlayerData.touch(loser);
    }

    /** Winner beats loser in Chess. */
    public static void recordChessResult(Player winner, Player loser) {
        if (winner == null || loser == null) {
            System.err.println("Warning: Cannot record Chess result - winner or loser is null");
            return;
        }
        // Prevent recording if winner and loser are the same player
        if (winner.getUsername().equalsIgnoreCase(loser.getUsername())) {
            System.err.println("Warning: Cannot record Chess result - winner and loser are the same player: " + winner.getUsername());
            return;
        }
        System.out.println("Recording Chess result: " + winner.getUsername() + " beats " + loser.getUsername());
        int wr = winner.getPlayerStats().getRankChess();
        int lr = loser.getPlayerStats().getRankChess();
        
        // Update Chess ratings
        int[] updated = eloUpdate(wr, lr, true, K_CHESS);
        PlayerData.updateChessRating(winner, updated[0]);
        PlayerData.updateChessRating(loser,  updated[1]);
        
        // Update win/loss stats
        winner.getPlayerStats().setWinsChess(winner.getPlayerStats().getWinsChess() + 1);
        loser.getPlayerStats().setLossChess(loser.getPlayerStats().getLossChess() + 1);
        
        PlayerData.touch(winner);
        PlayerData.touch(loser);
    }

    /** Winner beats loser in Tic-Tac-Toe. */
    public static void recordTicTacToeResult(Player winner, Player loser) {
        if (winner == null || loser == null) {
            System.err.println("Warning: Cannot record Tic-Tac-Toe result - winner or loser is null");
            return;
        }
        // Prevent recording if winner and loser are the same player
        if (winner.getUsername().equalsIgnoreCase(loser.getUsername())) {
            System.err.println("Warning: Cannot record Tic-Tac-Toe result - winner and loser are the same player: " + winner.getUsername());
            return;
        }
        System.out.println("Recording Tic-Tac-Toe result: " + winner.getUsername() + " beats " + loser.getUsername());
        int wr = winner.getPlayerStats().getRankTTT();
        int lr = loser.getPlayerStats().getRankTTT();
        
        // Update Tic-Tac-Toe ratings
        int[] updated = eloUpdate(wr, lr, true, K_TTT);
        PlayerData.updateTicTacToeRating(winner, updated[0]);
        PlayerData.updateTicTacToeRating(loser,  updated[1]);
        
        // Update win/loss stats
        winner.getPlayerStats().setWinsTTT(winner.getPlayerStats().getWinsTTT() + 1);
        loser.getPlayerStats().setLossTTT(loser.getPlayerStats().getLossTTT() + 1);
        
        PlayerData.touch(winner);
        PlayerData.touch(loser);
    }

    // ---------- internals ----------

    /** Basic Elo update; returns {winnerNew, loserNew}. */
    private static int[] eloUpdate(int ratingWinner, int ratingLoser, boolean winnerWins, int K) {
        double expW = 1.0 / (1.0 + Math.pow(10.0, (ratingLoser - ratingWinner) / 400.0));
        double expL = 1.0 - expW;
        double scoreW = winnerWins ? 1.0 : 0.0;
        double scoreL = 1.0 - scoreW;
        int newW = ratingWinner + (int)Math.round(K * (scoreW - expW));
        int newL = ratingLoser  + (int)Math.round(K * (scoreL - expL));
        return new int[] { newW, newL };
    }
}