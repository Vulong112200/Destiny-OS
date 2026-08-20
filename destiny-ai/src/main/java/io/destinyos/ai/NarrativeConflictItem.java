package io.destinyos.ai;

import java.util.List;
import java.util.Objects;

/**
 * One Fusion conflict, already rendered to Vietnamese text by the caller.
 * Conflicts are never pruned away by {@link NarrativePruner} - Master Spec
 * section 22 lists "giu conflict" ahead of every signal-selection rule, and
 * CLAUDE.md Rule E treats conflict as a valid result, not noise to filter.
 *
 * @param dimension nullable - a {@code SCOPE_CONFLICT} has no single dimension
 */
public record NarrativeConflictItem(
        String typeLabelVi,
        String dimension,
        List<String> involvedEngines,
        String description) {

    public NarrativeConflictItem {
        Objects.requireNonNull(typeLabelVi, "typeLabelVi");
        Objects.requireNonNull(description, "description");
        involvedEngines = involvedEngines == null ? List.of() : List.copyOf(involvedEngines);
    }
}
