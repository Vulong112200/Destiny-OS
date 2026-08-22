package io.destinyos.engines.bazi;

import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import java.time.Instant;
import java.util.Objects;

/**
 * Everything the Bát Tự chart needs, and nothing it does not.
 *
 * <p>Gender is deliberately absent. It is required for Đại Vận direction
 * (research item R2) and for nothing else in a chart, and R2 is open — so a
 * gender field here would sit unused while implying the engine does something
 * with it.
 *
 * @param utcInstant              birth instant in UTC
 * @param region                  jurisdiction for historical timezone
 *                                resolution (R14a/R14b). Use
 *                                {@link VietnameseRegion#UNKNOWN} when not
 *                                confidently known — the engine then declines
 *                                rather than assuming a side
 * @param longitudeDegreesIfKnown birth longitude, positive east, or
 *                                {@code null} to use civil clock time (R10).
 *                                Matters most for births near an hour-branch
 *                                or solar-term boundary
 * @param precision               never treat {@code UNKNOWN} as {@code EXACT}
 *                                (Master Spec §2): without an exact hour the
 *                                day and hour pillars are omitted, and with
 *                                them the Day Master and therefore every Thập
 *                                Thần
 */
public record BaziInput(
        Instant utcInstant,
        VietnameseRegion region,
        Double longitudeDegreesIfKnown,
        BirthTimePrecision precision
) {
    public BaziInput {
        Objects.requireNonNull(utcInstant, "utcInstant");
        region = region == null ? VietnameseRegion.UNKNOWN : region;
        precision = precision == null ? BirthTimePrecision.UNKNOWN : precision;
    }
}
