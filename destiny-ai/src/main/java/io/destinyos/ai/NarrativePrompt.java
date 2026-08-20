package io.destinyos.ai;

import java.util.List;

/** A provider-agnostic chat-completion request: system contract + pruned user payload. */
public record NarrativePrompt(List<ChatMessage> messages) {

    public NarrativePrompt {
        messages = List.copyOf(messages);
    }
}
