package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.destinyos.calendar.EarthlyBranch.*;
import static io.destinyos.calendar.HeavenlyStem.*;

/**
 * The 30-degree-per-branch table ("BẢNG ĐỘ VƯỢNG NGŨ HÀNH CỦA CÁC ĐỊA CHI
 * TÀNG NHÂN NGUYÊN", Thiệu Vĩ Hoa & Trần Viên, p.342), each branch split
 * bản khí / tạp khí 1 / tạp khí 2.
 *
 * <p><strong>Thân's role ordering follows the book (Mậu 9° / Nhâm 3°), a
 * declared Rule D choice</strong> (`docs/DECISION_LOG.md`, 2026-08-24) —
 * `destiny-calendar`'s own {@code HiddenStems} records the *set* the same
 * way but leaves this exact branch's two minor stems in the reverse order
 * from two independent web sources, and is not changed by this decision.
 * Sửu and Tỵ, where `HiddenStems` already flags the ordering as disputed
 * between its two sources, are recorded here exactly as the book resolves
 * them (siding with the English source in both cases — verified in
 * `docs/research_drafts/VERIFICATION_OPUS_R3.md` §A1).
 */
final class BranchDegreeTable {

    private BranchDegreeTable() {
    }

    /** Bản khí is always the first entry. Degrees sum to 30 for every branch. */
    private static final Map<EarthlyBranch, Map<HeavenlyStem, Integer>> TABLE = build();

    static Map<HeavenlyStem, Integer> of(EarthlyBranch branch) {
        return TABLE.get(branch);
    }

    /** The bản khí (principal) stem and its degree — always the highest-weighted entry. */
    static HeavenlyStem principal(EarthlyBranch branch) {
        return TABLE.get(branch).keySet().iterator().next();
    }

    private static Map<EarthlyBranch, Map<HeavenlyStem, Integer>> build() {
        Map<EarthlyBranch, Map<HeavenlyStem, Integer>> t = new LinkedHashMap<>();
        t.put(RAT, entries(QUY, 30));
        t.put(OX, entries(KY, 18, QUY, 9, TAN, 3));
        t.put(TIGER, entries(GIAP, 18, BINH, 9, MAU, 3));
        t.put(RABBIT, entries(AT, 30));
        t.put(DRAGON, entries(MAU, 18, AT, 9, QUY, 3));
        t.put(SNAKE, entries(BINH, 18, CANH, 9, MAU, 3));
        t.put(HORSE, entries(DINH, 21, KY, 9));
        t.put(GOAT, entries(KY, 18, DINH, 9, AT, 3));
        // Than: book's order is Canh (ban khi) 18, Mau 9, Nham 3 - decided
        // over destiny-calendar's HiddenStems agreed (undisputed) ordering
        // of Nham/Mau. See class Javadoc and DECISION_LOG.md.
        t.put(MONKEY, entries(CANH, 18, MAU, 9, NHAM, 3));
        t.put(ROOSTER, entries(TAN, 30));
        t.put(DOG, entries(MAU, 18, TAN, 9, DINH, 3));
        t.put(PIG, entries(NHAM, 21, GIAP, 9));
        return Map.copyOf(t);
    }

    /**
     * Builds an order-preserving map: {@link #principal} depends on the
     * <strong>first</strong> pair being the bản khí. {@code Map.copyOf}
     * deliberately does not guarantee iteration order (it's backed by a
     * probed hash table for maps of this size) — wrapping the
     * {@code LinkedHashMap} with {@link Collections#unmodifiableMap} instead
     * keeps insertion order intact.
     */
    private static Map<HeavenlyStem, Integer> entries(Object... pairs) {
        Map<HeavenlyStem, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((HeavenlyStem) pairs[i], (Integer) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }

    /** Convenience: every element present in a branch's full (non-khử-bì) table, deduplicated. */
    static Map<FiveElement, Integer> elementDegrees(EarthlyBranch branch) {
        Map<FiveElement, Integer> byElement = new LinkedHashMap<>();
        of(branch).forEach((stem, degree) ->
                byElement.merge(stem.element(), degree, Integer::sum));
        return byElement;
    }
}
