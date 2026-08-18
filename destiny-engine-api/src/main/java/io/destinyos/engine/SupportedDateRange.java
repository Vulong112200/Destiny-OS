package io.destinyos.engine;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The date span an engine can honestly serve.
 *
 * <p>This matters more than it looks. Every sourced calendar dataset covers a
 * finite range (research items R9, R15). An engine asked for a date outside its
 * range must return {@code NOT_APPLICABLE} with an explanation — never
 * extrapolate. Extrapolating past the end of a sourced dataset is fabrication
 * wearing the costume of arithmetic, and CLAUDE.md Rule C forbids it just as
 * firmly as inventing a formula outright.
 */
public record SupportedDateRange(LocalDate from, LocalDate to) {

    /** For engines with no date dependency at all, e.g. a Tarot draw. */
    public static SupportedDateRange unbounded() {
        return new SupportedDateRange(null, null);
    }

    public static SupportedDateRange of(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must not precede from");
        }
        return new SupportedDateRange(from, to);
    }

    public boolean covers(LocalDate date) {
        Objects.requireNonNull(date, "date");
        boolean afterStart = from == null || !date.isBefore(from);
        boolean beforeEnd  = to   == null || !date.isAfter(to);
        return afterStart && beforeEnd;
    }

    public String describe() {
        if (from == null && to == null) {
            return "unbounded";
        }
        return (from == null ? "-" : from) + " .. " + (to == null ? "-" : to);
    }
}
