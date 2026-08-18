package io.destinyos.core.evidence;

import io.destinyos.core.signal.Dimension;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An explainable finding produced by a deterministic engine (Master Spec §5,
 * DATA_MODEL_AND_RETENTION §5).
 *
 * <p>Evidence is what makes a result auditable: it names the rule that fired,
 * the version of that rule, and the fact it established. The UI's
 * "Vì sao có kết quả này?" panel is built from these.
 *
 * <p>{@code dimension} is present per DECISION_LOG C4 — Master Spec §5 omitted
 * it while the data model included it. Without it, pruning can only reach
 * evidence through a Signal, which fails for evidence not yet attached to one.
 *
 * @param evidenceId      stable identifier within one calculation
 * @param engine          emitting engine
 * @param school          school/methodology within that engine
 * @param ruleId          the rule that fired
 * @param ruleVersion     that rule's version — required for reproducibility
 * @param dimension       life area (C4)
 * @param fact            structured finding, never prose. Prose belongs to the
 *                        narrative layer, which must not be the source of truth
 * @param source          provenance, e.g. {@code rule-table}, {@code dataset}
 * @param evidenceGroupId groups related evidence for deduplication
 * @param dataConfidence  optional; absent unless the methodology defines it (C8)
 */
public record Evidence(
        String evidenceId,
        String engine,
        String school,
        String ruleId,
        String ruleVersion,
        Dimension dimension,
        Map<String, Object> fact,
        String source,
        String evidenceGroupId,
        DataConfidence dataConfidence
) {
    public Evidence {
        Objects.requireNonNull(evidenceId, "evidenceId");
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(ruleVersion, "ruleVersion");
        fact = fact == null ? Map.of() : Map.copyOf(fact);
    }

    /** Empty unless the methodology defines a data-quality marker (C8). */
    public Optional<DataConfidence> dataConfidenceIfDefined() {
        return Optional.ofNullable(dataConfidence);
    }
}
