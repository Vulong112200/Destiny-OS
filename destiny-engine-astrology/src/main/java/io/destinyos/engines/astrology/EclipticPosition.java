package io.destinyos.engines.astrology;

/**
 * A geocentric ecliptic position — longitude, latitude, and distance —
 * shared return shape for {@link Vsop87PlanetPosition} (the seven planets
 * beyond the Sun) and {@link Elp2000MoonPosition} (the Moon).
 *
 * <p>Reference frame: ecliptic and equinox of J2000.0, geometric
 * (astrometric) position — no light-time iteration, no aberration or
 * nutation correction. See both classes' Javadoc for why: this is the same
 * simplification {@code destiny-calendar}'s {@code SolarPosition} already
 * accepts, at a precision level (a few arcseconds to a few arcminutes,
 * cross-checked against JPL Horizons in each class's test suite) far finer
 * than astrology's narrowest orb.
 *
 * @param longitudeDegrees ecliptic longitude, normalized to [0, 360)
 * @param latitudeDegrees  ecliptic latitude, degrees (can be negative)
 * @param distanceAu       geocentric distance in astronomical units
 */
public record EclipticPosition(
        double longitudeDegrees,
        double latitudeDegrees,
        double distanceAu
) {
}
