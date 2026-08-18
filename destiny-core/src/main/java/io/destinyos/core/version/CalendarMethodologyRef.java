package io.destinyos.core.version;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Which calendar methodology produced a result, and under what authority
 * (ADR D3).
 *
 * <p>D3 establishes that {@code calendarVersion} alone is insufficient for
 * reproducibility: the <em>resolved timezone rule</em> and its source must also
 * be captured, or a historical result cannot be defended after the methodology
 * is revised.
 *
 * <p>{@code resolvedTimezoneRule} being empty is meaningful, not missing data.
 * It says no sourced rule covered this (date, region) — which MUST surface as
 * {@link io.destinyos.core.context.UncertaintyKind#HISTORICAL_TIMEZONE_RULE_UNKNOWN}
 * rather than falling through to a default offset.
 *
 * @param methodologyId        e.g. {@code VN_TRADITIONAL}, {@code CN_UTC8}.
 *                             Vietnamese is the default; Chinese exists for
 *                             comparison and MUST NOT silently replace it
 * @param methodologyVersion   version of that methodology
 * @param effectiveFrom        start of its validity range
 * @param effectiveTo          end of its validity range
 * @param source               citation for the methodology
 * @param resolvedTimezoneRule the rule actually applied, if one covered this case
 * @param timezoneRuleSource   citation for that rule
 */
public record CalendarMethodologyRef(
        String methodologyId,
        String methodologyVersion,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String source,
        String resolvedTimezoneRule,
        String timezoneRuleSource
) {
    public CalendarMethodologyRef {
        Objects.requireNonNull(methodologyId, "methodologyId");
        Objects.requireNonNull(methodologyVersion, "methodologyVersion");
    }

    /** Empty when no sourced rule covered this (date, region) — see class doc. */
    public Optional<String> resolvedTimezoneRuleIfKnown() {
        return Optional.ofNullable(resolvedTimezoneRule);
    }

    /** Whether the requested date falls inside this methodology's validity range. */
    public boolean covers(LocalDate date) {
        Objects.requireNonNull(date, "date");
        boolean afterStart = effectiveFrom == null || !date.isBefore(effectiveFrom);
        boolean beforeEnd  = effectiveTo   == null || !date.isAfter(effectiveTo);
        return afterStart && beforeEnd;
    }
}
