package matchmaking;

import chat.ChatChannel;
import chat.ChatMessage;
import chat.ChatService;
import org.junit.Test;

/**
 * Small demo / test class for the matchmaking and lobby system.
 *
 * You can either:
 *  - Run the main method manually, or
 *  - Run the JUnit test method below so VS Code's Test Runner recognizes it.
 */
public class MatchmakingTest {

    /**
     * JUnit test wrapper that simply calls the main demo method.
     * If anything throws an exception during matchmaking or chat setup,
     * this test will fail.
     */
    @Test
    public void matchmakingAndChatDemoRunsWithoutExceptions() {
        main(new String[0]);
    }

    /**
     * Manual demo entry point.
     *
     * Run this with (from project root):
     *   javac src/matchmaking/*.java src/chat/*.java
     *   java -cp src matchmaking.MatchmakingTest
     */
    public static void main(String[] args) {
        // --- 1. Create some players ---
        Player alice = new Player("alice", "Alice", 1500);
        Player bob   = new Player("bob",   "Bob",   1520);
        Player carol = new Player("carol", "Carol", 1800);

        System.out.println("=== Players ===");
        System.out.println(alice);
        System.out.println(bob);
        System.out.println(carol);
        System.out.println();

        // --- 2. Matchmaking demo ---
        Matchmaker matchmaker = new Matchmaker();

        // Alice and Bob search for a Chess match
        matchmaker.enqueue(alice, GameType.CHESS);
        matchmaker.enqueue(bob,   GameType.CHESS);

        // Carol searches for Tic-Tac-Toe
        matchmaker.enqueue(carol, GameType.TIC_TAC_TOE);

        System.out.println("=== Matchmaking ===");
        System.out.println("Queued for CHESS: " + matchmaker.getQueuedPlayers(GameType.CHESS));
        System.out.println("Queued for TIC_TAC_TOE: " + matchmaker.getQueuedPlayers(GameType.TIC_TAC_TOE));
        System.out.println();

        // Try to find any match
        Match match = matchmaker.findMatch();
        if (match != null) {
            System.out.println("Match found: " + match);

            // --- 2a. Chat demo: create a GameSession with a chat channel ---
            // Use the shared ChatService from AppContext to open a channel for this match.
            ChatService chatService = AppContext.getChatService();
            String matchId = buildMatchId(match);
            ChatChannel channel = chatService.openChannel(matchId);

            GameSession session = new GameSession(match, channel);
            System.out.println("Created GameSession for match: " + session.getMatch());

            // Simulate a couple of chat messages between the two players.
            chatService.sendMessage(
                    matchId,
                    match.getPlayer1().getId(),
                    match.getPlayer1().getName(),
                    "GLHF!"
            );
            chatService.sendMessage(
                    matchId,
                    match.getPlayer2().getId(),
                    match.getPlayer2().getName(),
                    "You too!"
            );

            System.out.println("Chat history for " + matchId + ":");
            for (ChatMessage message : chatService.getHistory(matchId)) {
                System.out.println("  [" + message.getTimestamp() + "] "
                        + message.getSenderName() + ": " + message.getContent());
            }
            System.out.println();
        } else {
            System.out.println("No match found yet.");
        }

        // Show updated queues after matching
        System.out.println("After matching:");
        System.out.println("Queued for CHESS: " + matchmaker.getQueuedPlayers(GameType.CHESS));
        System.out.println("Queued for TIC_TAC_TOE: " + matchmaker.getQueuedPlayers(GameType.TIC_TAC_TOE));
        System.out.println();

        // --- 3. Wait time estimate demo ---
        int etaCarol = matchmaker.estimateWait(carol);
        System.out.println("Estimated wait time for Carol (TIC_TAC_TOE): " + etaCarol + " seconds");
        System.out.println();

        // --- 4. Lobby demo ---
        LobbyManager lobbyManager = new LobbyManager();

        // Alice creates a GO lobby and gets an ID
        String lobbyId = lobbyManager.createLobby(alice, GameType.GO);
        System.out.println("=== Lobby ===");
        System.out.println("Alice created GO lobby with ID: " + lobbyId);

        // Bob joins Alice's lobby
        Lobby lobby = lobbyManager.joinLobby(lobbyId, bob);
        if (lobby != null) {
            System.out.println("Bob joined lobby " + lobbyId);
            System.out.println("Lobby participants: " + lobby.getParticipants());
            System.out.println("Lobby status: " + lobby.getStatus());
        } else {
            System.out.println("Bob could not join lobby " + lobbyId);
        }

        // Carol tries to join the same lobby (should fail because it is 1v1)
        Lobby lobbyForCarol = lobbyManager.joinLobby(lobbyId, carol);
        if (lobbyForCarol == null) {
            System.out.println("Carol could NOT join lobby " + lobbyId + " (lobby is full).");
        }
    }

    /**
     * Builds a deterministic id for this match so chat history can be
     * keyed consistently per game session.
     */
    private static String buildMatchId(Match match) {
        return match.getGameType() + "-"
                + match.getPlayer1().getId() + "-"
                + match.getPlayer2().getId();
    }
}
