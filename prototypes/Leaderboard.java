package leaderboard_logic;

import auth_logic.Player;
import auth_logic.PlayerData;
import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard {

    // Use Case 1: View Leaderboards
    public static List<Player> getTopPlayers(String game, int topN) {
        if (game == null || game.isBlank()) {
            throw new IllegalArgumentException("Invalid game type.");
        }
        return PlayerData.players.stream()
                .sorted(getComparatorByGame(game))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private static Comparator<Player> getComparatorByGame(String game) {
        return (p1, p2) -> {
            try {
                int rating1 = RankingAlgorithm.getPlayerRating(p1, game);
                int rating2 = RankingAlgorithm.getPlayerRating(p2, game);
                return Integer.compare(rating2, rating1); // Descending order
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid game type.");
            }
        };
    }
}