package chat;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatChannelTest {

    @Test
    void addMessageStoresMessageInOrder() {
        ChatChannel channel = new ChatChannel("match-123");

        ChatMessage msg1 = new ChatMessage("p1", "Player1", "hello", Instant.now(), DeliveryStatus.DELIVERED);
        ChatMessage msg2 = new ChatMessage("p2", "Player2", "hi", Instant.now(), DeliveryStatus.DELIVERED);

        channel.addMessage(msg1);
        channel.addMessage(msg2);

        List<ChatMessage> history = channel.getHistory();
        assertEquals(2, history.size());
        assertSame(msg1, history.get(0));
        assertSame(msg2, history.get(1));
    }

    @Test
    void listenersAreNotifiedWhenMessageAdded() {
        ChatChannel channel = new ChatChannel("match-abc");
        List<ChatMessage> received = new ArrayList<>();

        ChatListener listener = received::add;
        channel.addListener(listener);

        ChatMessage msg = new ChatMessage("p1", "Player1", "test", Instant.now(), DeliveryStatus.DELIVERED);
        channel.addMessage(msg);

        assertEquals(1, received.size());
        assertSame(msg, received.get(0));
    }
}
