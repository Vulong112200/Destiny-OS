package io.destinyos.ai;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.Objects;

/**
 * One pruning candidate for the AI narrative payload
 * (AI_NARRATIVE_SPEC.md section 3, Master Spec section 22).
 *
 * <p>Carries the real domain enums so {@link NarrativePruner} can apply the
 * spec's priority rules exactly (critical / STRONG / scenario-relevant
 * MEDIUM), plus the Vietnamese label text the caller already resolved via
 * {@code VietnameseLabels} - this module never imports destiny-i18n or any
 * concrete engine, so it receives rendered text rather than re-deriving it.
 */
public record NarrativeSignalItem(
        String engine,
        Dimension dimension,
        String dimensionLabelVi,
        Polarity polarity,
        String polarityLabelVi,
        Strength strength,
        String strengthLabelVi,
        boolean critical,
        String tag) {

    public NarrativeSignalItem {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(dimensionLabelVi, "dimensionLabelVi");
        Objects.requireNonNull(polarity, "polarity");
        Objects.requireNonNull(polarityLabelVi, "polarityLabelVi");
        Objects.requireNonNull(strength, "strength");
        Objects.requireNonNull(strengthLabelVi, "strengthLabelVi");
    }

    /**
     * Dedup key per AI_NARRATIVE_SPEC section 3 ("Loai: ... duplicate").
     * Two signals from the same engine, dimension and tag with the same
     * polarity are the same finding reported twice, not two findings.
     */
    String dedupeKey() {
        return engine + "|" + dimension + "|" + polarity + "|" + tag;
    }
}
