package io.destinyos.persistence.registry;

import io.destinyos.engine.MethodologyStatus;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers the methodologies named in the specification with their real,
 * current status (ADR D7).
 *
 * <p><strong>Every status and research reference below must trace back to
 * {@code docs/RESEARCH_BLOCKERS.md}.</strong> This class is not the place to
 * decide a methodology's status - it is the place that records the decision
 * already made there. If the register changes, this seeder must change to
 * match it; it must never drift ahead of it.
 *
 * <p>Deliberately absent from this list:
 * <ul>
 *   <li>The sexagenary cycle - pure arithmetic, not a user-selectable school,
 *       so it is not a registry entry (it has no Dụng Thần-style dispute to
 *       register a status against).</li>
 *   <li>Fusion, Applicability, Scenario - core infrastructure, not a
 *       metaphysical methodology a user selects.</li>
 *   <li>The Chinese UTC+8 calendar comparison methodology (ADR D3 permits
 *       it, does not commit to it) - not yet scheduled in any phase, so
 *       registering it now would be speculative.</li>
 * </ul>
 */
@Component
public class MethodologyRegistrySeeder {

    private final MethodologyRegistryService registry;

    public MethodologyRegistrySeeder(MethodologyRegistryService registry) {
        this.registry = registry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        seed();
    }

    /**
     * Idempotent per (methodologyId, version): a version already present is
     * left untouched, but a code change that bumps {@code entry.version()}
     * (e.g. promoting a status once real content/algorithm work lands) adds
     * a new version row even on an already-seeded, persistent database
     * (Supabase, not just the in-memory H2 test stand-in) - the same
     * "a version bump, not a silent change" discipline this project applies
     * everywhere else. Caught in practice: promoting TAROT_RWS,
     * NUMEROLOGY_PYTHAGOREAN and CALENDAR_VN_TRADITIONAL to
     * PRODUCTION_READY in code silently had no effect on a real Supabase
     * instance already seeded at "1.0" earlier in the same session, because
     * the previous check only asked "does any version exist" - fixed by
     * versioning the seed data itself.
     */
    @Transactional
    public void seed() {
        for (Entry entry : ENTRIES) {
            boolean versionAlreadySeeded = registry.allVersions(entry.methodologyId).stream()
                    .anyMatch(v -> v.version().equals(entry.version));
            if (!versionAlreadySeeded) {
                registry.register(entry.methodologyId, entry.displayNameVi, entry.domain,
                        entry.version, entry.status, entry.school, entry.source, entry.researchIds,
                        entry.notes);
            }
        }
    }

    private record Entry(String methodologyId, String displayNameVi, String domain,
                         String version, MethodologyStatus status, String school, String source,
                         Set<String> researchIds, String notes) {
    }

    // school/source are null exactly for the entries that may NOT calculate.
    // CONTENT_REQUIRED and PRODUCTION_READY are the two statuses under which
    // MethodologyStatus.mayCalculate() is true, and every entry at one of
    // those statuses names both - today NUMEROLOGY_PYTHAGOREAN, TAROT_RWS,
    // CALENDAR_VN_TRADITIONAL and BAZI_TUBINH_CHART. Getting this wrong is
    // exactly the mistake the MethodologyVersionEntity constructor guard
    // exists to catch: an earlier draft of this seeder left TAROT_RWS's
    // school/source blank and registration failed loudly at startup, as it
    // should have.
    private static final List<Entry> ENTRIES = List.of(

            new Entry("NUMEROLOGY_PYTHAGOREAN", "Thần số học - Pythagoras", "WESTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Pythagorean",
                    "Standard A-Z letter table (converging across all sources checked); "
                            + "Vietnamese normalization policy (Unicode NFD + explicit đ/Đ "
                            + "substitution) and Life Path per-component reduction order "
                            + "sourced and recorded in RESEARCH_BLOCKERS.md R8, 2026-08-18. "
                            + "Interpretive meaning content grounded in the standard, "
                            + "widely-converged Pythagorean numerology corpus (65 entries: "
                            + "5 number types x values 1-9/11/22/33), authored 2026-08-19.",
                    Set.of("R8"),
                    "Algorithm implemented and golden-tested (destiny-engine-numerology) "
                            + "against independently sourced worked examples: Life Path for "
                            + "1990-03-15 = 1; day 29 preserves master number 11; Expression "
                            + "for 'John Doe' = 8. Vietnamese interpretive content for every "
                            + "(type, value) pair now authored (NumerologyNumberMeanings) - "
                            + "the engine emits one real signal per computed number, "
                            + "dimension OTHER, polarity authored per pair since the same "
                            + "number reads differently by type. Y is simplified to "
                            + "always-consonant for Soul Urge/Personality pending a source "
                            + "for Vietnamese-specific treatment - a labelled "
                            + "simplification, not a researched rule. Chaldean remains "
                            + "RESEARCH_REQUIRED separately: no source for a "
                            + "Vietnamese-orthography mapping exists."),

            new Entry("NUMEROLOGY_CHALDEAN", "Thần số học - Chaldea", "WESTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R8"),
                    "No standard Chaldean letter-value mapping exists for "
                            + "Vietnamese orthography. Constructing one would be "
                            + "inventing a methodology, which CLAUDE.md Rule C forbids."),

