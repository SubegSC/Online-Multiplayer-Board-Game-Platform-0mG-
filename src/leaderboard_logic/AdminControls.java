package leaderboard_logic;

import auth_logic.Player;
import auth_logic.PlayerData;
import auth_logic.PlayerStats;

public class AdminControls {

    public static void resetLeaderboard(String game) {
        if (game == null || game.isBlank()) {
            throw new IllegalArgumentException("Invalid game type."); // Validate input
        }

        for (Player player : PlayerData.players) {
            PlayerStats stats = player.getPlayerStats();
            switch (game.toLowerCase()) {
                case "chess":
                    stats.setRankChess(1200);
                    stats.setWinsChess(0);
                    stats.setLossChess(0);
                    stats.setTieChess(0);
                    stats.getHistoricalRatings("chess").clear();
                    stats.getHistoricalRatings("chess").add(1200);
                    break;
                case "go":
                    stats.setRankGo(1200);
                    stats.setWinsGo(0);
                    stats.setLossGo(0);
                    stats.setTieGo(0);
                    stats.getHistoricalRatings("go").clear();
                    stats.getHistoricalRatings("go").add(1200);
                    break;
                case "tic tac toe":
                    stats.setRankTTT(1200);
                    stats.setWinsTTT(0);
                    stats.setLossTTT(0);
                    stats.setTieTTT(0);
                    stats.getHistoricalRatings("tic tac toe").clear();
                    stats.getHistoricalRatings("tic tac toe").add(1200);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid game type."); // Handle invalid game types
            }
        }
        System.out.println("Leaderboard for " + game + " has been reset.");
    }
}
