package chat;

import matchmaking.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest 
{
    @Test
    void gameSessionStoresMatchAndChatChannel() {
        // Create a minimal Match – adapt to your real constructor / factory
        Player alice = new Player("alice", "Alice", 1500);
        Player bob   = new Player("bob",   "Bob",   1520);

        Match dummyMatch = new Match(GameType.CHESS, alice, bob); // Using same player for simplicity

        ChatChannel channel = new ChatChannel("test-match");

        GameSession session = new GameSession(dummyMatch, channel);

        assertSame(dummyMatch, session.getMatch());
        assertNotNull(session.getChatChannel());
        assertEquals("test-match", session.getChatChannel().getChannelId());
    }
}
