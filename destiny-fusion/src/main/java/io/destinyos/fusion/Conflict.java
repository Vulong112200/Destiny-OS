package io.destinyos.fusion;

import io.destinyos.core.signal.Dimension;
import java.util.List;
import java.util.Objects;

/**
 * One detected conflict (FUSION_ENGINE_SPEC.md section 8). Evidence is kept,
 * never resolved away — a {@code METHODOLOGY_CONFLICT} in particular must
 * reach the user as two named positions, not a synthesized compromise
 * (Master Spec section 10 Rule F7).
 *
 * @param type            which kind of conflict this is
 * @param dimension       the dimension it occurred in, when the type is
 *                        dimension-scoped ({@code DIRECT_CONFLICT},
 *                        {@code METHODOLOGY_CONFLICT}); {@code null} for
 *                        {@code SCOPE_CONFLICT}, which by definition spans
 *                        more than one dimension
 * @param involvedEngines the distinct engines on either side of the conflict
 * @param description     a plain statement of what disagrees with what —
 *                        this is what the UI's "Điểm khác biệt" panel reads
 */
public record Conflict(ConflictType type, Dimension dimension, List<String> involvedEngines,
                       String description) {
    public Conflict {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
        involvedEngines = involvedEngines == null ? List.of() : List.copyOf(involvedEngines);
    }
}
