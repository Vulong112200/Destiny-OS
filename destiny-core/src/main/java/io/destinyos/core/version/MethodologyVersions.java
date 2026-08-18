package io.destinyos.core.version;

import java.util.Objects;

/**
 * The version tuple that, together with the input and seed, defines calculation
 * identity (CLAUDE.md §6, Master Spec §25).
 *
 * <p>Grouped into one value object rather than scattered across
 * {@code CalculationContext} because these travel together everywhere: into the
 * cache key, into the result hash, into the audit record. Splitting them makes
 * it easy to forget one, and a forgotten version component means a cache that
 * silently serves results computed under superseded rules.
 *
 * @param methodologyVersion the school's methodology version
 * @param algorithmVersion   the implementation's algorithm version
 * @param ruleVersion        the rule/data table version
 * @param calendarVersion    the calendar dataset version
 */
public record MethodologyVersions(
        String methodologyVersion,
        String algorithmVersion,
        String ruleVersion,
        String calendarVersion
) {
    public MethodologyVersions {
        Objects.requireNonNull(methodologyVersion, "methodologyVersion");
        Objects.requireNonNull(algorithmVersion, "algorithmVersion");
        Objects.requireNonNull(ruleVersion, "ruleVersion");
    }

    /**
     * Stable, ordered rendering for cache keys and hash input.
     *
     * <p>Ordering is fixed deliberately: a hash whose input encoding varies is
     * not reproducible, which would defeat the purpose (DECISION_LOG C7).
     */
    public String toCanonicalString() {
        return "m=" + methodologyVersion
                + ";a=" + algorithmVersion
                + ";r=" + ruleVersion
                + ";c=" + (calendarVersion == null ? "-" : calendarVersion);
    }
}
