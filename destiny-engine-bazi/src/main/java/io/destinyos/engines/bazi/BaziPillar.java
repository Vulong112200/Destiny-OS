package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.HiddenStems;
import io.destinyos.calendar.YinYang;
import java.util.List;
import java.util.Objects;

/**
 * One of the Tứ Trụ, with everything about it that is derivable without
 * interpretation.
 *
 * <p>{@code stemTenGod} and {@code hiddenStemTenGods} are {@code null}/empty
 * when the Day Master is unknown — which happens whenever birth time
 * precision does not support a day pillar (Master Spec §2). Thập Thần is
 * defined relative to the Day Master, so with no Day Master there is no Thập
 * Thần, and inventing one from the year stem instead would be a different
 * methodology wearing the same label.
 *
 * @param position          which pillar this is
 * @param stem              Thiên Can
 * @param branch            Địa Chi
 * @param hiddenStems       Tàng Can of {@code branch}
 * @param stemTenGod        Thập Thần of {@code stem} relative to the Day
 *                          Master; {@code null} for the day pillar itself
 *                          (the Day Master has no role relative to itself)
 *                          and when the Day Master is unknown
 * @param hiddenStemTenGods Thập Thần of each hidden stem, same order as
 *                          {@code HiddenStems.HiddenStemSet#all()}; empty
 *                          when the Day Master is unknown
 */
public record BaziPillar(
        PillarPosition position,
        HeavenlyStem stem,
        EarthlyBranch branch,
        HiddenStems.HiddenStemSet hiddenStems,
        TenGod stemTenGod,
        List<TenGod> hiddenStemTenGods
) {
    public BaziPillar {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(stem, "stem");
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(hiddenStems, "hiddenStems");
        hiddenStemTenGods = hiddenStemTenGods == null ? List.of() : List.copyOf(hiddenStemTenGods);
    }

    public FiveElement stemElement() {
        return stem.element();
    }

    public FiveElement branchElement() {
        return branch.element();
    }

    public YinYang stemPolarity() {
        return stem.polarity();
    }
}
