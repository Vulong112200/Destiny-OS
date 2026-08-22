package io.destinyos.calendar;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tàng Can — the Heavenly Stems concealed inside each Earthly Branch.
 *
 * <p><strong>Sources (two, independent, cross-checked):</strong>
 * <ul>
 *   <li>Vietnamese: 4T Human, "Can Tàng — Thiên can ẩn tàng trong địa chi"
 *       (4thuman.com/huyen-hoc-phuong-dong/bat-tu/can-tang/), retrieved
 *       2026-08-22.</li>
 *   <li>English/Chinese: Imperial Harvest, "Hidden Heavenly Stems (藏干) in
 *       Earthly Branches" (imperialharvest.com/blog/hidden-heavenly-stems/),
 *       retrieved 2026-08-22.</li>
 * </ul>
 *
 * <p><strong>What the two sources agree on, and what they do not.</strong>
 * The <em>set</em> of hidden stems is identical for all twelve branches in
 * both sources, and both name the same 主氣/chính khí (principal stem) for
 * all twelve. They <em>disagree</em> on which of the two remaining stems is
 * 中氣 (central) and which is 餘氣 (residual) for exactly two branches:
 * <ul>
 *   <li>Sửu — VN source orders Kỷ, Tân, Quý; EN source orders Ji, Gui, Xin.</li>
 *   <li>Tỵ — VN source orders Bính, Mậu, Canh; EN source orders Bing, Geng, Wu.</li>
 * </ul>
 *
 * <p>This class therefore models only what both sources establish: the
 * principal stem, named explicitly, plus the remaining stems as an
 * <em>unordered</em> set with no central/residual role assigned. Picking one
 * source's ordering for those two branches would be silently selecting a
 * school (CLAUDE.md Rule D) to no purpose, because nothing in Phase 8a
 * depends on the distinction — and the 60/30/10 percentages that both
 * sources attach to the roles are exactly the fabricated numeric weighting
 * ADR D6 forbids, so they are not imported either. If a later phase needs
 * the role ordering, it needs a resolved research item first, not a guess
 * made here.
 */
public final class HiddenStems {

    private static final Map<EarthlyBranch, HiddenStemSet> TABLE = build();

    private HiddenStems() {
    }

    /** Never empty: every branch conceals at least its own principal stem. */
    public static HiddenStemSet of(EarthlyBranch branch) {
        return TABLE.get(Objects.requireNonNull(branch, "branch"));
    }

    /**
     * @param principal            the 主氣/chính khí — the stem sharing the
     *                             branch's own element. Both cited sources
     *                             agree for all twelve branches
     * @param additional           the remaining hidden stems, listed in the
     *                             Vietnamese source's order but carrying
     *                             <em>no</em> central/residual role claim
     *                             (see this class's Javadoc); empty for Tý,
     *                             Mão and Dậu
     * @param roleOrderingDisputed whether the two cited sources disagree on
     *                             which additional stem is central and which
     *                             is residual. True for Sửu and Tỵ only. Any
     *                             future caller that wants to display roles
     *                             must surface this rather than pick a side
     */
    public record HiddenStemSet(HeavenlyStem principal, List<HeavenlyStem> additional,
                                boolean roleOrderingDisputed) {
        public HiddenStemSet {
            Objects.requireNonNull(principal, "principal");
            additional = additional == null ? List.of() : List.copyOf(additional);
        }

        /** Principal first, then the additional stems. */
        public List<HeavenlyStem> all() {
            var combined = new java.util.ArrayList<HeavenlyStem>(1 + additional.size());
            combined.add(principal);
            combined.addAll(additional);
            return List.copyOf(combined);
        }
    }

    private static Map<EarthlyBranch, HiddenStemSet> build() {
        Map<EarthlyBranch, HiddenStemSet> map = new EnumMap<>(EarthlyBranch.class);

        // Tý (Thủy) - Quý only.
        map.put(EarthlyBranch.RAT, agreed(HeavenlyStem.QUY));
        // Sửu (Thổ) - Kỷ chính; Tân and Quý additional. VN source orders
        // Tân then Quý, EN source orders Quý then Tân - role ordering disputed.
        map.put(EarthlyBranch.OX, disputed(HeavenlyStem.KY, HeavenlyStem.TAN, HeavenlyStem.QUY));
        // Dần (Mộc) - Giáp chính; Bính, Mậu.
        map.put(EarthlyBranch.TIGER, agreed(HeavenlyStem.GIAP, HeavenlyStem.BINH, HeavenlyStem.MAU));
        // Mão (Mộc) - Ất only.
        map.put(EarthlyBranch.RABBIT, agreed(HeavenlyStem.AT));
        // Thìn (Thổ) - Mậu chính; Ất, Quý.
        map.put(EarthlyBranch.DRAGON, agreed(HeavenlyStem.MAU, HeavenlyStem.AT, HeavenlyStem.QUY));
        // Tỵ (Hỏa) - Bính chính; Mậu, Canh. VN source orders Mậu then Canh,
        // EN source orders Canh then Mậu - role ordering disputed.
        map.put(EarthlyBranch.SNAKE, disputed(HeavenlyStem.BINH, HeavenlyStem.MAU, HeavenlyStem.CANH));
        // Ngọ (Hỏa) - Đinh chính; Kỷ.
        map.put(EarthlyBranch.HORSE, agreed(HeavenlyStem.DINH, HeavenlyStem.KY));
        // Mùi (Thổ) - Kỷ chính; Đinh, Ất.
        map.put(EarthlyBranch.GOAT, agreed(HeavenlyStem.KY, HeavenlyStem.DINH, HeavenlyStem.AT));
        // Thân (Kim) - Canh chính; Nhâm, Mậu.
        map.put(EarthlyBranch.MONKEY, agreed(HeavenlyStem.CANH, HeavenlyStem.NHAM, HeavenlyStem.MAU));
        // Dậu (Kim) - Tân only.
        map.put(EarthlyBranch.ROOSTER, agreed(HeavenlyStem.TAN));
        // Tuất (Thổ) - Mậu chính; Tân, Đinh.
        map.put(EarthlyBranch.DOG, agreed(HeavenlyStem.MAU, HeavenlyStem.TAN, HeavenlyStem.DINH));
        // Hợi (Thủy) - Nhâm chính; Giáp.
        map.put(EarthlyBranch.PIG, agreed(HeavenlyStem.NHAM, HeavenlyStem.GIAP));

        return Map.copyOf(map);
    }

    private static HiddenStemSet agreed(HeavenlyStem principal, HeavenlyStem... additional) {
        return new HiddenStemSet(principal, List.of(additional), false);
    }

    private static HiddenStemSet disputed(HeavenlyStem principal, HeavenlyStem... additional) {
        return new HiddenStemSet(principal, List.of(additional), true);
    }
}