            new Entry("TAROT_RWS", "Tarot - Rider-Waite-Smith", "WESTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Rider-Waite-Smith (RWS) - 78 cards: 22 Major Arcana, 56 Minor Arcana",
                    "DESTINY_OS_MASTER_SPECIFICATION.md section 17; interpretive content "
                            + "grounded in the standard Rider-Waite-Smith tradition (A.E. "
                            + "Waite, 1910, Pictorial Key to the Tarot, and the consistent "
                            + "derivative corpus since), authored 2026-08-19 "
                            + "(TarotCardMeanings, contentVersion 1.0)",
                    Set.of("R11"),
                    "Deck structure, seeded shuffle and orientation rule are fully "
                            + "specified. All 78 cards now carry authored Vietnamese "
                            + "meaning content (upright/reversed keywords, polarity, and "
                            + "career/finance/relationship/decision/general text) - the "
                            + "engine emits up to 5 real signals per drawn card. Polarity "
                            + "is authored once per orientation, not per dimension, and "
                            + "strength follows arcana/rank (Major -> STRONG, court -> "
                            + "MEDIUM, numbered Minor -> WEAK) - both documented "
                            + "simplifications, not fabrications."),

            new Entry("CALENDAR_VN_TRADITIONAL", "Lịch Việt Nam truyền thống", "EASTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Vietnamese lunisolar calendar, 105 degrees East meridian, "
                            + "no-zhongqi leap-month rule",
                    "Jean Meeus, Astronomical Algorithms (1998) - low-precision solar "
                            + "longitude and new-moon series; cross-checked byte-for-byte "
                            + "against two independent implementations (vanng822/amlich "
                            + "JS port and Vietnamese Wikipedia's \"Mo dun:Am lich\" Lua "
                            + "module) and against Ho Ngoc Duc's own published worked "
                            + "tables (xemamlich.uhm.vn/calrules_en.html, 1983-1986, "
                            + "second-precision) plus four named Vietnamese/Chinese "
                            + "divergence years (1985, 2007, 2030, 2053). Historical "
                            + "timezone table per Cong Bao Viet Nam gazette citations "
                            + "(R14a). Gio Ty 23:00 boundary and solar time policy per "
                            + "owner decision R10, 2026-08-19.",
                    Set.of("R14b", "R17"),
                    "destiny-calendar: astronomical core (new moon, solar longitude, "
                            + "solar terms, leap month) and Can Chi Year/Month/Day/Hour "
                            + "arithmetic implemented and golden-tested against 86 "
                            + "assertions, including an exhaustive 1900-2100 Tet scan and "
                            + "every date in Ho Ngoc Duc's published worked tables - none "
                            + "generated from this project's own code (CLAUDE.md section "
                            + "32). R14b (North/South geographic boundary, no source found "
                            + "across three research rounds) and R17 (region granularity, "
                            + "depends on R14b) remain genuinely open: a birth in an "
                            + "affected (date, region) resolves to RESEARCH_REQUIRED with "
                            + "no Can Chi fabricated, never a silent default - this is a "
                            + "per-calculation limitation, not a reason to withhold "
                            + "PRODUCTION_READY from the cases this methodology does cover, "
                            + "the same model TAROT_RWS and NUMEROLOGY_PYTHAGOREAN already "
                            + "use for their own content gaps (R11/R8)."),

            // Phase 8 is registered as TWO methodologies, not one, because the
            // two halves genuinely have different statuses (DECISION_LOG 2026-08-22).
            // Chart construction is verified and calculable; the interpretive
            // layer is not, and collapsing them into a single status would have
            // to lie in one direction or the other - either hiding a working
            // Tứ Trụ behind RESEARCH_REQUIRED, or implying a Dụng Thần exists.
            new Entry("BAZI_TUBINH_CHART",
                    "Bát Tự - Lập lá số Tứ Trụ và Đại Vận (Tử Bình)", "EASTERN",
                    "1.1",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Tử Bình / Tứ Trụ - ranh giới năm tại Lập Xuân, tháng theo Tiết Khí",
                    "Pillar arithmetic from destiny-calendar (Ngu Ho Don month stem, Ngu "
                            + "Thu Don hour stem, continuous 60-day cycle), golden-tested "
                            + "against Ho Ngoc Duc's published tables. Bat Tu-specific "
                            + "boundaries verified against published Four Pillars tables "
                            + "for the 1984-02-04/05 Lap Xuan transition, 2000-01-01, "
                            + "1990-03-15 and 2024-02-04, cross-checked between smxs.com "
                            + "and k366.com. Tang Can table cross-checked between "
                            + "4thuman.com (VN) and imperialharvest.com (EN); Thap Than "
                            + "derivation rule from phongthuykhaitoan.com (VN) and "
                            + "oracleeast.com/bazi-web.com (EN). Dai Van (R2, closed "
                            + "2026-08-22): direction rule unanimous across sources; "
                            + "distance measured to the adjacent sectional term (Tiet), "
                            + "which is already this project's golden-tested month "
                            + "boundary; three-days-to-one-year conversion confirmed by "
                            + "six published worked examples with no counterexample, and "
                            + "verified end-to-end in both directions against btime.com "
                            + "(1990-01-01, backward, 25 days, 8y4m) and k366.com (lunar "
                            + "17/1/1994, forward, 8 days, 2y8m). All retrieved 2026-08-22.",
                    Set.of("R18", "R19"),
                    "Phase 8a: four pillars, Ngu Hanh and Am Duong of every stem and "
                            + "branch, Tang Can hidden stems, Thap Than relative to the "
                            + "Day Master, and integer element counts. Emits evidence "
                            + "only and NO signals - a Bat Tu signal needs a polarity, "
                            + "and a polarity needs R1/R3, so the engine returns PARTIAL "
                            + "with Dung Than and Day Master strength reported "
                            + "as explicitly blocked sections. Dai Van left that blocked "
                            + "list when R2 closed: the sequence, its direction and its "
                            + "start age are chart data, computed whenever a gender is "
                            + "supplied and omitted with a stated reason when it is not. "
                            + "It still carries no polarity - whether a period is "
                            + "favourable needs R1 and R3. Two open items are its "
                            + "own rather than inherited: R18 (Lap Xuan vs Tet year "
                            + "boundary - the engine implements Lap Xuan, declares it, "
                            + "and flags every birth where the two conventions disagree) "
                            + "and R19 (solar-term instants run up to ~16 minutes early "
                            + "against published tables because the cited Meeus series "
                            + "omits nutation and aberration, so a 40-minute guard window "
                            + "raises a boundary uncertainty instead of claiming "
                            + "minute-level precision). Hidden-stem central/residual role "
                            + "ordering diverges between the two sources for Suu and Ty; "
                            + "the set and the principal stem are recorded, the disputed "
                            + "ordering is flagged and never used."),

