package chat;

import matchmaking.GameSession;
import matchmaking.Match; // Added import to access Match details
import java.util.List;
import java.util.Objects;

/**
 * Controller class that will mediate between the GUI and the chat backend.
 *
 * For now this is a simple skeleton: it knows the GameSession and ChatService,
 * and exposes methods that the GUI can call. The actual GUI wiring will be done later.
 */
public class ChatController {

    private final GameSession session;
    private final ChatService chatService;

    public ChatController(GameSession session, ChatService chatService) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.chatService = Objects.requireNonNull(chatService, "chatService must not be null");
    }

    public GameSession getSession() {
        return session;
    }

    /**
     * Sends a chat message on behalf of the given player.
     * This method will be called from the GUI.
     */
    public ChatMessage send(String senderId, String senderName, String rawText) {
        // MATCH ID FIX: Match class doesn't have getId(), so we generate it here.
        // Format: GAMETYPE-PLAYER1ID-PLAYER2ID
        String matchId = getMatchId();
        return chatService.sendMessage(matchId, senderId, senderName, rawText);
    }

    /**
     * Returns the current chat history for this session.
     * The GUI can call this when opening the in-game screen.
     */
    public List<ChatMessage> getHistory() {
        String matchId = getMatchId();
        return chatService.getHistory(matchId);
    }

    /**
     * Allows the GUI layer to register a listener for new messages.
     */
    public void addListener(ChatListener listener) {
        session.getChatChannel().addListener(listener);
    }

    public void removeListener(ChatListener listener) {
        session.getChatChannel().removeListener(listener);
    }

    // Helper to ensure we generate the Match ID consistently
    private String getMatchId() {
        Match m = session.getMatch();
        return m.getGameType() + "-" + m.getPlayer1().getId() + "-" + m.getPlayer2().getId();
    }
}