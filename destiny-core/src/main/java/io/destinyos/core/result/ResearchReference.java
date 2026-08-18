package io.destinyos.core.result;

import java.util.List;
import java.util.Objects;

/**
 * Structured explanation of why an engine returned {@code RESEARCH_REQUIRED}
 * or {@code NOT_IMPLEMENTED} (ADR D7).
 *
 * <p>The point is that a blocked engine must be <em>informative</em>. "Chưa
 * triển khai" with no reason teaches the user nothing; naming the missing
 * methodology and pointing at the research record turns a gap into something
 * legible. CLAUDE_CODE_WORKFLOW §8 requires the honest non-answer; this makes
 * it useful.
 *
 * @param researchId   register id, e.g. {@code R1}, {@code R14}
 * @param domain       e.g. {@code Bát Tự}, {@code Calendar}
 * @param missing      precisely what is missing — not a vague apology
 * @param blockedSince methodology/spec reference where the gap was recorded
 * @param knownVariants schools known to differ, so the gap is legible as a real
 *                     disagreement rather than an oversight (CLAUDE.md Rule D)
 */
public record ResearchReference(
        String researchId,
        String domain,
        String missing,
        String blockedSince,
        List<String> knownVariants
) {
    public ResearchReference {
        Objects.requireNonNull(researchId, "researchId");
        Objects.requireNonNull(missing, "missing");
        knownVariants = knownVariants == null ? List.of() : List.copyOf(knownVariants);
    }

    public static ResearchReference of(String researchId, String domain, String missing) {
        return new ResearchReference(researchId, domain, missing, null, List.of());
    }
}