            // R3 resolved 2026-08-24 (docs/DECISION_LOG.md, six decisions):
            // Thieu Vi Hoa's own point-scoring method for Day Master
            // strength, implemented and golden-tested against Vi du 5/6/7.
            // A separate entry from BAZI_TUBINH_CHART for the same reason
            // WESTERN_ASTROLOGY_CHART_ANGLES is separate from the chart
            // engine's own school - this is a NAMED school's verdict, not
            // this engine's own construction fact, and does not resolve
            // R1's "no consensus" gap (most schools still need this or an
            // equivalent before Dung Than can be chosen).
            new Entry("BAZI_DAY_MASTER_STRENGTH_TVH",
                    "Bát Tự - Cường độ Nhật Chủ (Thiệu Vĩ Hoa)", "EASTERN",
                    "1.0",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Thiệu Vĩ Hoa & Trần Viên - phương pháp tính điểm độ vượng Ngũ Hành "
                            + "(\"Dự đoán theo Tứ Trụ\", Chương 11)",
                    "Dự đoán theo Tứ trụ, Thiệu Vĩ Hoa & Trần Viên, Chương 11 muc II "
                            + "(tr.331-356). Xác minh bởi Claude Opus "
                            + "(docs/research_drafts/VERIFICATION_OPUS_R3.md): phương pháp có "
                            + "thật, 5/7 vi du tinh dung chinh xac; Vi du 1 va mot vi du khac "
                            + "bi loai vi loi tinh toan trong sach. Vi du 5, 6, 7 dung lam "
                            + "golden test.",
                    Set.of("R3"),
                    "destiny-engine-bazi: DayMasterStrengthResolver, golden-tested chính xác "
                            + "từng độ Ngũ Hành đối với Ví dụ 5, 6 và 7. Sáu quyết định Rule D "
                            + "được ghi trong DECISION_LOG.md: bốn quyết định trước khi implement "
                            + "(−6 độ thay vì −8; ngưỡng 18 độ không chặn điều chỉnh địa chi; "
                            + "thứ tự tàng can của Thân theo sách; ship riêng không gộp vào "
                            + "BAZI_TUBINH_CHART) cộng hai quyết định phát hiện khi chạy golden "
                            + "test (điều kiện 'không gặp hợp' cho boost chỉ áp dụng cho Lục "
                            + "Hợp, không áp dụng cho Tam Hội/Tam Hợp/Bán Tam Hợp; 'kẹp khắc' — "
                            + "một can bị cả hai bên cạnh khắc chế thì hủy bỏ hoàn toàn việc thử "
                            + "ngũ hợp). Không tính cho cách cục đặc biệt (chưa nhận diện được) "
                            + "và từ chối (không đoán) khi có Lục Xung không hóa giải vì bảng "
                            + "tra tổn thất chính xác chưa được số hóa. Không giải quyết R1 — "
                            + "đa số trường phái vẫn cần kết quả này (hoặc tương đương) trước "
                            + "khi chọn Dụng Thần."),

