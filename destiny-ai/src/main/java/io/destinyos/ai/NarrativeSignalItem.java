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
 *
 * <p>{@code title} and {@code meaning} carry the engine's own <em>authored</em>
 * interpretive content - the Vietnamese text {@code TarotEngine} and
 * {@code NumerologyEngine} already write into {@code Evidence.fact.meaning},
 * research-gated as R11/R8. Without them this record described a signal
 * entirely in categories: "TAROT, Su nghiep, Ung ho, Manh" says a Tarot card
 * was favourable but not which card, and nothing at all about what the card
 * is understood to mean. Both the prompt and the deterministic fallback then
 * had no choice but to write generically. Passing the authored text through
 * is what lets the narrative layer stay a narrative layer (CLAUDE.md Rule B):
 * it restates content the deterministic engines produced instead of
 * improvising around a category label.
 *
 * <p>Both are nullable and mean <em>this engine authored none</em>. That is a
 * real and common state - a Tarot card with no meaning authored for a given
 * dimension, an engine with no interpretive corpus at all - and it must reach
 * the prompt as an absence. Filling it with a placeholder, a restatement of
 * the enums, or anything else invented here would be exactly the fabrication
 * Rule C forbids, laundered through the one layer least able to be audited.
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
        String tag,
        String title,
        String meaning) {

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
