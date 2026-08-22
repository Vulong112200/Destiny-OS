package io.destinyos.engines.bazi;

import java.util.List;
import java.util.Objects;

/**
 * A part of a Bát Tự reading that this engine deliberately does not compute,
 * carried in the chart itself so the UI shows it as *blocked with a reason*
 * rather than simply missing (ADR D7 applied inside a payload rather than to
 * a whole engine).
 *
 * <p>This type is why {@code BaziChart} is honest. Without it, a chart with
 * no Dụng Thần section looks like a chart whose Dụng Thần happens to be
 * empty — and the single most-asked-for output of Bát Tự would silently
 * vanish instead of explaining that the school selection is open (R1).
 *
 * @param sectionId      stable id, e.g. {@code DUNG_THAN}
 * @param displayNameVi  Vietnamese name of the section (CLAUDE.md §9)
 * @param researchId     register entry in {@code docs/RESEARCH_BLOCKERS.md}
 * @param reasonVi       why it is blocked, in Vietnamese, for the user
 * @param knownVariants  schools known to differ, so the gap reads as a real
 *                       disagreement rather than an oversight (Rule D)
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