            new Entry("BAZI", "Bát Tự - Luận giải (Dụng Thần, cường độ Nhật Chủ)",
                    "EASTERN",
                    "1.3",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R1", "R3", "R20", "R21", "R22"),
                    "The interpretive half of Bát Tự, Phase 8b. Dụng Thần/Hỷ Thần/Kỵ "
                            + "Thần school selection (R1) and Day Master strength "
                            + "assessment (R3) are unresolved and are the reason "
                            + "BAZI_TUBINH_CHART emits no signals. R20 (combinations "
                            + "and clashes), R21 (Liu Nian/Yue/Ri) and R22 (Shen Sha) "
                            + "joined this entry on 2026-08-23: an audit against "
                            + "Master Spec section 13 found them named there with no "
                            + "research id at all, so unlike R1 and R3 the engine had "
                            + "no way to report them as blocked - a quieter failure "
                            + "than an admitted gap. R20 is the consequential one: it "
                            + "gates R3 under most schools and changes how the element "
                            + "tallies already shown should be read. R2 (Đại Vận) left "
                            + "this entry on 2026-08-22: its direction rule and "
                            + "day-to-year conversion were verified against published "
                            + "worked examples, so the sequence moved to the chart half. "
                            + "That does not narrow R1 or R3 - it separates what can be "
                            + "constructed from what must be judged. R3 is the harder of "
                            + "the two: the classical sources do not merely omit a "
                            + "scoring scheme, Trich Thien Tuy explicitly rejects "
                            + "treating strength as a hard binary, while every numeric "
                            + "scheme found on the modern web is uncited and mutually "
                            + "contradictory. The compute-both-and-report-both escape "
                            + "used for R7 and R18 does not apply, because there is no "
                            + "second well-defined answer to compute."),

            new Entry("ZIWEI", "Tử Vi Đẩu Số", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R4"),
                    "No authoritative an sao rulebook selected. Master Spec section "
                            + "14 explicitly forbids placeholder star placement."),

            new Entry("WESTERN_ASTROLOGY_CHART_ANGLES",
                    "Chiêm tinh học phương Tây - Lập lá số (góc chiếu, cung mọc)", "WESTERN",
                    "1.0",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Tropical zodiac, Whole Sign houses (R6, owner decision 2026-08-23)",
                    "Jean Meeus, Astronomical Algorithms (1998), ch. 12/22",
                    Set.of("R5", "R6"),
                    "Julian Day/T/obliquity/GMST/RAMC and the Midheaven formula "
                            + "follow Jean Meeus, Astronomical Algorithms (1998) ch. 12 "
                            + "and 22 directly. The Sun's ecliptic longitude reuses "
                            + "destiny-calendar's already golden-tested SolarPosition "
                            + "(VSOP87-derived), rather than re-deriving it - this is "
                            + "the self-build path R5's follow-up survey "
                            + "(R5_meeus_path_survey.md) found the original R5 draft had "
                            + "missed because it could not see this repository's own "
                            + "calendar module. The Ascendant formula was re-derived "
                            + "from first principles after two independent web sources "
                            + "were found to disagree on its atan2 quadrant by 180 "
                            + "degrees; the result was verified against two "
                            + "independently-reasoned numerical cases and against Meeus's "
                            + "own GMST worked example. Whole Sign houses need no "
                            + "time-based subdivision, so they carry no further "
                            + "uncertainty beyond the Ascendant itself. Phase 11 v1: "
                            + "emits Sun/Midheaven/Ascendant/houses as chart evidence "
                            + "and no signal. The Moon and the other seven planets "
                            + "(R5 - no VSOP87/ELP2000 data sourced with adequate rigor "
                            + "yet) and aspects (R6 - orb policy still undecided) remain "
                            + "registered BlockedSections on the chart, not silently "
                            + "omitted."),

            new Entry("WESTERN_ASTROLOGY", "Chiêm tinh học phương Tây - Luận giải", "WESTERN",
                    "1.1",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R5", "R6"),
                    "The interpretive half: planetary positions beyond the Sun, "
                            + "aspects between them, and what any of it means. Split "
                            + "from WESTERN_ASTROLOGY_CHART_ANGLES on 2026-08-23 the same "
                            + "way BAZI was split from BAZI_TUBINH_CHART - chart "
                            + "construction moved to CONTENT_REQUIRED once R5 (ephemeris: "
                            + "self-built on Meeus/VSOP87, owner decision) and R6 "
                            + "(zodiac/house system: Tropical + Whole Sign, owner "
                            + "decision) were resolved for the Sun/angles/houses; the "
                            + "remaining planets still have no sourced ephemeris and the "
                            + "aspect-orb policy is still open, so this entry stays "
                            + "blocked rather than narrowing what DECISION_REQUIRED used "
                            + "to cover."),

            new Entry("ICHING_HEXAGRAM_CASTING",
                    "Kinh Dịch - Gieo quẻ và xác định quẻ (Tam Tiền, Thi Thảo, Mai Hoa)",
                    "EASTERN",
                    "1.0",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Kinh Dịch - Tam Tiền (三錢起卦), Thi Thảo (蓍草筮法/大衍筮法), "
                            + "Mai Hoa Dịch Số (梅花易數 - Số, Năm Tháng Ngày Giờ)",
                    "說卦傳 ch.3/5 cho 8 quẻ đơn và hai cách sắp xếp Tiên Thiên/Hậu Thiên; "
                            + "bảng 64 quẻ Văn Vương kiểm chứng bằng 3 phép độc lập (song ánh, "
                            + "quy tắc cặp 綜卦/錯卦, quy ước đặt tên); 繫辭上傳 cho quy trình "
                            + "Thi Thảo 18 biến, phân phối 1:5:7:3 tự suy ra bằng tổ hợp học "
                            + "(không chỉ trích dẫn thứ cấp); 梅花易數 卷一 trích nguyên văn "
                            + "trực tiếp cho quy tắc chia-8 và năm-tháng-ngày-giờ.",
                    Set.of("R12"),
                    "Nghiên cứu bởi Claude Sonnet, xác minh độc lập bởi Claude Opus "
                            + "2026-08-24 - bản draft đầu tiên của dự án qua xác minh không "
                            + "tìm thấy lỗi sai nào. Bốn cách gieo quẻ, mỗi cách một quy trình "
                            + "riêng, không trộn lẫn (Rule D): Tam Tiền và Thi Thảo xác định "
                            + "hào động trực tiếp từ giá trị 6/9 của từng hào, hoàn toàn không "
                            + "vướng mắc gì; hai phương pháp Mai Hoa cần thêm một quyết định "
                            + "Rule D đã ghi vào DECISION_LOG.md (dư 0 khi chia hào động cho 6 "
                            + "→ hào 6, loại suy từ quy tắc chia-8 tường minh trong cùng văn "
                            + "bản). Emits chart evidence only (quẻ gốc, hào động, quẻ biến) - "
                            + "lời quẻ/lời hào nay đã có ở ICHING_HEXAGRAM_JUDGMENT_NGOTATTO, "
                            + "nhưng engine này vẫn không phát tín hiệu, vì việc chọn lời chính "
                            + "khi nhiều hào động và việc suy ra cát/hung vẫn chưa nghiên cứu - "
                            + "xem BlockedSection LINE_SELECTION_RULE và CAT_HUNG_POLARITY trên "
                            + "mỗi lá số."),

