package matchmaking;

import chat.ChatService;
import chat.ChatServiceStub;

/**
 * Application-wide context for shared services.
 *
 * For Project Iteration 3, this currently exposes a single shared ChatService
 * instance that can be used by matchmaking, game sessions, and GUI code.
 *
 * Later, other shared services (e.g., persistence or networking) could be
 * added here behind appropriate interfaces.
 */
public final class AppContext {

    // Single shared in-memory chat service for the whole application.
    private static final ChatServiceStub CHAT_SERVICE = new ChatServiceStub();

    // Prevent instantiation.
    private AppContext() {
    }

    /**
     * Returns the shared ChatService instance.
     * High-level code should depend on the ChatService interface, not the stub.
     */
    public static ChatService getChatService() {
        return CHAT_SERVICE;
    }

    /**
     * Package-private accessor for tests or internal configuration if needed.
     * Other packages should use getChatService() instead.
     */
    static ChatServiceStub getChatServiceStub() {
        return CHAT_SERVICE;
    }
}

