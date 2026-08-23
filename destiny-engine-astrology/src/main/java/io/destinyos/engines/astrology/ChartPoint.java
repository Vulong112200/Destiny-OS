package io.destinyos.engines.astrology;

import java.util.Objects;

/**
 * One point of a chart at a tropical ecliptic longitude — the Sun, the
 * Midheaven, or the Ascendant, whichever this instance represents.
 *
 * @param eclipticLongitudeDegrees raw tropical ecliptic longitude, [0, 360)
 * @param sign                     the zodiac sign this longitude falls in
 * @param degreesIntoSign          how far into that sign, [0, 30)
 */
public record ChartPoint(
        double eclipticLongitudeDegrees,
        ZodiacSign sign,
        double degreesIntoSign
) {
    public ChartPoint {
        Objects.requireNonNull(sign, "sign");
    }

    static ChartPoint of(double eclipticLongitudeDegrees) {
        var position = ZodiacSign.at(eclipticLongitudeDegrees);
        double normalized = ((eclipticLongitudeDegrees % 360.0) + 360.0) % 360.0;
        return new ChartPoint(normalized, position.sign(), position.degreesIntoSign());
    }
}
