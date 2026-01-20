package leaderboard_logic;

import auth_logic.Player;

public class AchievementSharing {

    public static void shareAchievement(Player player, String achievement) {
        // Generate shareable content
        String shareContent = player.getUsername() + " has achieved: " + achievement + "!";

        // Simulate sharing it is not exactly sharing but just a small simulation
        System.out.println("Sharing to social media: " + shareContent);
    }
}