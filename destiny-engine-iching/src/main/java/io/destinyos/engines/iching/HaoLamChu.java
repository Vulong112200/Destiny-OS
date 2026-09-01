package io.destinyos.engines.iching;

import java.util.Objects;
import java.util.Optional;

/**
 * The governing line of a hexagram (hào làm chủ) under the classical rule
 * 眾以寡為主，多以少為尊 — "chúng dĩ quả vi chủ, đa dĩ thiểu vi tôn".
 *
 * <p>Source: Nguyễn Hiến Lê, <em>Kinh Dịch — Đạo Của Người Quân Tử</em>, NXB
 * Văn Học, tr.101:
 *
 * <blockquote>
 * "Chúng dĩ quả vi chủ, đa dĩ thiểu vi tôn." Nghĩa là cái gì nhiều thì bỏ đi
 * mà lấy cái ít. Theo qui tắc đó, quẻ nào nhiều dương thì lấy âm là chủ;
 * ngược lại thì lấy dương làm chủ.
 * </blockquote>
 *
 * <p>The rule is fully determined by the hexagram's own yin/yang structure —
 * no school choice, no interpretation, no lookup table. That is why it can be
 * computed here at all, and it is the only part of the interpretive layer that
 * this source closes outright.
 *
 * <h2>This says nothing about good or bad — and that is the point</h2>
 *
 * <p>The temptation with a "governing line" is to read it as the auspicious
 * one. The source forecloses that explicitly, tr.102:
 *
 * <blockquote>
 * Tóm lại một hào tốt (hào 4 trong quẻ Lôi địa Dự) làm chủ cả quẻ mà một hào
 * xấu (hào 6 trong quẻ Trạch thiên Quải) cũng có thể làm chủ cả quẻ. Làm chủ
 * chỉ vì nó là số ít trong một đám số nhiều, chứ không phải vì tốt hay xấu.
 * </blockquote>
 *
 * <p>And tr.102–103, closing the section:
 *
 * <blockquote>
 * Vậy thì qui tắc "chúng dĩ quả vi chủ" trong Dịch không có nghĩa là đa số
 * phải phục tùng thiểu số, trái với chế độ dân chủ; mà chỉ có nghĩa là khi xét
 * ý nghĩa của quẻ thì tìm cái nét đặc biệt của quẻ, nét độc đặc đó là một hào
 * dương giữa năm hào âm, hoặc một hào âm giữa năm hào dương, không cần để ý
 * tới hào đó có cao quí hay không, tốt hay xấu.
 * </blockquote>
 *
 * <p>So this class contributes {@code Evidence} only. It must never be turned
 * into a {@code Signal}: doing so would assert a polarity the source spends
 * two paragraphs denying. Polarity comes from {@link CatHungLexicon}, which
 * reads verdicts out of the text instead of inferring them from structure.
 *
 * <h2>The exception the source names, and why the code keeps quiet about it</h2>
 *
 * <p>tr.103:
 *
 * <blockquote>
 * Chúng ta nên để ý: qui tắc "chúng dĩ quả vi chủ" có nhiều lệ ngoại, như quẻ
 * Cấu, hào 1 là hào âm duy nhất mà không phải là hào quan trọng nhất, quyết
 * định ý nghĩa của quẻ.
 * </blockquote>
 *
 * <p>{@link #of} still returns line 1 for quẻ 44 (Cấu / 姤), because that is
 * what the rule says and silently special-casing one hexagram would make the
 * function stop matching its own stated rule. The exception is carried
 * alongside the answer instead — see {@link #EXCEPTION_NOTE_VI} and
 * {@link #isSourceNamedException(int)}, which {@code IChingEngine} attaches to
 * the Evidence. A caveat published next to the result is honest; a caveat
 * compiled into the result as a hidden branch is not.
 *
 * <h2>One kind of governing line, not two (Rule D)</h2>
 *
 * <p>This source recognises a single governing line, determined solely by
 * minority of substance. It does <strong>not</strong> use the two-fold Chu Hi
 * scheme (成卦之主 <em>thành quái chi chủ</em> versus 主卦之主 <em>chủ quái chi
 * chủ</em>), nor Wang Bi's 卦主. Nothing here should be read as implementing
 * the more widespread two-fold concept — that would need its own source and
 * its own declared methodology.
 *
 * @see CatHungLexicon
 * @see IChingEngine
 */
public final class HaoLamChu {

