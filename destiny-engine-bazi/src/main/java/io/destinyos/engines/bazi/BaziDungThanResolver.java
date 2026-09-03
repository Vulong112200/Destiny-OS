package io.destinyos.engines.bazi;

import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.HiddenStems;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Dụng Thần (Useful Element) selection for the "cách phổ thông" (ordinary
 * pattern) rules of Thiệu Vĩ Hoa &amp; Trần Viên's <em>Dự đoán theo Tứ
 * trụ</em>, Chương 11 §IV mục 1 (pp.378-381) — the school this project
 * declared for R1 ({@code docs/DECISION_LOG.md} "R1 decided", 2026-09-03).
 * Source transcription: {@code docs/research_drafts/R1_thieuvyhoa_dung_than.md}
 * mục 1. Golden-tested against {@code docs/research_drafts/R1_chuong23_dung_than.md}
 * (10 worked charts, Chương 23 pp.583-629).
 *
 * <h2>Scope this class deliberately narrows (Stage 1, per explicit task
 * instructions — not this class's own discretion)</h2>
 * <ul>
 *   <li><strong>Only the 8 "cách phổ thông" with a fully-transcribed
 *       decision chain</strong> — Chính Tài (a, partially — see below),
 *       Chính Quan (b), Chính Ấn (c), Thương Quan (d), Thất Sát (e), Thực
 *       Thần (h), Kiến Lộc (i), Kình Dương (k) — plus Thiên Tài, which the
 *       source states reuses Chính Tài's logic verbatim ("Dụng thần của các
 *       cách: thiên tài, thiên quan, thực thần đều lấy theo phương pháp lý
 *       luận như trên", tr.380). The 13 "cách cục đặc biệt" (Tòng Cách, Hóa
 *       Cách, Nhuận Hạ, ...) from mục 2 are <strong>not</strong> implemented —
 *       the source gives only a flat table (cách → element), no decision
 *       procedure, and no numeric threshold for detecting when a chart
 *       qualifies as one of them (only a single worked example, Ví dụ 18 of
 *       mục V, shows the pattern being invoked at all).</li>
 *   <li><strong>Dụng Thần Điều Hầu (climate/seasonal adjustment, mục 5,
 *       pp.387-397) is never applied</strong>, even though several of the
 *       Chương 23 golden charts use it (Ví dụ 3b tr.595, Ví dụ 6 tr.605) —
 *       this is a deliberately separate, still-open item
 *       ({@code docs/RESEARCH_BLOCKERS.md} R1, "Điều Hậu / Thông Quan giữ
 *       nguyên BlockedSection riêng"). No precedence rule between Điều Hầu
 *       and Phù Ức is stated anywhere in the source, so this class does not
 *       invent one. Charts whose book answer used Điều Hầu are expected to
 *       mismatch this class's output — recorded, not hidden, in
 *       {@code BaziDungThanResolverGoldenTest}.</li>
 * </ul>
 *
 * <h2>"Cách" (pattern) identification — a stated technical inference, not a
 * citation</h2>
 * The source presents mục 1 as ten already-named patterns and never states
 * <em>how</em> to read a chart's pattern off its Tứ Trụ; the closest thing
 * to a rule is that pattern names track Nguyệt Lệnh (month branch). This
 * class resolves the pattern as: the Thập Thần (relative to the Day Master)
 * of the month branch's <strong>chính khí</strong> (principal hidden stem,
 * {@link HiddenStems#of}) — the standard Bát Tự convention that Nguyệt Lệnh
 * carries the highest weight of any single position. This is a stated
 * engineering inference, not something the source itself asserts.
 *
 * <p><strong>A genuine naming collision this inference exposes.</strong> The
 * source names ten cách phổ thông, but this project's {@link TenGod} model
 * (the standard structural one — see its own Javadoc) has exactly ten roles,
 * and {@link TenGod#THAT_SAT}'s own Javadoc already documents that "Thất
 * Sát" and "Thiên Quan" are the same role under this model. The source's
 * mục 1 gives Thất Sát (e) a complete, list-form rule, and <em>separately</em>
 * states "Thiên Quan ... lấy theo phương pháp lý luận như trên [Chính
 * Quan]" (tr.380) — two different rules claiming the one Thập Thần this
 * project can compute. This class resolves {@link TenGod#THAT_SAT} to the
 * Thất Sát rule (mục 1.e), since it is the one with a complete, directly
 * transcribed procedure; the "Thiên Quan reuses Chính Quan" footnote is
 * therefore unreachable through this identification method and is not
 * implemented. This is reported as a finding, not silently resolved as
 * though no tension existed.
 *
 * <p>{@link TenGod#THIEN_AN} (Thiên Ấn / Kiêu Thần) has no cách in mục 1 at
 * all — the source never writes a "cách thiên ấn" — so a month branch whose
 * chính khí is Thiên Ấn relative to the Day Master always refuses.
 *
 * <h2>"Nhiều" (a Thập Thần category is numerous) — an undocumented gap in
 * the source, filled by a stated, non-fabricated rule</h2>
 * Every decision chain in mục 1 is conditioned on some category — Tỉ Kiếp,
 * Ấn, Thực Thương, Tài, Quan Sát (the five standard groupings) — being
 * "nhiều" in the chart, but the source never states a threshold. Nothing in
 * this codebase defines one either. Rather than invent an arbitrary number,
 * this class reuses the only numeric baseline this project's own R3
 * ({@link DayMasterStrengthResolver}) already committed to: Vượng is
 * {@code ownSideDegrees >= 40%} of the total, i.e. two of the five groupings
 * (the Day Master's own element and the one that generates it) together
 * meeting the uniform 2-of-5 share. Applying the same uniform-share logic to
 * a single grouping gives 1-of-5, i.e. {@code >= 20%} of the total — so
 * "nhiều" here means a category's degree total (from
 * {@link DayMasterStrength#elementDegrees()}, the same already-golden-tested
 * numbers R3 produces, regrouped from Ngũ Hành into the five Thập Thần
 * categories relative to the Day Master) is at least one fifth of the
 * chart's total degrees. <strong>This is a stated engineering bridge from
 * R3's own threshold, not a number the source states</strong> — flagged
 * here exactly as the task that created this class required, so a future
 * reader does not mistake it for a citation.
 *
 * <p>"Không có X" (category X absent) is read literally: that category's
 * degree total is exactly zero.
 *
 * <h2>Chain-walking semantics — revised after checking the golden charts</h2>
 * The first draft of this class gated the primary element on it being
 * present in the chart (degree greater than 0), falling back to the stated
 * fallback category otherwise, and walked triggers in the source's own
 * textual order. Checked against the ten worked charts in
 * {@code R1_chuong23_dung_than.md}, both choices turned out to be
 * empirically wrong, not just unconfirmed:
 * <ul>
 *   <li>Vi du 3a (tr.591) and Vi du 5 (tr.600) both name a Dung Than whose
 *       element is completely absent from the chart (Kim for both, degree
 *       0 in this class's own computed {@link DayMasterStrength#elementDegrees()}),
 *       yet the book still names the primary (the theoretically needed
 *       antidote) as Dung Than rather than falling back to what is actually
 *       present. This matches ordinary Bat Tu theory - an absent Dung Than
 *       is a normal, named phenomenon, not an error state to fall back away
 *       from; Vi du 5's own text separately names the chart's actually
 *       present element (Ti Kiep) as Hy Than, not Dung Than, confirming
 *       presence-in-chart is not what "chon X lam dung than" is testing.
 *       Consequently: the primary element is always the answer once its
 *       trigger fires, whether or not it is present in the chart. The
 *       "khong co X thi Y" clauses are still transcribed in every trigger's
 *       citation text (nothing is cut), but this class does not know when
 *       they actually apply and does not simulate one - a stated gap, not a
 *       silently-resolved rule the golden charts already falsified.</li>
 *   <li>Vi du 9 (tr.618) has two categories simultaneously "nhieu" (Tai and
 *       Quan Sat both exceed the 1/5 threshold below); the book's answer
 *       (An) matches the Quan Sat trigger's primary, not the Tai trigger's,
 *       even though Tai is listed first in the source's own prose - Quan
 *       Sat's degree (122) is larger than Tai's (76) in this chart. So:
 *       when multiple triggers in a chain are simultaneously "nhieu", this
 *       class applies the one with the largest degree (the most dominant
 *       excess), not the textually-first one - ties keep the source's
 *       listed order.</li>
 * </ul>
 * If no trigger's condition holds at all, this class refuses
 * ({@link RefuseReason#NO_TRIGGER_MATCHED}).
 *
 * <p>A few clauses list two or three alternative primary elements joined by
 * "hoặc" (or) with no stated tie-break (e.g. Kình Dương Vượng: "chọn tài
 * quan có lực... hoặc chọn thực thương có lực"). This class breaks the tie
 * by picking whichever listed option has the higher degree total (ties
 * broken by the source's own listed order) — reusing the same
 * degree-comparison already used for "nhiều", not a new fabricated rule.
 * Unlike the primary/fallback question above, no golden chart happens to
 * exercise this specific tie-break, so it remains an unconfirmed (but
 * principled) choice.
 *
 * <h2>What is out of scope, inherited unresolved from elsewhere in this
 * codebase, per explicit task instruction</h2>
 * <ul>
 *   <li><strong>Trung hòa (balanced/neutral Day Master).</strong>
 *       {@link DayMasterStrength#vuong()} is a strict boolean
 *       ({@code ownSideDegrees * 5 >= totalDegrees * 2}); nothing in R3 or
 *       elsewhere in this codebase defines a third "trung hòa" state near
 *       that 40% boundary. This class does <strong>not</strong> add one —
 *       every chart is treated as either Vượng or Nhược per that existing
 *       boolean, with no special handling for charts close to the
 *       boundary. This is a real, unaddressed gap, not a resolved one.</li>
 *   <li><strong>Suspected Tòng Cách / Chuyên Vượng.</strong>
 *       {@link DayMasterStrengthResolver}'s own Javadoc already states this
 *       method's formula excludes special-pattern charts and that "every
 *       result from this class is only as good as the assumption that the
 *       chart is an ordinary one" — it defines no extremity threshold for
 *       detecting one. This class inherits that same gap verbatim and does
 *       not invent a threshold to refuse on; a chart that a human reader
 *       would call Tòng Cách is processed by the ordinary-pattern chains
 *       above exactly like any other chart.</li>
 * </ul>
 *
 * <h2>Hỷ Thần (Favourable Element)</h2>
 * Mục 1 (this class's entire source) states Dụng Thần selection only. Hỷ
 * Thần/Kỵ Thần for the natal chart is mục 3 (pp.382-385) — a different
 * research question (closer to R21, the luck-cycle interaction, per
 * {@code R1_thieuvyhoa_dung_than.md}'s own analysis) and out of this
 * class's scope. {@link DungThanResult#hyThan()} is therefore always empty.
 */
public final class BaziDungThanResolver {

    /** Bumps whenever a rule choice in this class's own logic changes. */
    static final String RULE_VERSION = "1.0";

    private BaziDungThanResolver() {
    }

    /** One of the ten cách phổ thông this class can identify and resolve. */
    public enum Pattern {
        CHINH_TAI, THIEN_TAI, CHINH_QUAN, CHINH_AN, THUONG_QUAN, THAT_SAT, THUC_THAN, KIEN_LOC, KINH_DUONG
    }

    /** Why {@link #resolve} refused rather than answered. */
    public enum RefuseReason {
        /** Month branch's chính khí is {@link TenGod#THIEN_AN} — mục 1 has no rule for it. */
        PATTERN_NOT_IDENTIFIED,
        /** No {@link DayMasterStrength} was supplied (e.g. an unresolved Lục Xung, or no hour precision). */
        DAY_MASTER_STRENGTH_UNAVAILABLE,
        /** Pattern is Chính Tài/Thiên Tài and the Day Master is Nhược — source's Nhược branch was never transcribed. */
        NO_RULE_FOR_PATTERN_BRANCH,
        /** No trigger category in the matched chain was judged "nhiều" for this chart. */
        NO_TRIGGER_MATCHED
    }

    /**
     * @param pattern  the cách phổ thông identified from Nguyệt Lệnh
     * @param dungThan the Useful Element this chain resolved to
     * @param hyThan   always empty — see class Javadoc
     * @param citation which rule fired, quoted, with page number
     */
    public record DungThanResult(Pattern pattern, FiveElement dungThan, List<FiveElement> hyThan, String citation) {
        public DungThanResult {
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(dungThan, "dungThan");
            hyThan = hyThan == null ? List.of() : List.copyOf(hyThan);
            Objects.requireNonNull(citation, "citation");
        }
    }

    /** Either a resolved verdict, or a refusal that carries its own reason (Rule C — never a silent guess). */
    public sealed interface Outcome permits Resolved, Refused {
    }

    public record Resolved(DungThanResult result) implements Outcome {
    }

    public record Refused(RefuseReason reason, String detail) implements Outcome {
    }

    /** Convenience for callers that only want the resolved value, discarding the refusal reason. */
    public static Optional<DungThanResult> resolveOptional(BaziPillar year, BaziPillar month, BaziPillar day,
                                                     BaziPillar hour, DayMasterStrength strength) {
        Outcome outcome = resolve(year, month, day, hour, strength);
        return outcome instanceof Resolved r ? Optional.of(r.result()) : Optional.empty();
    }

    public static Outcome resolve(BaziPillar year, BaziPillar month, BaziPillar day, BaziPillar hour,
                           DayMasterStrength strength) {
        Objects.requireNonNull(year, "year");
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(day, "day");
        Objects.requireNonNull(hour, "hour");

        if (strength == null) {
            return new Refused(RefuseReason.DAY_MASTER_STRENGTH_UNAVAILABLE,
                    "Khong co DayMasterStrength (R3) - khong the xac dinh Vuong/Nhuoc de chon nhanh quy tac.");
        }

        HeavenlyStem dayMaster = day.stem();
        HeavenlyStem monthPrincipal = HiddenStems.of(month.branch()).principal();
        TenGod monthTenGod = TenGods.of(dayMaster, monthPrincipal);
        Pattern pattern = patternOf(monthTenGod);
        if (pattern == null) {
            return new Refused(RefuseReason.PATTERN_NOT_IDENTIFIED,
                    "Nguyet Lenh chinh khi la " + monthPrincipal + ", Thap Than " + monthTenGod
                            + " (Thien An/Kieu Than) so voi Nhat Chu " + dayMaster
                            + " - muc 1 khong co 'cach thien an', khong the tra cuu.");
        }

        boolean vuong = strength.vuong();
        if ((pattern == Pattern.CHINH_TAI || pattern == Pattern.THIEN_TAI) && !vuong) {
            return new Refused(RefuseReason.NO_RULE_FOR_PATTERN_BRANCH,
                    "Cach " + pattern + ", Nhat Chu Nhuoc: nhanh Nhuoc cua muc 1.a (Chinh Tai) chua duoc doc/"
                            + "trich day du trong docs/research_drafts/R1_thieuvyhoa_dung_than.md (chi co mot phan "
                            + "nhanh Vuong, cau bi cat o '...', khong co nhanh Nhuoc). Khong tu bia.");
        }

        List<Trigger> chain = chainFor(pattern, vuong);
        FiveElement dm = dayMaster.element();

        // Among every trigger whose "when" category is judged "nhieu", pick
        // the one with the largest degree (most dominant excess) - not the
        // textually-first one. Unconditional triggers (when == null, Kien
        // Loc/Kinh Duong) are always eligible with degree treated as
        // "infinite priority" since there is nothing to compare them against
        // in those chains (each has exactly one trigger). See class Javadoc,
        // "Chain-walking semantics", for the Vi du 9 evidence behind this.
        Trigger best = null;
        long bestDeg = -1;
        for (Trigger t : chain) {
            if (t.when() == null) {
                best = t;
                break;
            }
            long deg = degree(t.when(), dm, strength);
            if (nhieu(t.when(), dm, strength) && deg > bestDeg) {
                bestDeg = deg;
                best = t;
            }
        }
        if (best == null) {
            return new Refused(RefuseReason.NO_TRIGGER_MATCHED,
                    "Cach " + pattern + ", Nhat Chu " + (vuong ? "Vuong" : "Nhuoc")
                            + ": khong co Thap Than nao trong chuoi quy tac duoc danh gia la 'nhieu' (>= 1/5 tong do).");
        }

        // The primary element is always the answer once its trigger fires -
        // presence in the chart is not required (see class Javadoc for the
        // Vi du 3a/5 evidence). Multiple "hoac" options are broken by degree,
        // ties keeping the source's own listed order.
        Category chosen = pickPrimary(best.primary(), dm, strength);
        FiveElement element = categoryElement(chosen, dm);
        String citation = best.citation() + " => dung than = " + element + ".";
        return new Resolved(new DungThanResult(pattern, element, List.of(), citation));
    }

    private static Pattern patternOf(TenGod tg) {
        return switch (tg) {
            case TY_KIEN -> Pattern.KIEN_LOC;
            case KIEP_TAI -> Pattern.KINH_DUONG;
            case THUC_THAN -> Pattern.THUC_THAN;
            case THUONG_QUAN -> Pattern.THUONG_QUAN;
            case THIEN_TAI -> Pattern.THIEN_TAI;
            case CHINH_TAI -> Pattern.CHINH_TAI;
            case THAT_SAT -> Pattern.THAT_SAT;
            case CHINH_QUAN -> Pattern.CHINH_QUAN;
            case THIEN_AN -> null;
            case CHINH_AN -> Pattern.CHINH_AN;
        };
    }

    // ------------------------------------------------------------------
    // The five standard Thap Than groupings (Tuong Sinh/Tuong Khac relative
    // to the Day Master's own element), and their degree lookup.
    // ------------------------------------------------------------------

    private enum Category {
        TI_KIEP, AN, THUC_THUONG, TAI, QUAN_SAT
    }

    private static FiveElement categoryElement(Category c, FiveElement dm) {
        return switch (c) {
            case TI_KIEP -> dm;
            case AN -> dm.generatedBy();
            case THUC_THUONG -> dm.generates();
            case TAI -> dm.controls();
            case QUAN_SAT -> dm.controlledBy();
        };
    }

    private static int degree(Category c, FiveElement dm, DayMasterStrength strength) {
        return strength.elementDegrees().getOrDefault(categoryElement(c, dm), 0);
    }

    /**
     * "Nhiều": this category's degree is at least one fifth of the chart's
     * total — the uniform 1-of-5 share, matching the same reasoning R3's own
     * 40% (2-of-5) Vượng threshold already commits to. See class Javadoc —
     * this is a stated technical bridge, not a number the source states.
     */
    private static boolean nhieu(Category c, FiveElement dm, DayMasterStrength strength) {
        long deg = degree(c, dm, strength);
        long total = strength.totalDegrees();
        return total > 0 && 5 * deg >= total;
    }

    /**
     * Picks the single-element answer among (usually one, occasionally
     * several "hoặc"-joined) primary options: the one with the highest
     * degree, ties keeping the source's own listed order. Never gates on
     * presence — see class Javadoc "Chain-walking semantics" for why (Vi du
     * 3a/5 both name a Dụng Thần with degree 0). {@code options} is never
     * empty for any chain defined in this class.
     */
    private static Category pickPrimary(List<Category> options, FiveElement dm, DayMasterStrength strength) {
        Category best = options.get(0);
        int bestDeg = degree(best, dm, strength);
        for (int i = 1; i < options.size(); i++) {
            Category c = options.get(i);
            int d = degree(c, dm, strength);
            if (d > bestDeg) {
                bestDeg = d;
                best = c;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // Decision chains, transcribed from R1_thieuvyhoa_dung_than.md muc 1
    // (tr.378-381). "when" null = unconditional (Kien Loc, Kinh Duong).
    // ------------------------------------------------------------------

    private record Trigger(Category when, List<Category> primary, List<Category> fallback, String citation) {
    }

    private static Trigger trig(Category when, List<Category> primary, List<Category> fallback, String citation) {
        return new Trigger(when, primary, fallback, citation);
    }

    private static List<Trigger> chainFor(Pattern pattern, boolean vuong) {
        return switch (pattern) {
            case CHINH_TAI -> vuong ? chinhTaiVuong("Cach Chinh Tai (a)") : List.of();
            case THIEN_TAI -> vuong
                    ? chinhTaiVuong("Cach Thien Tai (tai dung logic Chinh Tai/a, tr.380: \"Dung than cua cac cach: "
                            + "thien tai, thien quan, thuc than deu lay theo phuong phap ly luan nhu tren\")")
                    : List.of();
            case CHINH_QUAN -> vuong ? chinhQuanVuong() : chinhQuanNhuoc();
            case CHINH_AN -> vuong ? chinhAnVuong() : chinhAnNhuoc();
            case THUONG_QUAN -> vuong ? thuongQuanVuong() : thuongQuanNhuoc();
            case THAT_SAT -> vuong ? thatSatVuong() : thatSatNhuoc();
            case THUC_THAN -> vuong ? thucThanVuong() : thucThanNhuoc();
            case KIEN_LOC -> vuong ? kienLocVuong() : kienLocNhuoc();
            case KINH_DUONG -> vuong ? kinhDuongVuong() : kinhDuongNhuoc();
        };
    }

    // a. Chinh Tai (tr.378) - source text is cut off mid-sentence after the
    // second trigger, and its Nhuoc branch was never transcribed at all
    // (see class Javadoc and NO_RULE_FOR_PATTERN_BRANCH). Only these two
    // Vuong-branch triggers are backed by the source; no third trigger, and
    // no fallback for the second, are implemented because none is quoted.
    private static List<Trigger> chinhTaiVuong(String label) {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(Category.THUC_THUONG),
                        label + ", Nhat Chu vuong, Ti Kiep nhieu: \"quan sat co the che ngu ti kiep doat tai nen lay "
                                + "quan sat lam dung than. Thuc thuong co the lam hao ton khi cua ti kiep, nen trong "
                                + "truong hop khong co quan sat co the lay thuc than lam dung than\" (tr.378)"),
                trig(Category.AN, List.of(Category.TAI), List.of(),
                        label + ", Nhat Chu vuong, An nhieu: \"tai se khac an thu, nen lay tai lam dung than\" "
                                + "(tr.378) - nguon bi cat ngay sau cau nay, khong co nhanh du phong duoc trich.")
        );
    }

    // b. Chinh Quan (tr.378-379). The Vuong chain's first trigger's own
    // sentence is split across the tr.378/379 page boundary in the source
    // ("...cua ti kiep, trong truong hop khong co quan sat, co the lay thuc
    // than, thuong quan lam dung than") - its primary (Quan Sat) is inferred
    // from the identical opening every other rule in muc 1 uses for "Ti Kiep
    // nhieu" (a, c, d all state Quan Sat as primary there), not itself
    // directly quoted for rule b - flagged, not a citation for this specific
    // instance.
    private static List<Trigger> chinhQuanVuong() {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(Category.THUC_THUONG),
                        "Cach Chinh Quan (b), Nhat Chu vuong, Ti Kiep nhieu: \"...cua ti kiep, trong truong hop khong "
                                + "co quan sat, co the lay thuc than, thuong quan lam dung than\" (tr.378-379; primary "
                                + "Quan Sat suy tu cach mo dau giong het cua cach a/c/d, khong phai trich truc tiep "
                                + "cho rieng cach b)"),
                trig(Category.AN, List.of(Category.TAI), List.of(Category.QUAN_SAT),
                        "Cach Chinh Quan (b), Nhat Chu vuong, An nhieu: \"tai khac an, nen co the lay tai tinh lam "
                                + "dung than. Quan sat co the la xi hoi cua an thu, vi vay trong truong hop khong co "
                                + "tai, co the lay quan sat lam dung than\" (tr.379)"),
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(Category.TAI),
                        "Cach Chinh Quan (b), Nhat Chu vuong, Thuong Quan/Thuc Than nhieu: \"an che ngu duoc thuong, "
                                + "thuc nen co the lay an thu lam dung than. Thuong, thuc sinh tai nhung lam ton hao "
                                + "than, truong hop khong co an thu, co the lay tai tinh lam dung than\" (tr.379)")
        );
    }

    private static List<Trigger> chinhQuanNhuoc() {
        return List.of(
                trig(Category.TAI, List.of(Category.TI_KIEP), List.of(Category.AN),
                        "Cach Chinh Quan (b), Nhat Chu nhuoc, Tai nhieu: \"ti kiep co the ho giup tai nen co the lay "
                                + "ti kiep lam dung than. An thu co the lam hao ton khi cua tai, trong truong hop "
                                + "khong co ti kiep, co the lay an thu lam dung than\" (tr.379)"),
                trig(Category.QUAN_SAT, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Chinh Quan (b), Nhat Chu nhuoc, Quan Sat nhieu: \"nhung cach chinh quan khong duoc lay "
                                + "thuc, thuong lam dung than. An thu co the lam xi hoi cua quan sat, nen lay an thu "
                                + "lam dung than. Ti kiep co the giup do than, trong truong hop khong co an thu, co "
                                + "the lay ti kiep lam dung than\" (tr.379) - luu y rang buoc tuong minh cam dung "
                                + "Thuc/Thuong da duoc ap dung: An la nhanh chinh, khong phai Thuc Thuong."),
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(),
                        "Cach Chinh Quan (b), Nhat Chu nhuoc, Thuc/Thuong nhieu: \"an co the che ngu thuc thuong, "
                                + "nen lay an thu lam dung than\" (tr.379)")
        );
    }

    // c. Chinh An (tr.379-380).
    private static List<Trigger> chinhAnVuong() {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(Category.THUC_THUONG),
                        "Cach Chinh An (c), Nhat Chu vuong, Ti Kiep nhieu: \"quan sat co the che ngu ti kiep nen co "
                                + "the lay quan sat lam dung than. Thuc thuong co the lam xi hoi cua ti kiep, trong "
                                + "truong hop khong co quan sat, co the lay thuc than, thuong quan lam dung than\" "
                                + "(tr.379)"),
                trig(Category.TAI, List.of(Category.QUAN_SAT), List.of(),
                        "Cach Chinh An (c), Nhat Chu vuong, Tai nhieu: \"quan sat co the lam xi hoi cua tai tinh, "
                                + "nen co the lay quan sat lam dung than\" (tr.379)"),
                trig(Category.AN, List.of(Category.TAI), List.of(Category.THUC_THUONG),
                        "Cach Chinh An (c), Nhat Chu vuong, An nhieu: \"tai co the khac an, nen lay tai tinh lam "
                                + "dung than. Thuc than co the lam hao ton khi cua an thu, trong truong hop khong co "
                                + "tai tinh, co the lay thuc than, thuong quan lam dung than\" (tr.379)")
        );
    }

    private static List<Trigger> chinhAnNhuoc() {
        return List.of(
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Chinh An (c), Nhat Chu nhuoc, Thuc/Thuong nhieu: \"an thu co the che ngu thuc thuong, "
                                + "nen co the lay an thu lam dung than. Ti kiep co the lam xep khi cua thuc, thuong, "
                                + "nen trong truong hop khong co an thu co the lay ti kiep lam dung than\" (tr.379-380)"),
                trig(Category.QUAN_SAT, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Chinh An (c), Nhat Chu nhuoc, Quan Sat nhieu: \"an lam xep hoi cua quan sat, nen co "
                                + "the lay an thu lam dung than. Ti kiep co the giup than, lai co the lam hao ton "
                                + "khi cua quan sat, trong truong hop khong co an thu, co the lay ti kiep lam dung "
                                + "than\" (tr.380)"),
                trig(Category.TAI, List.of(Category.TI_KIEP), List.of(Category.AN),
                        "Cach Chinh An (c), Nhat Chu nhuoc, Tai nhieu: \"ti kiep co the bao ve tai, nen lay ti kiep "
                                + "lam dung than. An thu co the lam ton hao nguyen khi cua tai, truong hop khong co "
                                + "ti kiep, co the lay an thu lam dung than\" (tr.380)")
        );
    }

    // d. Thuong Quan (tr.380).
    private static List<Trigger> thuongQuanVuong() {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(),
                        "Cach Thuong Quan (d), Nhat Chu vuong, Ti Kiep nhieu: \"quan sat co the che ngu ti kiep "
                                + "(nhung chua ly tuong lam, vi quan, thuong khong the cung gap) nen lay quan sat "
                                + "lam dung than\" (tr.380)"),
                trig(Category.AN, List.of(Category.TAI), List.of(Category.QUAN_SAT),
                        "Cach Thuong Quan (d), Nhat Chu vuong, An nhieu: \"tai khac an, lay tai lam dung than. Quan "
                                + "sat co the lam xep nguyen khi cua an thu, nen truong hop khong co tai, co the lay "
                                + "quan sat lam dung than\" (tr.380)")
        );
    }

    private static List<Trigger> thuongQuanNhuoc() {
        return List.of(
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Thuong Quan (d), Nhat Chu nhuoc, Thuc/Thuong nhieu: \"an thu vua sinh than vua che "
                                + "ngu duoc thuc, thuong, nen lay an thu lam dung than. Ti kiep lam xep hoi cua "
                                + "thuc, thuong, nen truong hop khong co an, co the lay ti kiep giup than lam dung "
                                + "than\" (tr.380)"),
                trig(Category.TAI, List.of(Category.TI_KIEP), List.of(Category.AN),
                        "Cach Thuong Quan (d), Nhat Chu nhuoc, Tai nhieu: \"ti kiep co the ho tai, nen lay ti kiep "
                                + "lam dung than. An thu co the lam hao khi cua tai, khi khong co ti kiep, co the "
                                + "lay an thu lam dung than\" (tr.380)"),
                trig(Category.QUAN_SAT, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Thuong Quan (d), Nhat Chu nhuoc, Quan Sat nhieu: \"an thu co the lam xi hoi quan sat, "
                                + "nen co the lay an lam dung than. Ti kiep co the lam hao ton khi cua quan sat, co "
                                + "the lay ti kiep giup than lam dung than\" (tr.380) - nhanh du phong 'khong co an' "
                                + "khong duoc noi tuong minh o cau thu hai nhu cac trigger khac, suy ra tu cau truc "
                                + "chinh/du-phong nhat quan cua toan chuong, khong phai trich truc tiep.")
        );
    }

    // e. That Sat (tr.380), list form in the source (not prose chains).
    private static List<Trigger> thatSatVuong() {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(Category.TAI),
                        "Cach That Sat (e), Nhat Chu vuong, Ti Kiep nhieu thi chon quan sat lam dung than. Neu "
                                + "khong co quan sat thi chon tai tinh (tr.380)"),
                trig(Category.AN, List.of(Category.TAI), List.of(Category.QUAN_SAT, Category.THUC_THUONG),
                        "Cach That Sat (e), Nhat Chu vuong, An nhieu thi chon tai tinh lam dung than. Khong co tai "
                                + "tinh thi chon quan sat hoac thuc thuong (tr.380)"),
                trig(Category.QUAN_SAT, List.of(Category.THUC_THUONG), List.of(),
                        "Cach That Sat (e), Nhat Chu vuong, Quan Sat nhieu thi chon thuc thuong lam dung than "
                                + "(tr.380)")
        );
    }

    private static List<Trigger> thatSatNhuoc() {
        return List.of(
                trig(Category.TAI, List.of(Category.TI_KIEP), List.of(Category.AN),
                        "Cach That Sat (e), Nhat Chu nhuoc, Tai nhieu thi chon ti kiep lam dung than. Khong co ti "
                                + "kiep thi chon an tinh (tr.380)"),
                trig(Category.QUAN_SAT, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach That Sat (e), Nhat Chu nhuoc, Quan Sat nhieu thi chon an tinh lam dung than. Khong co "
                                + "an thi chon ti kiep (tr.380)"),
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach That Sat (e), Nhat Chu nhuoc, Thuc Thuong nhieu thi chon an tinh lam dung than. "
                                + "Khong co an tinh thi chon ti kiep (tr.380)")
        );
    }

    // h. Thuc Than (tr.380-381). No f, g in the source (see research note).
    private static List<Trigger> thucThanVuong() {
        return List.of(
                trig(Category.TI_KIEP, List.of(Category.QUAN_SAT), List.of(Category.THUC_THUONG, Category.TAI),
                        "Cach Thuc Than (h), Nhat Chu vuong, Ti Kiep nhieu thi chon quan sat lam dung than. Khong "
                                + "co quan sat thi chon thuc thuong hoac tai tinh (tr.380)"),
                trig(Category.AN, List.of(Category.TAI), List.of(Category.QUAN_SAT, Category.THUC_THUONG),
                        "Cach Thuc Than (h), Nhat Chu vuong, An tinh nhieu thi chon tai tinh lam dung than. Khong "
                                + "co tai tinh thi chon quan sat hoac thuc thuong lam dung than thong quan (tr.380-"
                                + "381)"),
                trig(Category.TAI, List.of(Category.QUAN_SAT), List.of(Category.THUC_THUONG),
                        "Cach Thuc Than (h), Nhat Chu vuong, Tai tinh nhieu thi chon quan sat lam dung than. Khong "
                                + "co quan sat thi chon thuc thuong (tr.381)")
        );
    }

    private static List<Trigger> thucThanNhuoc() {
        return List.of(
                trig(Category.TAI, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Thuc Than (h), Nhat Chu nhuoc, Tai tinh nhieu thi chon an tinh lam dung than. Khong "
                                + "co an tinh thi chon ti kiep (tr.381)"),
                trig(Category.THUC_THUONG, List.of(Category.AN), List.of(Category.TI_KIEP),
                        "Cach Thuc Than (h), Nhat Chu nhuoc, Thuc thuong nhieu thi chon an tinh lam dung than. "
                                + "Khong co an tinh thi chon ti kiep (tr.381)")
        );
    }

    // i. Kien Loc (tr.381) - unconditional (no "nhieu" trigger stated).
    private static List<Trigger> kienLocVuong() {
        return List.of(
                trig(null, List.of(Category.TAI, Category.QUAN_SAT), List.of(Category.THUC_THUONG),
                        "Cach Kien Loc (i), Nhat Chu vuong: \"chon tai, quan co luc lam dung than. Neu khong co thi "
                                + "chon thuc thuong co luc\" (tr.381)")
        );
    }

    private static List<Trigger> kienLocNhuoc() {
        return List.of(
                trig(null, List.of(Category.AN, Category.TI_KIEP), List.of(),
                        "Cach Kien Loc (i), Nhat Chu nhuoc: \"chon an tinh hoac ti kiep lam dung than\" (tr.381)")
        );
    }

    // k. Kinh Duong (tr.381) - unconditional (no "nhieu" trigger stated).
    private static List<Trigger> kinhDuongVuong() {
        return List.of(
                trig(null, List.of(Category.TAI, Category.QUAN_SAT, Category.THUC_THUONG), List.of(),
                        "Cach Kinh Duong (k), Nhat Chu vuong: \"chon tai quan co luc lam dung than, hoac chon thuc "
                                + "thuong co luc lam dung than\" (tr.381)")
        );
    }

    private static List<Trigger> kinhDuongNhuoc() {
        return List.of(
                trig(null, List.of(Category.AN, Category.TI_KIEP), List.of(),
                        "Cach Kinh Duong (k), Nhat Chu nhuoc: \"chon an tinh hoac ti kiep giup tro than lam dung "
                                + "than\" (tr.381)")
        );
    }
}
