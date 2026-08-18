package io.destinyos.core.result;

import java.util.Objects;

/**
 * A failure record. Deliberately carries no stack trace and no throwable:
 * this crosses module and API boundaries, and CLAUDE.md §40 forbids leaking
 * internals into logs and responses.
 *
 * @param code      machine-readable failure class
 * @param message   safe, user-appropriate description
 * @param engine    which engine failed — so isolation is visible in the result
 */
public record EngineError(String code, String message, String engine) {
    public EngineError {
        Objects.requireNonNull(code, "code");
    }

    public static EngineError of(String code, String message, String engine) {
        return new EngineError(code, message, engine);
    }

    public static EngineError timeout(String engine, long timeoutMs) {
        return new EngineError(
                "ENGINE_TIMEOUT",
                "Engine exceeded its time budget of " + timeoutMs + " ms.",
                engine);
    }
}
