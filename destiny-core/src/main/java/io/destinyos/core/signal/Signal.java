package io.destinyos.core.signal;

import java.util.List;
import java.util.Objects;

/**
 * The normalisation layer between an engine and Fusion
 * (Master Spec §6, FUSION_ENGINE_SPEC §3).
 *
 * <p>This type is the reason Fusion can be built before Bát Tự exists: Fusion
 * consumes {@code Signal}, never a concrete engine (ADR D5). Every engine
 * reduces its findings to this shape, and Fusion has no way to special-case
 * any of them.
 *
 * <p><strong>No numeric score, weight or confidence appears here</strong>
 * (ADR D6). Once a {@code double} exists in this record, averaging it becomes
 * the path of least resistance and the evidence-based fusion the specification
 * demands quietly degrades into the weighted average it forbids.
 *
 * @param signalId    stable identifier within one calculation
 * @param engine      the engine that emitted this — the unit of source diversity
 * @param school      the school/methodology within that engine (Rule D)
 * @param dimension   life area this speaks to
 * @param tag         machine-readable label, e.g. {@code FINANCE_CAUTION}
 * @param polarity    direction
 * @param strength    magnitude — never a probability
 * @param applicability whether this participates in fusion at all
 * @param critical    sole encoding of criticality (DECISION_LOG C3). A critical
 *                    signal MUST survive a majority vote against it
 *                    (FUSION_ENGINE_SPEC §9)
 * @param evidenceIds the evidence this rests on
 * @param evidenceGroupId groups signals derived from the same evidence so
 *                    deduplication can avoid counting one finding many times
 *                    (FUSION_ENGINE_SPEC §5)
 */
public record Signal(
        String signalId,
        String engine,
        String school,
        Dimension dimension,
        String tag,
        Polarity polarity,
        Strength strength,
        Applicability applicability,
        boolean critical,
        List<String> evidenceIds,
        String evidenceGroupId
) {
    public Signal {
        Objects.requireNonNull(signalId, "signalId");
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(polarity, "polarity");
        Objects.requireNonNull(strength, "strength");
        Objects.requireNonNull(applicability, "applicability");
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
    }

    /** Whether this signal takes part in fusion (FUSION_ENGINE_SPEC §4). */
    public boolean participates() {
        return applicability.participates();
    }

    /**
     * A critical signal that participates. These are the signals
     * FUSION_ENGINE_SPEC §9 says majority voting must never erase.
     */
    public boolean isActiveCritical() {
        return critical && participates();
    }
}
