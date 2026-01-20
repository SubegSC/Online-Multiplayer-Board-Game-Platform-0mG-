// File: src/matchmaking/Match.java
package matchmaking;

import java.util.Objects;

/**
 * Result of a successful matchmaking operation: two players paired
 * for a specific game type.
 *
 * This replaces any dependency on a Game class and is fully self-contained.
 */
public class Match {

    private final GameType gameType;
    private final Player player1;
    private final Player player2;

    public Match(GameType gameType, Player player1, Player player2) {
        this.gameType = Objects.requireNonNull(gameType, "gameType");
        this.player1 = Objects.requireNonNull(player1, "player1");
        this.player2 = Objects.requireNonNull(player2, "player2");
    }

    public GameType getGameType() {
        return gameType;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    @Override
    public String toString() {
        return "Match{" +
                "gameType=" + gameType +
                ", player1=" + player1 +
                ", player2=" + player2 +
                '}';
    }
}