package io.destinyos.engine;

import io.destinyos.core.signal.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * What an engine can do, and what it needs to do it (command §4).
 *
 * <p>Declared rather than discovered, so the Applicability layer can decide
 * whether an engine belongs in a scenario before running it. An engine that
 * cannot contribute to a scenario should never consume a thread.
 *
 * @param dimensions       life areas this engine can speak to
 * @param requiresBirthTime whether an exact hour is needed. Master Spec §2
 *                         forbids treating UNKNOWN as EXACT, so an engine
 *                         declaring true here must decline imprecise input
 * @param requiresLocation whether birth location is needed
 * @param requiresName     whether a name is needed, e.g. Numerology
 * @param requiresCalendar whether the Calendar foundation is a prerequisite.
 *                         False for Numerology and Tarot — the property that
 *                         lets Phases 4 and 5 proceed while the Calendar
 *                         research is still open (ADR D2)
 * @param deterministic    whether the same input always yields the same output
 * @param requiresSeed     whether a seed is needed for reproducibility
 * @param supportedDateRange honest bounds; outside them the engine must
 *                         return NOT_APPLICABLE rather than extrapolate
 */
public record EngineCapability(
        Set<Dimension> dimensions,
        boolean requiresBirthTime,
        boolean requiresLocation,
        boolean requiresName,
        boolean requiresCalendar,
        boolean deterministic,
        boolean requiresSeed,
        SupportedDateRange supportedDateRange
) {
    public EngineCapability {
        dimensions = dimensions == null ? Set.of() : Set.copyOf(dimensions);
        supportedDateRange = supportedDateRange == null
                ? SupportedDateRange.unbounded()
                : supportedDateRange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean supports(Dimension dimension) {
        return dimensions.contains(dimension);
    }

    /** Builder — the record has enough boolean fields that positional construction misleads. */
    public static final class Builder {
        private Set<Dimension> dimensions = Set.of();
        private boolean requiresBirthTime;
        private boolean requiresLocation;
        private boolean requiresName;
        private boolean requiresCalendar;
        private boolean deterministic = true;
        private boolean requiresSeed;
        private SupportedDateRange range = SupportedDateRange.unbounded();

        public Builder dimensions(Dimension... values) {
            this.dimensions = Set.of(values);
            return this;
        }

        public Builder dimensions(List<Dimension> values) {
            this.dimensions = Set.copyOf(Objects.requireNonNull(values));
            return this;
        }

        public Builder requiresBirthTime(boolean v) { this.requiresBirthTime = v; return this; }
        public Builder requiresLocation(boolean v)  { this.requiresLocation = v;  return this; }
        public Builder requiresName(boolean v)      { this.requiresName = v;      return this; }
        public Builder requiresCalendar(boolean v)  { this.requiresCalendar = v;  return this; }
        public Builder deterministic(boolean v)     { this.deterministic = v;     return this; }
        public Builder requiresSeed(boolean v)      { this.requiresSeed = v;      return this; }
        public Builder supportedDateRange(SupportedDateRange v) { this.range = v; return this; }

        public EngineCapability build() {
            return new EngineCapability(dimensions, requiresBirthTime, requiresLocation,
                    requiresName, requiresCalendar, deterministic, requiresSeed, range);
        }
    }
}
