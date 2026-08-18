package io.destinyos.core.result;

import java.util.Objects;

/**
 * A caveat attached to an otherwise usable result — a boundary condition, an
 * assumption, an input precision limit.
 *
 * <p>Warnings are pruning-priority material for the narrative layer
 * (AI_NARRATIVE_SPEC §3) and must survive into the AI payload rather than
 * being trimmed as noise.
 *
 * @param code       machine-readable, for the Vietnamese label registry
 * @param message    human-readable detail
 * @param critical   whether this must not be dropped during pruning
 */
public record EngineWarning(String code, String message, boolean critical) {
    public EngineWarning {
        Objects.requireNonNull(code, "code");
    }

    public static EngineWarning of(String code, String message) {
        return new EngineWarning(code, message, false);
    }

    public static EngineWarning critical(String code, String message) {
        return new EngineWarning(code, message, true);
    }
}
