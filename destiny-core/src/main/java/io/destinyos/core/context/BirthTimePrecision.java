package io.destinyos.core.context;

/**
 * How well the birth time is known (Master Spec §2).
 *
 * <p>Master Spec §2 is blunt about this: "Không được coi UNKNOWN là EXACT."
 * An unknown birth time cannot yield an hour pillar, and pretending otherwise
 * manufactures precision that was never in the input.
 */
public enum BirthTimePrecision {
    EXACT,
    APPROXIMATE,
    UNKNOWN;

    /** Whether methodologies requiring a precise hour may run at all. */
    public boolean supportsHourPrecision() {
        return this == EXACT;
    }
}
