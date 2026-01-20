// File: src/matchmaking/Lobby.java
package matchmaking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a simple 1v1 lobby where a host can invite another player.
 * This is used by LobbyManager and is independent of the rest of the project.
 */
public class Lobby {

    public enum Status {
        WAITING,
        FULL
    }

    private final String id;
    private final GameType gameType;
    private final Player host;
    private final List<Player> participants = new ArrayList<>(2);
    private Status status = Status.WAITING;

    public Lobby(String id, GameType gameType, Player host) {
        this.id = Objects.requireNonNull(id, "id");
        this.gameType = Objects.requireNonNull(gameType, "gameType");
        this.host = Objects.requireNonNull(host, "host");
        this.participants.add(host);
    }

    public String getId() {
        return id;
    }

    public GameType getGameType() {
        return gameType;
    }

    public Player getHost() {
        return host;
    }

    public Status getStatus() {
        return status;
    }

    public List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }

    public boolean isFull() {
        return participants.size() >= 2;
    }

    public boolean canJoin(Player player) {
        Objects.requireNonNull(player, "player");
        return status == Status.WAITING
                && !isFull()
                && participants.stream().noneMatch(p -> p.equals(player));
    }

    public void addPlayer(Player player) {
        if (!canJoin(player)) {
            throw new IllegalStateException("Cannot join lobby " + id);
        }
        participants.add(player);
        if (isFull()) {
            status = Status.FULL;
        }
    }
}