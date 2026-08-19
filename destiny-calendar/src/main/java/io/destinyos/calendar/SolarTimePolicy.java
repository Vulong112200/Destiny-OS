package io.destinyos.calendar;

/**
 * Versioned so a past result stays reproducible if a more precise policy
 * is adopted later (CLAUDE.md section 6) — see {@link SolarTimeCorrection}.
 */
public enum SolarTimePolicy {
    /**
     * Longitude-only mean solar time correction (4 minutes per degree from
     * the civil timezone's standard meridian). This is a deliberate scope
     * reduction from full "true"/apparent solar time (chân thái dương giờ,
     * R10): the equation-of-time (elliptical-orbit) component is
     * NOT_IMPLEMENTED in this version, flagged rather than guessed at,
     * since it is the one part of this phase with no independent
     * cross-check yet available.
     */
    MEAN_SOLAR_TIME_V1,
    /** Civil clock time used unmodified — the R10 fallback when longitude is unknown. */
    CIVIL_TIME_NO_CORRECTION
}
