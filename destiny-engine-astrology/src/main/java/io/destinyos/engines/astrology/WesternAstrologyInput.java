package io.destinyos.engines.astrology;

import java.time.Instant;
import java.util.Objects;

/**
 * Everything a Western astrology chart needs.
 *
 * <p><strong>Deliberately not routed through {@code HistoricalTimezoneRuleTable}</strong>
 * the way Bát Tự and Phong Thủy are. That machinery exists to resolve
 * Vietnam's own historical civil-time offset (research items R14a/R14b) for
 * charts whose subject is assumed to be in Vietnam. A natal chart's birth
 * location is not Vietnam-specific in general, so this input takes the birth
 * instant as an already-resolved UTC {@link Instant} and a geographic
 * location directly — the same shape essentially every astrology calculator
 * uses, and the caller's responsibility to have converted correctly for
 * whatever place and era the birth occurred in.
 *
 * @param utcInstant             birth instant in UTC
 * @param latitudeDegrees        birth latitude, positive north, in
 *                               [-90, 90]. Needed for the Ascendant, which
 *                               (unlike the Midheaven) depends on latitude
 * @param longitudeDegreesEast   birth longitude, positive east, in
 *                               [-180, 180] — matches the sign convention
 *                               {@code BaziInput.longitudeDegreesIfKnown}
 *                               already uses elsewhere in this project
 */
public record WesternAstrologyInput(
        Instant utcInstant,
        double latitudeDegrees,
        double longitudeDegreesEast
) {
    public WesternAstrologyInput {
        Objects.requireNonNull(utcInstant, "utcInstant");
    }
}
