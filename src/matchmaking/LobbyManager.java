// File: src/matchmaking/LobbyManager.java
package matchmaking;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Manages lobbies: creating them and joining them.
 * This directly matches your tasks: createLobby() and joinLobby().
 */
public class LobbyManager {

    private final Map<String, Lobby> lobbies = new HashMap<>();
    private final Random random = new Random();

    /**
     * Create a new lobby hosted by the given player for the specified game type.
     * Returns the generated lobby ID (the host can share this with a friend).
     */
    public synchronized String createLobby(Player host, GameType gameType) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(gameType, "gameType");

        String id = generateLobbyId();
        Lobby lobby = new Lobby(id, gameType, host);
        lobbies.put(id, lobby);
        return id;
    }

    /**
     * Attempt to join a lobby by ID.
     * Returns the Lobby if join succeeded, or null if:
     * - the lobby does not exist, or
     * - the lobby is full, or
     * - the player is already in the lobby.
     */
    public synchronized Lobby joinLobby(String lobbyId, Player player) {
        Objects.requireNonNull(lobbyId, "lobbyId");
        Objects.requireNonNull(player, "player");

        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return null; // lobby not found
        }

        if (!lobby.canJoin(player)) {
            return null; // cannot join (full or already there)
        }

        lobby.addPlayer(player);
        return lobby;
    }

    /**
     * Get a lobby by ID (for UI or debugging).
     * Returns null if it does not exist.
     */
    public synchronized Lobby getLobby(String lobbyId) {
        Objects.requireNonNull(lobbyId, "lobbyId");
        return lobbies.get(lobbyId);
    }

    // ------------- helpers -------------

    private String generateLobbyId() {
        // simple 6-character alphanumeric ID
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String id = sb.toString();
        if (lobbies.containsKey(id)) {
            return generateLobbyId(); // avoid collision
        }
        return id;
    }
}