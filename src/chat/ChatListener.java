package chat;

/**
 * Listener interface for chat updates.
 * Implemented by UI controllers or other components
 * that want to be notified when a new message is added.
 */
public interface ChatListener {

    /**
     * Called whenever a new message is added to a ChatChannel.
     *
     * @param message the newly added message
     */
    void onMessageAdded(ChatMessage message);
}