            new Entry("ICHING_HEXAGRAM_JUDGMENT_NGOTATTO",
                    "Kinh Dịch - Quẻ từ và hào từ (cổ văn + Ngô Tất Tố)", "EASTERN",
                    "1.1",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Hán văn: 周易 (zh.wikisource.org); dịch âm/dịch nghĩa: Ngô Tất Tố - "
                            + "\"Kinh Dịch Trọn Bộ\", NXB Văn Học",
                    "Hai nguồn cho hai tầng khác bản chất (Rule D, DECISION_LOG.md 2026-09-01). "
                            + "HÁN VĂN 卦辭/爻辭 lấy từ zh.wikisource.org/wiki/周易/*, đọc bằng "
                            + "wikitext thô (action=raw) để không model nào chuẩn hóa ký tự - cổ "
                            + "văn là văn bản chung của mọi ấn bản, không phải lựa chọn dịch "
                            + "thuật. DỊCH ÂM/DỊCH NGHĨA từ Ngô Tất Tố (bản số hóa "
                            + "khoahoctamlinh.vn, chủ dự án cung cấp 2026-08-31; mất 1954 nên "
                            + "vào phạm vi công cộng tại VN từ 2005, Điều 27 Luật SHTT) - đây "
                            + "mới là lựa chọn dịch thuật nên ghi tên dịch giả và không sửa lời "
                            + "văn. Đủ 64 quẻ từ + 386 hào từ (gồm Dụng Cửu/Dụng Lục) kèm số "
                            + "trang. Dị bản của bản wikisource được khai báo: dùng 无 (không "
                            + "dùng 無), 于 (không dùng 於), quẻ 49 chép 巳日 không phải 己日.",
                    Set.of("R24", "R25"),
                    "ĐÃ QUA XÁC MINH OPUS 2026-09-01 (VERIFICATION_OPUS_R24.md) - lần bác bỏ "
                            + "nặng nhất của quy trình này: bản ship ngày 2026-08-31 có 287/386 "
                            + "hào từ mang Hán văn SAI (bộ trích vớ trúng đoạn 象曰 của hào "
                            + "trước), và test đi kèm chỉ khẳng định isNotBlank() nên PASS trong "
                            + "khi ghim văn bản sai vào spec. Xác minh cũng tìm: 15.4% ký tự Hán "
                            + "là Kangxi Radical codepoint; Hán văn của chính cuốn sách sai khác "
                            + "cổ văn ở 34/64 quẻ từ và 280/386 hào từ (hỏng OCR tới mức trộn "
                            + "chữ Latin vào Hán văn) - đó là lý do đổi nguồn Hán văn. Hai lỗi "
                            + "nhãn hào thật của sách bắt được bằng suy dẫn âm/dương (quẻ 45 hào "
                            + "1, quẻ 51 hào 4); 5 quẻ từ bị cắt cụt đã nối lại; 5 khuyết tật "
                            + "của sách đã vá kèm lý do trên từng entry. Được xác nhận đúng: thứ "
                            + "tự chương = thứ tự Văn Vương (59/60 khớp HexagramTable) và phần "
                            + "tiếng Việt (383/384 sạch). Vẫn CONTENT_REQUIRED chứ chưa "
                            + "PRODUCTION_READY vì: chưa ai đọc kiểm độ chính xác bản dịch tiếng "
                            + "Việt (mọi phép kiểm đều là cấu trúc), Hán văn nay dựa một nguồn "
                            + "cho 56/64 quẻ, và chưa ai đọc kiểm bản dịch. Cập nhật 2026-09-01 "
                            + "(vòng hai): 27 entry bị lỗi TRÍCH của chính dự án đã được sửa - "
                            + "26 entry có lời bình GIẢI NGHĨA của sách chạy tràn vào trường "
                            + "nghia không dấu phân cách (quẻ 1 hào 1 dài ~2000 ký tự trong khi "
                            + "lời dịch chỉ 32), 1 entry mang lời Tượng truyện thay cho lời dịch "
                            + "hào (quẻ 16 hào 6), và 1 lỗi in \"Háo\"/\"Hào\" (quẻ 45 hào 6). "
                            + "Bộ test cũ PASS suốt vì chỉ khẳng định isNotBlank() - đúng khuôn "
                            + "mẫu lỗi mà chính VERIFICATION_OPUS_R24 §B3 đã cảnh báo. Nay có "
                            + "test suy dẫn: không marker lời bình, nhãn vị trí suy từ position "
                            + "+ âm/dương, độ dài nghia bám số chữ Hán, và đếm bảng == 386 "
                            + "(LineJudgmentTable nay ném lỗi ngay khi trùng khoá thay vì ghi "
                            + "đè im lặng). Engine NAY ĐÃ PHÁT SIGNAL - xem ICHING_CAT_HUNG_LEXICAL."),

