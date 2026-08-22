package io.destinyos.engines.fengshui;

import io.destinyos.calendar.VietnameseRegion;
import java.time.Instant;
import java.util.Objects;

/**
 * @param utcInstant              birth instant in UTC. Needed rather than just
 *                                a year because the Kua year boundary falls in
 *                                early February, so a January or early-February
 *                                birth cannot be resolved from a year alone
 * @param gender                  required: the male and female formulas differ
 *                                and are not symmetric (R7). No default is
 *                                offered, because every possible default would
 *                                be a fabricated answer for half of users
 * @param region                  jurisdiction for historical timezone
 *                                resolution (R14a/R14b)
 * @param longitudeDegreesIfKnown birth longitude, positive east, or
 *                                {@code null}. Only matters for a birth within
 *                                minutes of the Lập Xuân instant
 * @param facingDirection         the direction of the house, office or room
 *                                being assessed, or {@code null}. Bát Trạch is
 *                                a relation between a person and a direction:
 *                                with no direction there is a Kua profile but
 *                                nothing to judge, so the engine emits the
 *                                profile as evidence and no signals
 */
public record FengShuiKuaInput(
        Instant utcInstant,
        Gender gender,
        VietnameseRegion region,
        Double longitudeDegreesIfKnown,
        CompassDirection facingDirection
) {
    public FengShuiKuaInput {
        Objects.requireNonNull(utcInstant, "utcInstant");
        region = region == null ? VietnameseRegion.UNKNOWN : region;
    }
}
