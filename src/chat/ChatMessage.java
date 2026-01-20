package chat;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single chat message within a game session.
 * Immutable: once created, the message contents do not change.
 */
public final class ChatMessage {

    private final String senderId;
    private final String senderName;
    private final String content;
    private final Instant timestamp;
    private final DeliveryStatus status;

    public ChatMessage(String senderId, String senderName, String content, Instant timestamp, DeliveryStatus status) {
        this.senderId = Objects.requireNonNull(senderId, "senderId must not be null");
        this.senderName = Objects.requireNonNull(senderName, "senderName must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + senderName + ": " + content + " (" + status + ")";
    }
}
