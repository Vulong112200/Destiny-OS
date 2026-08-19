package io.destinyos.calendar;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One sourced (date-range, region) -&gt; UTC offset row (R14a).
 *
 * @param from        inclusive start date, or {@code null} for open-ended past
 * @param toExclusive exclusive end date, or {@code null} for open-ended future
 * @param region      {@code null} means the offset applied regardless of
 *                    region (before the 1955 partition, and again after the
 *                    1975 reunification) — not the same as
 *                    {@link VietnameseRegion#UNKNOWN}, which means the
 *                    *query* didn't say, not that the rule doesn't care
 * @param utcOffsetHours the offset in force
 * @param source      citation
 */
public record HistoricalTimezoneRule(
        LocalDate from,
        LocalDate toExclusive,
        VietnameseRegion region,
        double utcOffsetHours,
        String source
) {
    public HistoricalTimezoneRule {
        Objects.requireNonNull(source, "source");
    }

    public boolean covers(LocalDate date, VietnameseRegion queryRegion) {
        Objects.requireNonNull(date, "date");
        boolean afterStart = from == null || !date.isBefore(from);
        boolean beforeEnd = toExclusive == null || date.isBefore(toExclusive);
        boolean regionMatches = region == null || region == queryRegion;
        return afterStart && beforeEnd && regionMatches;
    }
}