    /** Methodology id — its own, separate from casting, text, and polarity (Rule D). */
    public static final String METHODOLOGY_ID = "ICHING_HAO_LAM_CHU_NGUYENHIENLE";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Hào làm chủ theo qui tắc 眾以寡為主 (chúng dĩ quả vi chủ) — Nguyễn Hiến Lê, "
                    + "\"Kinh Dịch — Đạo Của Người Quân Tử\", NXB Văn Học, tr.101-103";

    /** Shipped on every Evidence so the result cannot be read as a verdict. */
    public static final String NEUTRALITY_NOTE_VI =
            "Hào làm chủ KHÔNG có nghĩa là hào tốt. Nguyên văn tr.102: \"Tóm lại một hào tốt "
                    + "(hào 4 trong quẻ Lôi địa Dự) làm chủ cả quẻ mà một hào xấu (hào 6 trong "
                    + "quẻ Trạch thiên Quải) cũng có thể làm chủ cả quẻ. Làm chủ chỉ vì nó là số "
                    + "ít trong một đám số nhiều, chứ không phải vì tốt hay xấu.\" Vì vậy mục này "
                    + "chỉ là Evidence và không phát sinh Signal cát/hung nào.";

    /** The exception the source itself names, carried with the answer rather than hidden in it. */
    public static final String EXCEPTION_NOTE_VI =
            "Chính sách nguồn đã bác ca này. Nguyên văn tr.103: \"qui tắc 'chúng dĩ quả vi chủ' "
                    + "có nhiều lệ ngoại, như quẻ Cấu, hào 1 là hào âm duy nhất mà không phải là "
                    + "hào quan trọng nhất, quyết định ý nghĩa của quẻ.\" Hàm tính vẫn trả về hào "
                    + "1 vì đó đúng là điều qui tắc phát biểu; ghi chú này đi kèm để không ai đọc "
                    + "kết quả như một kết luận của sách.";

    /** Quẻ 44 Cấu (姤) — the one hexagram the source names as an exception. */
    private static final int CAU = 44;

    private HaoLamChu() {
    }

    /**
     * The governing line's position (1-6, bottom-up), or empty when the rule
     * does not single one out.
     *
     * <p>Empty is the common case and a real answer, not a failure: the rule
     * only speaks when exactly one line stands alone against the other five.
     * A 4-2, 3-3, or 2-4 split has no minority of one, so there is no
     * "nét độc đặc" to point at, and inventing one would be a guess.
     *
     * <p>Pure Kiền (6 dương) and pure Khôn (6 âm) also return empty — there is
     * no minority at all, matching tr.102's own aside that the rule is stated
     * "không kể hai quẻ càn, khôn".
     */
    public static Optional<Integer> of(Hexagram hexagram) {
        Objects.requireNonNull(hexagram, "hexagram");
        boolean[] yang = lines(hexagram);
        int yangCount = 0;
        for (boolean isYang : yang) {
            if (isYang) {
                yangCount++;
            }
        }
        int yinCount = 6 - yangCount;
        if (yangCount != 1 && yinCount != 1) {
            return Optional.empty();
        }
        boolean minorityIsYang = yangCount == 1;
        for (int i = 0; i < 6; i++) {
            if (yang[i] == minorityIsYang) {
                return Optional.of(i + 1);
            }
        }
        throw new IllegalStateException("Unreachable: a minority of one was counted but not found");
    }

    /** True if the governing line is yang (dương), given a position from {@link #of}. */
    public static boolean isYang(Hexagram hexagram, int position1to6) {
        Objects.requireNonNull(hexagram, "hexagram");
        if (position1to6 < 1 || position1to6 > 6) {
            throw new IllegalArgumentException("Line position must be 1-6, got " + position1to6);
        }
        return lines(hexagram)[position1to6 - 1];
    }

    /**
     * True for the one hexagram the source names as an exception to the rule
     * (quẻ 44 Cấu). Lets the caller attach {@link #EXCEPTION_NOTE_VI} without
     * changing what {@link #of} returns.
     */
    public static boolean isSourceNamedException(int kingWenNumber) {
        return kingWenNumber == CAU;
    }

    /** Lines 1-6 bottom-up: lower trigram bottom→top, then upper trigram bottom→top. */
    private static boolean[] lines(Hexagram hexagram) {
        return new boolean[] {
            hexagram.lower().bottomYang(), hexagram.lower().middleYang(), hexagram.lower().topYang(),
            hexagram.upper().bottomYang(), hexagram.upper().middleYang(), hexagram.upper().topYang(),
        };
    }
}
