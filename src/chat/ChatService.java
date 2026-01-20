package chat;

import java.util.List;

/**
 * Abstraction for chat behaviour.
 * High-level modules depend on this interface rather than a concrete implementation.
 */
public interface ChatService {

    /**
     * Opens (or retrieves) the chat channel for the given match.
     *
     * @param matchId unique identifier for the match / game session
     * @return a ChatChannel representing the message history for this match
     */
    ChatChannel openChannel(String matchId);

    /**
     * Sends a message in the context of the given match.
     * Implementations may sanitize the text and set the delivery status.
     *
     * @param matchId   id of the match
     * @param senderId  unique sender id
     * @param senderName name to display
     * @param rawText   raw message text from the UI
     * @return the ChatMessage instance that was stored
     * @throws IllegalArgumentException if the message is empty or invalid
     */
    ChatMessage sendMessage(String matchId,
                            String senderId,
                            String senderName,
                            String rawText);

    /**
     * Loads the current history for the given match.
     *
     * @param matchId id of the match
     * @return list of ChatMessage objects in chronological order
     */
    List<ChatMessage> getHistory(String matchId);
}
