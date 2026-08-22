package io.destinyos.engines.fengshui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Derives the Bát Trạch relation between a life trigram and a direction from
 * the Bát Biến Du Niên line-change rule.
 *
 * <p><strong>Derived, not transcribed.</strong> The usual way to implement this
 * is to copy an 8×8 table of 64 cells. That was tried first here and rejected:
 * a copied table can contain a cell nobody ever reads again, and the only
 * complete English-language table found turned out to contain four such cells
 * (see below). The rule below has eight cases instead of sixty-four, each
 * checkable against the sources, and a table generated from it can be tested
 * against published tables rather than merely trusted.
 *
 * <p><strong>The rule.</strong> Compare the three lines of the life trigram
 * with those of the direction's trigram; the relation depends only on
 * <em>which</em> lines differ:
 *
 * <table>
 *   <caption>Bát Biến Du Niên</caption>
 *   <tr><th>Lines that differ</th><th>Relation</th></tr>
 *   <tr><td>none</td><td>Phục Vị (伏位)</td></tr>
 *   <tr><td>top only</td><td>Sinh Khí (生氣)</td></tr>
 *   <tr><td>middle only</td><td>Tuyệt Mệnh (絕命)</td></tr>
 *   <tr><td>bottom only</td><td>Hoạ Hại (禍害)</td></tr>
 *   <tr><td>top + middle</td><td>Ngũ Quỷ (五鬼)</td></tr>
 *   <tr><td>top + bottom</td><td>Lục Sát (六煞)</td></tr>
 *   <tr><td>middle + bottom</td><td>Thiên Y (天醫)</td></tr>
 *   <tr><td>all three</td><td>Diên Niên (延年)</td></tr>
 * </table>
 *
 * <p><strong>Sources</strong> (both retrieved 2026-08-22):
 * <ul>
 *   <li>Chinese: the 八宅歌訣 line-change mnemonic — "一爻不变" → 伏位,
 *       "三爻全变" → 延年, "只上爻变" → 生气, "中下爻变" → 天医,
 *       "上中爻变" → 五鬼, "上下爻变" → 六煞, "只中爻变" → 绝命 (with
 *       bottom-only → 祸害 by elimination), reported from
 *       {@code blog.sina.com.cn/s/blog_50ba879a0100e5py.html} and
 *       {@code zhuanlan.zhihu.com/p/108686392}.</li>
 *   <li>Vietnamese: "Bát Biến Du Niên" ({@code lyhocphuongdong.vn}), which
 *       states the Tuyệt Mệnh pairs explicitly as Càn–Ly, Khôn–Khảm,
 *       <strong>Cấn–Tốn, Đoài–Chấn</strong>, that Lục Sát changes the upper and
 *       lower lines, and that Hoạ Hại changes the lower line.</li>
 * </ul>
 *
 * <p><strong>Verification against published tables.</strong> The rule was not
 * taken on trust; it was tested as a hypothesis against tables nobody here
 * produced:
 * <ul>
 *   <li>{@code masterseanchan.com}'s complete 8×8 matrix: <strong>60 of 64
 *       cells agree.</strong> The four that do not are a Lục Sát ↔ Tuyệt Mệnh
 *       swap confined to its Đoài and Cấn rows.</li>
 *   <li>{@code nguyenthehoa.com}'s per-cung page for Cấn: <strong>8 of 8
 *       agree</strong> — including both disputed cells, where it gives Tuyệt
 *       Mệnh at Đông Nam and Lục Sát at Đông, against the English table and
 *       <em>with</em> this rule.</li>
 *   <li>{@code phongthuykhaitoan.com}'s per-cung page for Chấn: agrees on every
 *       direction it states unambiguously.</li>
 * </ul>
 *
 * <p>Three independent arguments identify those four cells as that one source's
 * error rather than a rival school: the Vietnamese source contradicts them
 * directly; they break the symmetry {@code rel(A, dir(B)) == rel(B, dir(A))}
 * that a line-difference rule guarantees by construction, since XOR is
 * symmetric; and within the English table itself the two affected line-change
 * patterns split 6 cells to 2 in favour of this rule. All three checks are
 * asserted in {@code BatTrachTableTest}.
 */
public final class BatTrachTable {

    private BatTrachTable() {
    }

    /**
     * The relation a direction has for someone whose life trigram is
     * {@code lifeTrigram}.
     *
     * <p>Symmetric by construction — the three booleans are differences, and a
     * difference does not care which side it is computed from. That is a real
     * property of the tradition rather than an implementation convenience: a
     * table that is asymmetric here has an error in it.
     */
    public static BatTrachRelation relation(Trigram lifeTrigram, Trigram directionTrigram) {
        Objects.requireNonNull(lifeTrigram, "lifeTrigram");
        Objects.requireNonNull(directionTrigram, "directionTrigram");

        boolean bottom = lifeTrigram.bottomYang() != directionTrigram.bottomYang();
        boolean middle = lifeTrigram.middleYang() != directionTrigram.middleYang();
        boolean top = lifeTrigram.topYang() != directionTrigram.topYang();

        if (!bottom && !middle && !top) {
            return BatTrachRelation.PHUC_VI;
        }
        if (top && !middle && !bottom) {
            return BatTrachRelation.SINH_KHI;
        }
        if (middle && !top && !bottom) {
            return BatTrachRelation.TUYET_MENH;
        }
        if (bottom && !top && !middle) {
            return BatTrachRelation.HOA_HAI;
        }
        if (top && middle && !bottom) {
            return BatTrachRelation.NGU_QUY;
        }
        if (top && bottom && !middle) {
            return BatTrachRelation.LUC_SAT;
        }
        if (middle && bottom && !top) {
            return BatTrachRelation.THIEN_Y;
        }
        return BatTrachRelation.DIEN_NIEN;
    }

    /** Same, keyed by compass direction. */
    public static BatTrachRelation relation(Trigram lifeTrigram, CompassDirection direction) {
        return relation(lifeTrigram, Trigram.ofDirection(direction));
    }

    /**
     * All eight directions for one life trigram, in compass order.
     *
     * <p>Compass order rather than best-to-worst on purpose: a caller ranking
     * them is making a presentation decision, and a map that arrives
     * pre-sorted by desirability quietly makes that decision for them.
     */
    public static Map<CompassDirection, BatTrachRelation> allDirections(Trigram lifeTrigram) {
        Objects.requireNonNull(lifeTrigram, "lifeTrigram");
        Map<CompassDirection, BatTrachRelation> map = new EnumMap<>(CompassDirection.class);
        for (CompassDirection direction : CompassDirection.values()) {
            map.put(direction, relation(lifeTrigram, direction));
        }
        return Map.copyOf(map);
    }
}
