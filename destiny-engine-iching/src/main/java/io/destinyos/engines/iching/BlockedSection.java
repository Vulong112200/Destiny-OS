package io.destinyos.engines.iching;

import java.util.List;
import java.util.Objects;

/**
 * A part of an I Ching reading this engine deliberately does not compute,
 * carried on the reading itself so the UI shows it as *blocked with a
 * reason* rather than simply missing (ADR D7) — the same device
 * {@code destiny-engine-bazi} and {@code destiny-engine-astrology} use.
 *
 * @param sectionId     stable id, e.g. {@code LINE_JUDGMENT_TEXT}
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
