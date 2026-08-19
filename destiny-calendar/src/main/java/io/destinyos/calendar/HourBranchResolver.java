package io.destinyos.calendar;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Maps a time-of-day to its two-hour Earthly Branch bucket under
 * {@link ZiHourBoundaryPolicy#ZI_HOUR_23_00} (R10): Tý covers 23:00-00:59,
 * Sửu 01:00-02:59, and so on in order, wrapping at 23:00, not midnight.
 */
public final class HourBranchResolver {

    private HourBranchResolver() {
    }

    /** Earthly Branch for the given time-of-day. Only {@link #ZI_HOUR_23_00} exists so far. */
    public static EarthlyBranch branchAt(LocalTime time, ZiHourBoundaryPolicy policy) {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(policy, "policy");
        int hour = time.getHour();
        int shifted = (hour + 1) % 24; // so that 23:00 aligns to the start of the Ty bucket
        int branchIndex = shifted / 2 + 1;
        return EarthlyBranch.fromIndex(branchIndex);
    }

    /**
     * Whether this time-of-day belongs to the *next* calendar date's day
     * pillar under R10 — true only for the 23:00-23:59 window.
     */
    public static boolean rollsOverToNextDay(LocalTime time, ZiHourBoundaryPolicy policy) {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(policy, "policy");
        return time.getHour() == 23;
    }
}