            new Entry("ICHING_CAT_HUNG_LEXICAL",
                    "Kinh Dịch - Cát/hung đọc từ từ vựng phán định trong cổ văn", "EASTERN",
                    "1.0",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Từ vựng phán định cổ văn (吉/凶/悔/吝/无咎); nghĩa 5 chữ theo bảng thuật ngữ "
                            + "của Nguyễn Hiến Lê - \"Kinh Dịch - Đạo Của Người Quân Tử\", NXB "
                            + "Văn Học, tr.92",
                    "Đóng BlockedSection CAT_HUNG_POLARITY (2026-09-01) và là lý do Kinh Dịch "
                            + "lần đầu phát Signal. Đây là phép ĐỌC, không phải công thức: các "
                            + "chữ 吉/凶/悔/吝/无咎 có thật trong Hán văn đã ship (nguồn "
                            + "zh.wikisource, đã qua kiểm codepoint CJK), nên mỗi cực tính truy "
                            + "được về đúng một chữ ở đúng một vị trí trong đúng một câu, và "
                            + "Evidence ghi lại chữ đã khớp. Nghĩa 5 chữ lấy NGUYÊN VĂN tr.92: "
                            + "cát = tốt lành; hung = ngược lại với cát, XẤU NHẤT (nên là chữ "
                            + "duy nhất được STRONG); hối = lỗi, ăn năn; lận = LỖI NHỎ, tiếc "
                            + "(nên nhẹ hơn hối); vô cữu = không có lỗi hoặc lỗi không về ai cả. "
                            + "Các dạng ghép (元吉, 大吉, 中吉, 終吉, 貞吉, 小吉, 終凶, 征凶, "
                            + "无悔, 悔亡) là SUY DẪN của dự án từ chính 5 chữ đó, khai báo tường "
                            + "minh chứ không giả làm trích dẫn (Rule D). Quét chuỗi dài trước "
                            + "chuỗi ngắn là điều kiện đúng đắn, không phải tối ưu: 92 entry "
                            + "mang 无咎/無咎 so với 7 entry mang 咎 trần, nên đọc sai thứ tự sẽ "
                            + "gán NGƯỢC cực tính cho 92 entry. CỐ Ý KHÔNG tính: tứ đức "
                            + "(元/亨/利/貞) vì tr.173 định nghĩa trinh là \"chính và bền\" tức "
                            + "đức tính CÓ ĐIỀU KIỆN, và tr.90-92 nêu 5 cách đọc tứ đức cạnh "
                            + "tranh (a-đ) trong đó cách đ của Cao Hanh (亨=享 tế hưởng, 利貞=利占) "
                            + "phủ định cách a mà chính tác giả chọn; 孚 (tin) vì không phải "
                            + "phán định; 厲 (lệ) vì không nguồn nào tra nghĩa. Đường suy cát/hung "
                            + "theo VỊ HÀO (đắc trung hào 2/5, đắc chính) đã bị BÁC dù đếm được "
                            + "hoàn toàn, vì chính tr.101 tự phủ định: \"trong Dịch, không có qui "
                            + "tắc gì luôn luôn đúng, có rất nhiều lệ ngoại, phải tùy thời mà "
                            + "xét\", và cùng trang nêu luôn phản ví dụ (hai hào đều bất chính "
                            + "mà nghĩa tốt; ca khác đều chính mà nghĩa xấu) - một quy tắc mà "
                            + "chính nguồn của nó in phản ví dụ là đúng thứ Rule C cấm.",
                    Set.of("R24", "R25"),
                    "Độ phủ đo trên toàn bộ 448 văn bản đã ship: 65% có ít nhất một chữ phán "
                            + "định, 35% không có chữ nào và được báo NEUTRAL - đó là văn bản từ "
                            + "chối phán, không phải khoảng trống bị bỏ. 28 entry mang ĐỒNG THỜI "
                            + "chữ tốt và chữ xấu; theo Rule E đây là kết quả hợp lệ nên engine "
                            + "phát nhiều Signal đối cực riêng biệt và để máy consensus/conflict "
                            + "sẵn có tự nhận ra, TUYỆT ĐỐI không lấy trung bình. Bảng valence "
                            + "được TỰ KIỂM CHÉO bằng chính bản dịch Ngô Tất Tố đã ship (nguồn "
                            + "độc lập với bảng): 吉 được ông dịch là \"tốt\" ở 113/119 entry "
                            + "(95%), 咎 là \"lỗi\" ở 88/91 (97%), 吝 là \"tiếc\" ở 19/20 (95%) - "
                            + "đó là phép biến bảng từ một khẳng định thành thứ có nguồn thứ hai "
                            + "đồng ý, và là test sẽ đỏ nếu ai lặng lẽ đảo một cực tính. Chưa "
                            + "PRODUCTION_READY vì: chỉ có một nguồn tra nghĩa cho 5 chữ, và "
                            + "10 dạng ghép vẫn là suy dẫn chưa có nguồn trực tiếp. Không Signal "
                            + "nào được đánh critical trong khi LINE_SELECTION_RULE còn treo: có "
                            + "nhiều hào động thì engine không thể khẳng định chữ 凶 nào mới là "
                            + "câu trả lời cho câu hỏi đã đặt."),

