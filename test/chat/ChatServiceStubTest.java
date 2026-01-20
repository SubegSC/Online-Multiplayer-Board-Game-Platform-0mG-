package chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceStubTest {

    private ChatServiceStub service;

    @BeforeEach
    void setUp() {
        service = new ChatServiceStub();
    }

    @Test
    void sendMessageStoresMessageInChannel() {
        String matchId = "match-1";

        ChatMessage message = service.sendMessage(matchId, "p1", "Player1", "  hello world  ");

        assertEquals("p1", message.getSenderId());
        assertEquals("Player1", message.getSenderName());
        assertEquals("hello world", message.getContent());
        assertEquals(DeliveryStatus.DELIVERED, message.getStatus());

        List<ChatMessage> history = service.getHistory(matchId);
        assertEquals(1, history.size());
        assertEquals(message.getContent(), history.get(0).getContent());
    }

    @Test
    void sendMessageThrowsOnEmptyText() {
        String matchId = "match-2";

        assertThrows(IllegalArgumentException.class, () ->
                service.sendMessage(matchId, "p1", "Player1", "   ")
        );
    }

    @Test
    void messagesAreIsolatedPerMatch() {
        String match1 = "match-1";
        String match2 = "match-2";

        service.sendMessage(match1, "p1", "Player1", "hello in 1");
        service.sendMessage(match2, "p2", "Player2", "hello in 2");

        List<ChatMessage> history1 = service.getHistory(match1);
        List<ChatMessage> history2 = service.getHistory(match2);

        assertEquals(1, history1.size());
        assertEquals(1, history2.size());
        assertEquals("hello in 1", history1.get(0).getContent());
        assertEquals("hello in 2", history2.get(0).getContent());
    }

    @Test
    void simulateFailureMarksMessagesAsFailed() {
        String matchId = "match-3";

        service.setSimulateFailure(true);
        ChatMessage message = service.sendMessage(matchId, "p1", "Player1", "this may fail");

        assertEquals(DeliveryStatus.FAILED, message.getStatus());
    }
}
