package matchmaking;

import chat.ChatChannel;

import java.util.Objects;

/**
 * Represents a running game session.
 * Wraps a Match together with its associated ChatChannel.
 *
 * More responsibilities (game controller, timers, etc.) can be added later.
 */
public class GameSession {

    private final Match match;
    private final ChatChannel chatChannel;

    public GameSession(Match match, ChatChannel chatChannel) {
        this.match = Objects.requireNonNull(match, "match must not be null");
        this.chatChannel = Objects.requireNonNull(chatChannel, "chatChannel must not be null");
    }

    public Match getMatch() {
        return match;
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }
}
