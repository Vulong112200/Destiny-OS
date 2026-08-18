package io.destinyos.core.context;

import io.destinyos.core.version.CalendarMethodologyRef;
import io.destinyos.core.version.MethodologyVersions;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Everything needed to reproduce a calculation (CLAUDE.md §4, Master Spec §25).
 *
 * <p>Carries the fields CLAUDE.md §4 mandates, plus the extensions ADR D3
 * requires. D3's finding was that {@code calendarVersion} alone is not enough:
 * a Vietnamese birth datum's Can Chi depends on which historical timezone rule
 * was in force at that date <em>in that region</em>, so the resolved rule and
 * its source must travel with the result.
 *
 * <p>{@code uncertainties} is the field that keeps the system honest. When the
 * calendar cannot resolve a case, that fact is recorded here and propagates
 * outward rather than being quietly resolved to something plausible.
 *
 * @param calculationId       unique per calculation
 * @param school              selected school (CLAUDE.md Rule D)
 * @param versions            methodology/algorithm/rule/calendar versions
 * @param timezone            timezone used for interpretation
 * @param locale              output locale — Vietnamese in production
 * @param seed                present only where the methodology uses randomness
 * @param calculatedAt        when this ran
 * @param birthRegion         region for historical rule resolution (D3, R17)
 * @param calendarMethodology which calendar methodology, and the rule it resolved (D3)
 * @param birthTimePrecision  never treat UNKNOWN as EXACT (Master Spec §2)
 * @param uncertainties       unresolved conditions that MUST reach the user (D3)
 */
public record CalculationContext(
        String calculationId,
        String school,
        MethodologyVersions versions,
        ZoneId timezone,
        Locale locale,
        Long seed,
        Instant calculatedAt,
        String birthRegion,
        CalendarMethodologyRef calendarMethodology,
        BirthTimePrecision birthTimePrecision,
        List<Uncertainty> uncertainties
) {
    public CalculationContext {
        Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(versions, "versions");
        Objects.requireNonNull(timezone, "timezone");
        Objects.requireNonNull(calculatedAt, "calculatedAt");
        locale = locale == null ? Locale.forLanguageTag("vi-VN") : locale;
        birthTimePrecision = birthTimePrecision == null
                ? BirthTimePrecision.UNKNOWN
                : birthTimePrecision;
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);
    }

    /** Present only where the methodology uses randomness (Master Spec §17). */
    public Optional<Long> seedIfPresent() {
        return Optional.ofNullable(seed);
    }

    public Optional<CalendarMethodologyRef> calendarMethodologyIfPresent() {
        return Optional.ofNullable(calendarMethodology);
    }

    /**
     * Whether any recorded uncertainty could change the result. When true the
     * UI MUST warn the user rather than presenting the outcome as settled
     * (ADR D3, audit risk RK3).
     */
    public boolean hasResultAffectingUncertainty() {
        return uncertainties.stream().anyMatch(Uncertainty::affectsResult);
    }

    public List<Uncertainty> resultAffectingUncertainties() {
        return uncertainties.stream().filter(Uncertainty::affectsResult).toList();
    }

    /** Returns a copy with one more uncertainty recorded. Immutable. */
    public CalculationContext withUncertainty(Uncertainty uncertainty) {
        Objects.requireNonNull(uncertainty, "uncertainty");
        var merged = new java.util.ArrayList<>(uncertainties);
        merged.add(uncertainty);
        return new CalculationContext(calculationId, school, versions, timezone, locale,
                seed, calculatedAt, birthRegion, calendarMethodology, birthTimePrecision, merged);
    }

    /**
     * Stable rendering of calculation identity for hashing and cache keys
     * (CLAUDE.md §6, DECISION_LOG C7).
     *
     * <p>Region is included deliberately: under D3 two people born at the same
     * UTC instant in different regions may legitimately receive different
     * Can Chi, so region is part of identity, not incidental metadata.
     */
    public String toIdentityString() {
        return "school=" + school
                + ";" + versions.toCanonicalString()
                + ";tz=" + timezone.getId()
                + ";region=" + (birthRegion == null ? "-" : birthRegion)
                + ";calMeth=" + (calendarMethodology == null
                        ? "-" : calendarMethodology.methodologyId()
                                + "@" + calendarMethodology.methodologyVersion())
                + ";precision=" + birthTimePrecision
                + ";seed=" + (seed == null ? "-" : seed);
    }
}
