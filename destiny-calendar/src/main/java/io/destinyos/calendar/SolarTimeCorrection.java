package io.destinyos.calendar;

import java.time.Duration;

/**
 * Mean solar time correction: the sun crosses 15 degrees of longitude per
 * hour (360 degrees / 24 hours), so 1 degree of longitude east or west of
 * the civil timezone's standard meridian is worth 4 minutes of clock-time
 * offset. This is elementary spherical timekeeping, not a disputed
 * methodology choice — the choice R10 actually made is *whether* to apply
 * it at all (yes, when longitude is known) and to what precision tier
 * ({@link SolarTimePolicy#MEAN_SOLAR_TIME_V1}, not full apparent solar
 * time with the equation-of-time term — deferred, see that enum's Javadoc).
 */
public final class SolarTimeCorrection {

    private static final double MINUTES_PER_DEGREE = 4.0;
    private static final double DEGREES_PER_HOUR = 15.0;

    private SolarTimeCorrection() {
    }

    /**
     * @param longitudeDegreesEast birth longitude, positive east
     * @param civilUtcOffsetHours  the civil UTC offset actually in force
     *                             (from {@link HistoricalTimezoneRuleTable}),
     *                             which fixes the standard meridian to
     *                             correct against
     * @return signed correction to ADD to civil clock time to get mean
     *         solar time
     */
    public static Duration meanSolarTimeCorrection(double longitudeDegreesEast, double civilUtcOffsetHours) {
        double standardMeridianDegrees = civilUtcOffsetHours * DEGREES_PER_HOUR;
        double diffDegrees = longitudeDegreesEast - standardMeridianDegrees;
        double minutes = diffDegrees * MINUTES_PER_DEGREE;
        return Duration.ofSeconds(Math.round(minutes * 60));
    }
}
