package leaderboard_logic;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardConfig {

    private static Map<String, Integer> kFactors = new HashMap<>();

    static {
        kFactors.put("chess", 32);
        kFactors.put("go", 24);
        kFactors.put("tic tac toe", 16);
    }

    public static void setKFactor(String game, int kFactor) {
        if (kFactor <= 0) {
            throw new IllegalArgumentException("K-Factor must be positive.");
        }
        kFactors.put(game.toLowerCase(), kFactor);
        System.out.println("K-Factor for " + game + " set to " + kFactor);
    }

    public static int getKFactor(String game) {
        return kFactors.getOrDefault(game.toLowerCase(), 32); // Default K-Factor
    }
}