            new Entry("ICHING_HAO_LAM_CHU_NGUYENHIENLE",
                    "Kinh Dịch - Hào làm chủ (chúng dĩ quả vi chủ)", "EASTERN",
                    "1.0",
                    MethodologyStatus.PRODUCTION_READY,
                    "Qui tắc 眾以寡為主，多以少為尊 - Nguyễn Hiến Lê, \"Kinh Dịch - Đạo Của "
                            + "Người Quân Tử\", NXB Văn Học, tr.101-103",
                    "Qui tắc xác định HOÀN TOÀN bằng cấu trúc âm/dương của chính quẻ, không có "
                            + "lựa chọn trường phái và không cần bảng tra. Nguyên văn tr.101: "
                            + "\"Chúng dĩ quả vi chủ, đa dĩ thiểu vi tôn. Nghĩa là cái gì nhiều "
                            + "thì bỏ đi mà lấy cái ít. Theo qui tắc đó, quẻ nào nhiều dương thì "
                            + "lấy âm là chủ; ngược lại thì lấy dương làm chủ.\" Chỉ phát Evidence, "
                            + "TUYỆT ĐỐI không phát Signal, vì nguồn phủ định điều đó hai lần: "
                            + "tr.102 \"Làm chủ chỉ vì nó là số ít trong một đám số nhiều, chứ "
                            + "không phải vì tốt hay xấu\", và tr.102-103 \"...không cần để ý tới "
                            + "hào đó có cao quí hay không, tốt hay xấu\". Lệ ngoại mà sách tự nêu "
                            + "(tr.103: quẻ Cấu, hào 1 là hào âm duy nhất mà không phải hào quyết "
                            + "định ý nghĩa của quẻ) được ship KÈM kết quả chứ không biên dịch "
                            + "thành một nhánh if ẩn - hàm vẫn trả hào 1 cho quẻ 44 vì đó đúng là "
                            + "điều qui tắc phát biểu. Nguồn này chỉ có MỘT loại hào chủ, KHÔNG "
                            + "dùng cặp 成卦之主/主卦之主 của Chu Hi, nên không được đọc thành đã "
                            + "implement khái niệm hào chủ theo trường phái phổ biến hơn (Rule D).",
                    Set.of("R24"),
                    "GOLDEN TEST ĐẦU TIÊN của Kinh Dịch theo nghĩa của dự án - một ví dụ có đáp "
                            + "án in sẵn trong nguồn độc lập, đối chiếu đầu-cuối, lấp đúng gạch "
                            + "đầu dòng thứ ba ở VERIFICATION_OPUS_R24.md §E. Hai ví dụ sách tự "
                            + "kiểm được ghim: quẻ 16 Lôi địa Dự (5 âm, 1 dương) -> hào 4 làm "
                            + "chủ, và quẻ 43 Trạch thiên Quải (5 dương, 1 âm) -> hào 6 làm chủ "
                            + "(tr.102). Hai ví dụ này còn đối nghịch nhau về tốt/xấu, và đó "
                            + "chính là luận điểm của sách. Số quẻ có hào chủ được suy dẫn độc "
                            + "lập chứ không tra bảng: có 6 cách đặt một hào dương giữa năm hào "
                            + "âm và 6 cách ngược lại, nên đúng 12/64 quẻ có hào chủ; 3-3, 4-2, "
                            + "2-4 và hai quẻ thuần Kiền/Khôn đều trả về rỗng - rỗng là câu trả "
                            + "lời thật, vì chọn \"hào thiểu số đầu tiên\" trong ca 4-2 sẽ là tự "
                            + "đặt ra một quy tắc sách không hề phát biểu, và sẽ vô hình vì hàm "
                            + "vẫn trả về một con số hợp lý cho mọi quẻ."),

