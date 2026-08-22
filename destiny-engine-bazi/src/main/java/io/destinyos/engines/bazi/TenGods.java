package io.destinyos.engines.bazi;

import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import java.util.Objects;

/**
 * Derives the {@link TenGod} of a stem relative to the Day Master.
 *
 * <p>Total function over all 100 (Day Master, target) stem pairs, and
 * deliberately implemented as the rule rather than as a 10x10 lookup table:
 * a transcribed table can contain a typo that no reader will ever notice,
 * whereas the rule below is the same sentence the cited sources state, and a
 * test asserts every one of the 100 pairs resolves and that each of the ten
 * roles is reachable.
 *
 * <p>Derived from <em>stems only</em>, never from branches — the same choice
 * every source consulted makes, and the reason {@link io.destinyos.calendar.YinYang}'s
 * "Tý is functionally Âm" subtlety is unreachable here.
 */
final class TenGods {

    private TenGods() {
    }

    static TenGod of(HeavenlyStem dayMaster, HeavenlyStem target) {
        Objects.requireNonNull(dayMaster, "dayMaster");
        Objects.requireNonNull(target, "target");

        boolean samePolarity = dayMaster.polarity() == target.polarity();
        FiveElement.ElementRelation relation = dayMaster.element().relationTo(target.element());

        return switch (relation) {
            case SAME        -> samePolarity ? TenGod.TY_KIEN     : TenGod.KIEP_TAI;
            case I_GENERATE  -> samePolarity ? TenGod.THUC_THAN   : TenGod.THUONG_QUAN;
            case I_CONTROL   -> samePolarity ? TenGod.THIEN_TAI   : TenGod.CHINH_TAI;
            case CONTROLS_ME -> samePolarity ? TenGod.THAT_SAT    : TenGod.CHINH_QUAN;
            case GENERATES_ME -> samePolarity ? TenGod.THIEN_AN   : TenGod.CHINH_AN;
        };
    }
}
