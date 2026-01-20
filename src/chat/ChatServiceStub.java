package chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory stub implementation of ChatService.
 * 
 * For Project Iteration 3 this simulates how a real chat backend would behave.
 * Later, a real implementation could replace this one while keeping the same interface.
 */
public class ChatServiceStub implements ChatService {

    private final Map<String, ChatChannel> channels = new HashMap<>();

    /**
     * Flag used to simulate message delivery failures for testing and UC9 alt flows.
     */
    private boolean simulateFailure = false;

    @Override
    public synchronized ChatChannel openChannel(String matchId) {
        return channels.computeIfAbsent(matchId, ChatChannel::new);
    }

    @Override
    public synchronized ChatMessage sendMessage(String matchId,
                                                String senderId,
                                                String senderName,
                                                String rawText) {
        if (rawText == null) {
            throw new IllegalArgumentException("Message text must not be null");
        }

        String sanitized = sanitize(rawText);
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Message text must not be empty after sanitization");
        }

        DeliveryStatus status = simulateFailure ? DeliveryStatus.FAILED : DeliveryStatus.DELIVERED;

        ChatMessage message = new ChatMessage(
                senderId,
                senderName,
                sanitized,
                Instant.now(),
                status
        );

        ChatChannel channel = openChannel(matchId);
        channel.addMessage(message);

        return message;
    }

    @Override
    public synchronized List<ChatMessage> getHistory(String matchId) {
        ChatChannel channel = channels.get(matchId);
        if (channel == null) {
            return List.of();
        }
        return new ArrayList<>(channel.getHistory());
    }

    /**
     * Very simple sanitization: trim whitespace and collapse line breaks.
     * Can be extended later if needed.
     */
    private String sanitize(String raw) {
        String trimmed = raw.trim();
        // For now just collapse any newlines to spaces.
        return trimmed.replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Enables or disables simulated failures.
     * When enabled, sendMessage will produce messages with FAILED status.
     */
    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    /**
     * For testing or debugging: clears all channels and history.
     */
    public synchronized void clearAll() {
        channels.clear();
    }
}
