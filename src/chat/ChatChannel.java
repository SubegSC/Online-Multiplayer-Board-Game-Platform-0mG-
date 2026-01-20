package chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a chat channel associated with a single game session / match.
 * Stores message history and notifies registered listeners of new messages.
 */
public class ChatChannel {

    private final String channelId; // typically a matchId or sessionId
    private final List<ChatMessage> messages = new ArrayList<>();
    private final List<ChatListener> listeners = new ArrayList<>();

    public ChatChannel(String channelId) {
        this.channelId = Objects.requireNonNull(channelId, "channelId must not be null");
    }

    public String getChannelId() {
        return channelId;
    }

    /**
     * Appends a new message to the channel and notifies listeners.
     */
    public void addMessage(ChatMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        messages.add(message);
        notifyListeners(message);
    }

    /**
     * Returns an unmodifiable view of the message history.
     * Messages are in chronological order of insertion.
     */
    public List<ChatMessage> getHistory() {
        return Collections.unmodifiableList(messages);
    }

    public void addListener(ChatListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(ChatMessage message) {
        for (ChatListener listener : new ArrayList<>(listeners)) {
            listener.onMessageAdded(message);
        }
    }
}