            new Entry("FENGSHUI_KUA", "Phong Thủy - Bát Trạch (Cung Phi)", "EASTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Bat Trach (Bat Bien Du Nien) - both year-boundary conventions "
                            + "computed, neither silently selected",
                    "Kua formula and the gendered 5-substitution cross-checked between "
                            + "hoc.kabala.vn and nguyenthehoa.com (VN, the latter with two "
                            + "fully worked 1978 examples) and wofs.com + fengshuimall.com "
                            + "(EN); all three agree exactly, including the discontinuity at "
                            + "year 2000. The 8x8 direction table is DERIVED from the Bat Bien "
                            + "Du Nien line-change rule (Chinese 8-mansions mnemonic; "
                            + "Vietnamese 'Bat Bien Du Nien', which names the Tuyet Menh pairs "
                            + "Can-Ly, Khon-Kham, Can-Ton, Doai-Chan) and verified against "
                            + "three published tables - masterseanchan.com's 8x8 matrix "
                            + "(60/64), nguyenthehoa.com's Can page (8/8) and "
                            + "phongthuykhaitoan.com's Chan page. Life-area mapping authored "
                            + "from the descriptions at kasai.com.vn, xaydung365.com.vn and "
                            + "nguyenthehoa.com. All retrieved 2026-08-22.",
                    Set.of("R7"),
                    "Phase 10. Four of R7's five open items are closed: the school (Bat "
                            + "Trach only - Master Spec section 20 forbids blending Phi Tinh "
                            + "or Huyen Khong), the asymmetric male/female formulas, the "
                            + "gendered '5' substitution (male -> Khon, female -> Can), and "
                            + "the direction mapping. The table is derived from a cited rule "
                            + "rather than transcribed, which is what let a verification pass "
                            + "identify four wrong cells in the only complete "
                            + "English-language table found - by symmetry, by a 6:2 majority "
                            + "inside that table, and by direct contradiction from a "
                            + "Vietnamese source. R7's fifth item, the year boundary, stays "
                            + "OPEN and is represented rather than resolved: Vietnamese "
                            + "practice uses the lunar year and classical practice uses Lap "
                            + "Xuan, no source arbitrates, and Bat Tu's R18 decision does not "
                            + "transfer because its evidence was about Four Pillars tables. "
                            + "The engine computes both and, when they disagree, reports both "
                            + "and emits no signal. Signals require a facing direction: a Kua "
                            + "number alone is a profile, not a judgement, so without a "
                            + "direction the engine returns the profile as evidence only."),

            new Entry("ICHING", "Kinh Dịch - Luận giải (chọn lời chính khi nhiều hào động)", "EASTERN",
                    "1.3",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12", "R24"),
                    "This umbrella entry has been emptied out one layer at a time, and only one "
                            + "layer is left. The casting/hexagram-identification half split off "
                            + "to ICHING_HEXAGRAM_CASTING (CONTENT_REQUIRED) on 2026-08-24, the "
                            + "same way BAZI split from BAZI_TUBINH_CHART. On 2026-08-31 the "
                            + "line/hexagram meaning content itself split off, to "
                            + "ICHING_HEXAGRAM_JUDGMENT_NGOTATTO. On 2026-09-01 the cát/hung "
                            + "polarity split off as well, to ICHING_CAT_HUNG_LEXICAL, closing "
                            + "the CAT_HUNG_POLARITY BlockedSection - so Kinh Dịch now emits real "
                            + "Signals into Fusion, and the governing-line layer shipped "
                            + "alongside it as ICHING_HAO_LAM_CHU_NGUYENHIENLE (Evidence only, on "
                            + "purpose). What remains here, and is genuinely still unresearched: "
                            + "which line's judgment text to treat as the PRIMARY reading when "
                            + "several lines move at once. Chu Hi's seven-rule scheme (R12 §7) "
                            + "rests on one low-confidence secondary summary. A second source was "
                            + "checked on 2026-09-01 and does NOT close it: Nguyễn Hiến Lê, "
                            + "\"Kinh Dịch - Đạo Của Người Quân Tử\" - tr.104 he declares \"Đoạn "
                            + "này liên quan tới việc bói, chúng tôi không có ý khảo về môn bói, "
                            + "nên chỉ giảng qua thôi\"; tr.106-107 he says only that every moving "
                            + "line flips AT ONCE giving exactly ONE changed hexagram, counts as "
                            + "far as \"hai, ba hào cùng biến\" and stops, never mentions 4/5/6 "
                            + "moving lines, and gives no rule at all for choosing a primary "
                            + "text. Recorded so a future round does not reopen this source. "
                            + "Consequence while it stays open: with more than one moving line, "
                            + "each line's cát/hung Signal participates at reduced Applicability "
                            + "and none is marked critical."),

            new Entry("MAIHOA", "Mai Hoa Dịch Số - Luận giải", "EASTERN",
                    "1.1",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12"),
                    "Mai Hoa's own casting methods (Number, Year-Month-Day-Hour) are now "
                            + "resolved and implemented under ICHING_HEXAGRAM_CASTING as of "
                            + "2026-08-24 - the number-extraction, timestamp-conversion and "
                            + "trigram/hexagram-derivation gaps this entry originally named are "
                            + "closed. What remains open and specific to Mai Hoa: splitting a "
                            + "single multi-digit number into upper/lower trigrams (secondary "
                            + "sources only - two independent sources agree, but the primary "
                            + "text does not contain this rule, confirmed by direct re-fetch) - "
                            + "not shipped; the Number method requires two supplied numbers "
                            + "instead. Mai Hoa's own interpretive layer (體用 body/use "
                            + "analysis, five-element relationships between trigrams) is "
                            + "entirely unresearched and distinct from plain Kinh Dịch line "
                            + "text - not attempted this round."),

            new Entry("QIMEN", "Kỳ Môn Độn Giáp", "EASTERN",
                    "1.0",
                    MethodologyStatus.OUT_OF_SCOPE, null, null,
                    Set.of("R13"),
                    "METHODOLOGY_RESEARCH_REGISTER.md section 9: not to be "
                            + "implemented without a full rule specification, which "
                            + "does not exist in this repository.")
    );
}
