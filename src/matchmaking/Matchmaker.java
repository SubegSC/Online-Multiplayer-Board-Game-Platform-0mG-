// File: src/matchmaking/Matchmaker.java
package matchmaking;

import java.util.*;

/**
 * Matchmaker is responsible for:
 * - Queueing players for a given game type
 * - Removing players from the queue
 * - Pairing players based on rating similarity
 * - Estimating wait time for a player
 *
 */
public class Matchmaker {

    /** Maximum rating difference allowed when pairing players. */
    private static final int MAX_RATING_DIFFERENCE = 200;

    /** Base wait time (seconds) used by estimateWait(). */
    private static final int BASE_WAIT_SECONDS = 15;

    /**
     * For each game type, we keep a queue of players waiting for a match.
     */
    private final Map<GameType, List<QueuedPlayer>> queues =
            new EnumMap<>(GameType.class);

    public Matchmaker() {
        for (GameType type : GameType.values()) {
            queues.put(type, new ArrayList<>());
        }
    }

    /**
     * Internal data holder for a player in the matchmaking queue.
     */
    private static final class QueuedPlayer {
        final Player player;
        final GameType gameType;
        final int rating;
        final long enqueuedAtMillis;

        QueuedPlayer(Player player, GameType gameType) {
            this.player = player;
            this.gameType = gameType;
            this.rating = player.getRating();
            this.enqueuedAtMillis = System.currentTimeMillis();
        }
    }

    /**
     * Adds a player to the queue for the given game type.
     * System adds to queue after player clicks Play.
     */
    public synchronized void enqueue(Player player, GameType gameType) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(gameType, "gameType");

        List<QueuedPlayer> queue = queues.get(gameType);

        // avoid duplicates in this game's queue
        for (QueuedPlayer qp : queue) {
            if (qp.player.equals(player)) {
                return;
            }
        }

        queue.add(new QueuedPlayer(player, gameType));
    }

    /**
     * Removes a player from all queues.
     * This covers the UC4 alternative: player cancels search.
     */
    public synchronized void dequeue(Player player) {
        Objects.requireNonNull(player, "player");
        for (List<QueuedPlayer> queue : queues.values()) {
            queue.removeIf(qp -> qp.player.equals(player));
        }
    }

    /**
     * Attempts to find a match for any game type.
     * If a suitable pair is found, removes them from the queue and returns
     * a Match instance. Otherwise, returns null.
     */
    public synchronized Match findMatch() {
        for (GameType type : GameType.values()) {
            Match match = findMatchForType(type);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    /**
     * Estimate the waiting time (in seconds) for a given player.
     * If the player is not currently in any queue, returns -1.
     */
    public synchronized int estimateWait(Player player) {
        Objects.requireNonNull(player, "player");

        for (List<QueuedPlayer> queue : queues.values()) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).player.equals(player)) {
                    int playersAhead = i;
                    int pairsAhead = playersAhead / 2;
                    return (pairsAhead + 1) * BASE_WAIT_SECONDS;
                }
            }
        }
        return -1;
    }

    /**
     * Returns a snapshot of the current queue for a given game type.
     * Useful for debugging or for UI to show "players searching".
     */
    public synchronized List<Player> getQueuedPlayers(GameType type) {
        Objects.requireNonNull(type, "type");
        List<Player> result = new ArrayList<>();
        for (QueuedPlayer qp : queues.get(type)) {
            result.add(qp.player);
        }
        return result;
    }

    // ------------ internal helpers ------------

    private Match findMatchForType(GameType type) {
        List<QueuedPlayer> queue = queues.get(type);
        if (queue.size() < 2) {
            return null;
        }

        // sort by rating so closest players are adjacent
        queue.sort(Comparator.comparingInt(qp -> qp.rating));

        QueuedPlayer bestA = null;
        QueuedPlayer bestB = null;
        int bestDiff = Integer.MAX_VALUE;

        for (int i = 0; i < queue.size() - 1; i++) {
            QueuedPlayer a = queue.get(i);
            QueuedPlayer b = queue.get(i + 1);
            int diff = Math.abs(a.rating - b.rating);
            if (diff < bestDiff && diff <= MAX_RATING_DIFFERENCE) {
                bestDiff = diff;
                bestA = a;
                bestB = b;
            }
        }

        if (bestA == null || bestB == null) {
            // nobody within acceptable rating range
            return null;
        }

        // remove both from queue
        queue.remove(bestA);
        queue.remove(bestB);

        // return self-contained Match object
        return new Match(type, bestA.player, bestB.player);
    }
}