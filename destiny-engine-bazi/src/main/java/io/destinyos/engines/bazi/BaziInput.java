package io.destinyos.engines.bazi;

import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.Gender;
import java.time.Instant;
import java.util.Objects;

/**
 * Everything the Bát Tự chart needs, and nothing it does not.
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
 * @param gender                  optional, and optional for a specific reason:
 *                                it decides the Đại Vận direction (R2) and
 *                                nothing else. The Tứ Trụ itself does not use
 *                                it, so a missing gender costs the luck cycles
 *                                and leaves the chart intact — unlike Phong
 *                                Thủy, where the Kua number <em>is</em> the
 *                                output and the engine must decline. Never
 *                                defaulted: a guessed direction runs the whole
 *                                sequence the wrong way while looking correct
 */
public record BaziInput(
        Instant utcInstant,
        VietnameseRegion region,
        Double longitudeDegreesIfKnown,
        BirthTimePrecision precision,
        Gender gender
) {
    public BaziInput {
        Objects.requireNonNull(utcInstant, "utcInstant");
        region = region == null ? VietnameseRegion.UNKNOWN : region;
        precision = precision == null ? BirthTimePrecision.UNKNOWN : precision;
    }
}
