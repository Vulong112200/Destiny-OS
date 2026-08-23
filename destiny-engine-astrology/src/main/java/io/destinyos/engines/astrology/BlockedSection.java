package io.destinyos.engines.astrology;

import java.util.List;
import java.util.Objects;

/**
 * A part of a Western astrology reading this engine deliberately does not
 * compute, carried in the chart itself so the UI shows it as *blocked with a
 * reason* rather than simply missing (ADR D7 applied inside a payload rather
 * than to a whole engine) — the same device {@code destiny-engine-bazi} uses
 * for Dụng Thần and Day Master strength.
 *
 * @param sectionId     stable id, e.g. {@code PLANETS_BEYOND_SUN}
 * @param displayNameVi Vietnamese name of the section (CLAUDE.md §9)
 * @param researchId    register entry in {@code docs/RESEARCH_BLOCKERS.md}
 * @param reasonVi      why it is blocked, in Vietnamese, for the user
 * @param knownVariants schools/options known to differ, so the gap reads as
 *                      a real open question rather than an oversight (Rule D)
 */
public record BlockedSection(
        String sectionId,
        String displayNameVi,
        String researchId,
        String reasonVi,
        List<String> knownVariants
) {
    public BlockedSection {
        Objects.requireNonNull(sectionId, "sectionId");
        Objects.requireNonNull(displayNameVi, "displayNameVi");
        Objects.requireNonNull(researchId, "researchId");
        Objects.requireNonNull(reasonVi, "reasonVi");
        knownVariants = knownVariants == null ? List.of() : List.copyOf(knownVariants);
    }
}
