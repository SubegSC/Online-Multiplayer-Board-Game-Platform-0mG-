package auth_logic;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String username;
    private PlayerStats stats;
    public List<Player> friendsList = new ArrayList<>(); // simple stub

    public Player(String username) {
        this.username = username;
        this.stats = new PlayerStats(); // defaults inside PlayerStats (e.g., 1200s)
    }

    public String getUsername() { return username; }
    // Convenience alias so GUI/leaderboard code that calls getName() still works.
    public String getName() { return username; }

    public PlayerStats getPlayerStats() { return stats; }

    // Basic stub; adapt if you track real presence
    public String getStatus() { return "Online"; }
}
