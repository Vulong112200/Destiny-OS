package io.destinyos.ai;

import java.util.Objects;

/** One message in a chat-completion request. {@code role} is "system" or "user". */
public record ChatMessage(String role, String content) {

    public ChatMessage {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(content, "content");
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }
}
