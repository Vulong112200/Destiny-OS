package io.destinyos.engines.iching;

import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads cát/hung polarity out of the judgment vocabulary that is physically
 * present in a 卦辭/爻辭's Chinese text — the resolution of R24's
 * {@code CAT_HUNG_POLARITY} blocked section, and the reason
 * {@link IChingEngine} can finally emit {@code Signal}s at all.
 *
 * <h2>Why this is a reading and not a formula (Rule A / Rule C)</h2>
 *
 * <p>The characters 吉 / 凶 / 悔 / 吝 / 无咎 are <em>in the data already</em>.
 * The Chinese text came from {@code zh.wikisource.org} raw wikitext and is
 * codepoint-checked by {@code HexagramJudgmentTableTest.Derived
 * .chineseUsesProperCodepoints}, so scanning it for these characters reads
 * what the source says rather than inventing a score. That is the whole
 * design constraint: every polarity this class returns can be traced back to
 * a specific character at a specific offset in a specific line of text, and
 * {@link Match#term()} plus {@link Match#position()} carry exactly that so
 * the Evidence stays auditable.
 *
 * <h2>Why the position-based route was rejected</h2>
 *
 * <p>The obvious alternative — derive polarity from đắc trung (hào 2, 5) and
 * đắc chính (dương at an odd position, âm at an even one) — was investigated
 * against Nguyễn Hiến Lê, <em>Kinh Dịch — Đạo Của Người Quân Tử</em> (NXB Văn
 * Học) and <strong>rejected because that book disowns it</strong>. Page 101:
 * <em>"Một lần nữa, trong Dịch, không có qui tắc gì luôn luôn đúng, có rất
 * nhiều lệ ngoại, phải tùy thời mà xét."</em> The same page hands over the
 * counterexamples itself — two lines that are both bất chính yet read well,
 * and another pair that are both chính yet read badly. A rule its own source
 * publishes counterexamples to is precisely the "formula that looks right"
 * Rule C forbids.
 *
 * <h2>Where the five valences come from</h2>
 *
 * <p>Nguyễn Hiến Lê, page 92, glosses exactly five judgment terms, and this
 * table is those five, verbatim:
 *
 * <ul>
 *   <li>吉 <em>cát</em> — "nghĩa là tốt lành"</li>
 *   <li>凶 <em>hung</em> — "ngược lại với cát, xấu nhất"</li>
 *   <li>悔 <em>hối</em> — "là lỗi, ăn năn"</li>
 *   <li>吝 <em>lận</em> — "là lỗi nhỏ, tiếc"</li>
 *   <li>无咎 <em>vô cữu</em> — "là không có lỗi hoặc lỗi không về ai cả"</li>
 * </ul>
 *
 * <p>Two things fall straight out of his wording and are not this project's
 * invention: 凶 sits at the <em>bottom</em> of the scale ("xấu nhất"), which
 * is why it alone gets {@link Strength#STRONG}; and 吝 is a <em>smaller</em>
 * fault than 悔 ("lỗi nhỏ"), which is why the two differ in strength rather
 * than being flattened together.
 *
 * <h2>What is deliberately NOT in this table, and why (Rule D)</h2>
 *
 * <p>Everything excluded here is excluded because a source declined to
 * support it, not because it was overlooked:
 *
 * <ul>
 *   <li><strong>貞 trinh, 亨 hanh, 利 lợi, 元 nguyên (tứ đức).</strong> Nguyễn
 *       Hiến Lê keeps these in a separate section (pages 90–92) from the
 *       cát/hung glossary, and page 173 says why: <em>"Trinh là chính và
 *       bền"</em> — a virtue held <em>conditionally</em>, not a verdict. He
 *       also lists <strong>five competing readings</strong> of tứ đức (a–đ)
 *       and declares he follows reading (a), where reading (đ) — Cao Hanh's,
 *       taking 亨 as 享 (a sacrificial offering) and 利貞 as 利占 — negates it
 *       outright. Scoring a term whose own source offers five incompatible
 *       readings would be silently choosing a school. So 貞 alone scores nothing;
 *       only the compound 貞吉 does, and it scores as the 吉 it contains.</li>
 *   <li><strong>孚 phu.</strong> Means "tin" (trust). Not a verdict at all,
 *       and not glossed as one anywhere in the source.</li>
 *   <li><strong>厲 lệ.</strong> Genuinely reads as danger, but no consulted
 *       source glosses it, so it stays out rather than being guessed in.</li>
 * </ul>
 *
 * <h2>The ordering bug this class exists to avoid</h2>
 *
 * <p>Compounds are matched <strong>before</strong> the single characters they
 * contain, and this is load-bearing, not tidiness. Measured over the shipped
 * 450 entries: <strong>92 carry 无咎/無咎</strong> against only
 * <strong>7 carrying a bare 咎</strong>. A naive scan that saw 咎 first would
 * therefore report 92 entries as faulty when their text says the opposite —
 * "không có lỗi". 悔亡 (hối vong, the regret <em>disappears</em>) inverts 悔
 * the same way, in 19 entries.
 *
 * <p>Both 无 and 無 are accepted. R24 §C1 recorded that the wikisource
 * edition writes 无; the shipped data also contains one 無咎 and one 無悔, so
 * handling only one form would drop real matches.
 *
 * <h2>Multiple verdicts in one line are not averaged (Rule E)</h2>
 *
 * <p>28 of the 450 entries carry both a favourable and an unfavourable term.
 * {@link #scan} returns <em>all</em> of them and never reduces them to a
 * single polarity; {@code IChingEngine} turns each into its own Signal and
 * lets the existing consensus/conflict machinery see the disagreement. A
 * hexagram text that says both "tốt" and "hung" is a CONFLICT, which this
 * project treats as a valid result rather than something to smooth away.
 *
 * @see IChingEngine
 */
public final class CatHungLexicon {

    /** Methodology id — separate from the casting and text methodologies (Rule D). */
    public static final String METHODOLOGY_ID = "ICHING_CAT_HUNG_LEXICAL";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Từ vựng phán định cổ văn (吉/凶/悔/吝/无咎), nghĩa theo Nguyễn Hiến Lê — "
                    + "\"Kinh Dịch — Đạo Của Người Quân Tử\", NXB Văn Học, tr.92";
    public static final String SOURCE =
            "Cực tính được ĐỌC ra từ chính chữ Hán đã ship (nguồn zh.wikisource, đã qua "
                    + "kiểm codepoint CJK), không phải tính bằng công thức. Nghĩa của 5 chữ "
                    + "phán định lấy nguyên văn từ bảng thuật ngữ của Nguyễn Hiến Lê tr.92: "
                    + "cát = tốt lành; hung = ngược lại với cát, xấu nhất; hối = lỗi, ăn năn; "
                    + "lận = lỗi nhỏ, tiếc; vô cữu = không có lỗi hoặc lỗi không về ai cả. "
                    + "Thang mạnh/yếu suy trực tiếp từ lời ông: hung là \"xấu nhất\" nên STRONG, "
                    + "lận là \"lỗi nhỏ\" so với hối nên nhẹ hơn. Các dạng ghép (元吉, 大吉, "
                    + "終吉, 貞吉, 悔亡, 无悔, 終凶, 征凶) là suy dẫn của dự án từ chính 5 chữ "
                    + "đó, KHÔNG phải trích dẫn trực tiếp — khai báo tường minh theo Rule D. "
                    + "Tứ đức (元/亨/利/貞) CỐ Ý không tính cực tính: tr.173 định nghĩa trinh là "
                    + "\"chính và bền\" tức đức tính có điều kiện, và tr.90-92 nêu 5 cách đọc "
                    + "tứ đức cạnh tranh nhau (a-đ) trong đó cách đ của Cao Hanh phủ định cách "
                    + "a mà chính tác giả chọn. 孚 (tin) và 厲 (lệ) cũng không tính: một chữ "
                    + "không phải phán định, một chữ không nguồn nào tra nghĩa. "
                    + "Đường suy cát/hung theo VỊ HÀO (đắc trung / đắc chính) đã bị BÁC vì "
                    + "chính tr.101 tự phủ định: \"trong Dịch, không có qui tắc gì luôn luôn "
                    + "đúng, có rất nhiều lệ ngoại, phải tùy thời mà xét\", và nêu luôn phản "
                    + "ví dụ. Chi tiết: docs/research_drafts/R24_iching_hexagram_judgments.md.";

    private CatHungLexicon() {
    }

    /**
     * One judgment term found in a text, kept together with where it was
     * found so the Evidence can be audited back to the character.
     *
     * @param term       the matched Chinese term, exactly as it appears
     * @param code        stable ASCII id, used to build Signal tags
     * @param hanViet    its Hán-Việt reading
     * @param position   0-based character offset in the scanned text
     * @param polarity   the verdict this term carries
     * @param strength   how strongly, per the source's own wording
     * @param glossVi    the Vietnamese gloss — verbatim from the source for
     *                   the five glossed terms, this project's wording for
     *                   the declared compounds
     * @param fromGloss  true if the source glosses this term directly; false
     *                   if it is this project's declared derivation (Rule D)
     */
    public record Match(
            String term,
            String code,
            String hanViet,
            int position,
            Polarity polarity,
            Strength strength,
            String glossVi,
            boolean fromGloss
    ) {
        public Match {
            Objects.requireNonNull(term, "term");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(hanViet, "hanViet");
            Objects.requireNonNull(polarity, "polarity");
            Objects.requireNonNull(strength, "strength");
            Objects.requireNonNull(glossVi, "glossVi");
        }
    }

    /** A term the lexicon knows, before it is located in any particular text. */
    private record Term(String chinese, String code, String hanViet, Polarity polarity,
                        Strength strength, String glossVi, boolean fromGloss) {
    }

    /**
     * The table, <strong>ordered longest-first</strong>. {@link #scan} relies
     * on this order: a compound claims its characters before any shorter term
     * can, which is what keeps 无咎 from being read as 咎. Do not reorder.
     */
    private static final List<Term> TERMS = List.of(
            // ---- Compounds. Project derivation from the five glossed terms (Rule D). ----
            new Term("元吉", "NGUYEN_CAT", "nguyên cát", Polarity.SUPPORT, Strength.STRONG,
                    "Cát lớn — 元 là \"đầu tiên, lớn, trùm mọi điều thiện\" (tr.90)", false),
            new Term("大吉", "DAI_CAT", "đại cát", Polarity.SUPPORT, Strength.STRONG,
                    "Rất tốt lành", false),
            new Term("中吉", "TRUNG_CAT", "trung cát", Polarity.SUPPORT, Strength.MEDIUM,
                    "Tốt lành ở mức vừa", false),
            new Term("終吉", "CHUNG_CAT", "chung cát", Polarity.SUPPORT, Strength.MEDIUM,
                    "Về sau thì tốt lành", false),
            new Term("貞吉", "TRINH_CAT", "trinh cát", Polarity.SUPPORT, Strength.MEDIUM,
                    "Tốt lành NẾU giữ được chính và bền — 貞 là điều kiện, không phải "
                            + "phán định (tr.173)", false),
            new Term("小吉", "TIEU_CAT", "tiểu cát", Polarity.SUPPORT, Strength.WEAK,
                    "Tốt lành nhỏ", false),
            new Term("終凶", "CHUNG_HUNG", "chung hung", Polarity.NEGATIVE, Strength.STRONG,
                    "Về sau thì xấu", false),
            new Term("征凶", "CHINH_HUNG", "chinh hung", Polarity.NEGATIVE, Strength.STRONG,
                    "Đi (tiến hành) thì xấu", false),
            new Term("无咎", "VO_CUU", "vô cữu", Polarity.SUPPORT, Strength.WEAK,
                    "Không có lỗi hoặc lỗi không về ai cả (tr.92)", true),
            new Term("無咎", "VO_CUU", "vô cữu", Polarity.SUPPORT, Strength.WEAK,
                    "Không có lỗi hoặc lỗi không về ai cả (tr.92)", true),
            new Term("无悔", "VO_HOI", "vô hối", Polarity.SUPPORT, Strength.WEAK,
                    "Không phải ăn năn", false),
            new Term("無悔", "VO_HOI", "vô hối", Polarity.SUPPORT, Strength.WEAK,
                    "Không phải ăn năn", false),
            new Term("悔亡", "HOI_VONG", "hối vong", Polarity.SUPPORT, Strength.WEAK,
                    "Sự ăn năn mất đi — ngược hẳn với 悔 đứng một mình", false),

            // ---- The five terms Nguyễn Hiến Lê glosses at tr.92, verbatim. ----
            new Term("吉", "CAT", "cát", Polarity.SUPPORT, Strength.MEDIUM,
                    "Nghĩa là tốt lành (tr.92)", true),
            new Term("凶", "HUNG", "hung", Polarity.NEGATIVE, Strength.STRONG,
                    "Ngược lại với cát, xấu nhất (tr.92)", true),
            new Term("悔", "HOI", "hối", Polarity.CAUTION, Strength.MEDIUM,
                    "Là lỗi, ăn năn (tr.92)", true),
            new Term("吝", "LAN", "lận", Polarity.CAUTION, Strength.WEAK,
                    "Là lỗi nhỏ, tiếc (tr.92)", true),
            new Term("咎", "CUU", "cữu", Polarity.CAUTION, Strength.MEDIUM,
                    "Lỗi — suy trực tiếp từ chính lời tra \"vô cữu là KHÔNG CÓ lỗi\" (tr.92)",
                    false)
    );

    /**
     * Every judgment term in {@code hanTu}, ordered by where it appears.
     *
     * <p>Characters are claimed by at most one match, longest term first, so
     * a text containing 无咎 yields one 无咎 and no bare 咎. An empty result
     * means the text carries no verdict this lexicon recognises — a real
     * finding (35% of the shipped 450 entries), reported as
     * {@link Polarity#NEUTRAL} rather than guessed at.
     *
     * @param hanTu the Chinese judgment text; null or blank yields an empty list
     */
    public static List<Match> scan(String hanTu) {
        if (hanTu == null || hanTu.isBlank()) {
            return List.of();
        }
        boolean[] claimed = new boolean[hanTu.length()];
        List<Match> matches = new ArrayList<>();
        for (Term term : TERMS) {
            int from = 0;
            while (true) {
                int at = hanTu.indexOf(term.chinese(), from);
                if (at < 0) {
                    break;
                }
                if (unclaimed(claimed, at, term.chinese().length())) {
                    claim(claimed, at, term.chinese().length());
                    matches.add(new Match(term.chinese(), term.code(), term.hanViet(), at,
                            term.polarity(), term.strength(), term.glossVi(), term.fromGloss()));
                }
                from = at + 1;
            }
        }
        matches.sort((a, b) -> Integer.compare(a.position(), b.position()));
        return List.copyOf(matches);
    }

    /**
     * True if {@code matches} disagree with themselves — one term favourable
     * and another not. Reported as its own fact rather than resolved: a text
     * saying both "tốt" and "hung" is a CONFLICT, a valid result under
     * Rule E, and averaging it away would destroy the finding.
     */
    public static boolean isMixed(List<Match> matches) {
        boolean favourable = false;
        boolean unfavourable = false;
        for (Match match : matches) {
            if (match.polarity() == Polarity.SUPPORT) {
                favourable = true;
            } else if (match.polarity() == Polarity.NEGATIVE || match.polarity() == Polarity.CAUTION) {
                unfavourable = true;
            }
        }
        return favourable && unfavourable;
    }

    private static boolean unclaimed(boolean[] claimed, int at, int length) {
        for (int i = at; i < at + length; i++) {
            if (claimed[i]) {
                return false;
            }
        }
        return true;
    }

    private static void claim(boolean[] claimed, int at, int length) {
        for (int i = at; i < at + length; i++) {
            claimed[i] = true;
        }
    }
}
