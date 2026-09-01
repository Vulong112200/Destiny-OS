package io.destinyos.engines.bazi;

import io.destinyos.calendar.CanChi;
import io.destinyos.calendar.CanChiPillar;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.HiddenStems;
import io.destinyos.calendar.HistoricalTimezoneRule;
import io.destinyos.calendar.HistoricalTimezoneRuleTable;
import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.LunarCalendar;
import io.destinyos.calendar.LunarDate;
import io.destinyos.calendar.SolarTerm;
import io.destinyos.calendar.SolarTimeCorrection;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.Uncertainty;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.EngineWarning;
import io.destinyos.core.result.ResearchReference;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Signal;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.SupportedDateRange;
import io.destinyos.engine.ValidationResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Bát Tự chart construction — Phase 8a, and only Phase 8a.
 *
 * <p><strong>Why this engine exists while Phase 8 is still blocked.</strong>
 * {@code docs/RESEARCH_BLOCKERS.md} blocks Phase 8 on three items: Dụng Thần
 * school selection (R1), Đại Vận start age and direction (R2), and Day Master
 * strength assessment (R3). All three are <em>interpretive</em>. None of them
 * touches chart construction, which the calendar cluster's resolution
 * (R9/R10/R14a/R15/R16, 2026-08-19) unblocked. Phase 8 was therefore split:
 * 8a is this, 8b is everything the three open items gate. The split is
 * recorded in {@code docs/DECISION_LOG.md}.
 *
 * <p><strong>This engine emits no signals, on purpose.</strong> A
 * {@link Signal} requires a {@link io.destinyos.core.signal.Polarity}, and
 * there is no honest polarity to give: whether Canh Kim in the month pillar is
 * favourable depends entirely on Day Master strength (R3) and on the Dụng Thần
 * school (R1). Emitting NEUTRAL signals to fill the gap would inflate Fusion's
 * source count with content-free votes, which {@code FUSION_ENGINE_SPEC} §4
 * exists to prevent. So the status is {@link EngineStatus#PARTIAL}: real hard
 * data, no interpretation, and a {@link ResearchReference} naming the research
 * that would change that.
 *
 * <p><strong>School (Rule D).</strong> Tử Bình / Tứ Trụ with
 * {@link BaziYearBoundary#LAP_XUAN} and solar-term months. When a birth falls
 * in the window where the Lập Xuân and Tết conventions disagree, both year
 * pillars are reported and the disagreement is raised as a result-affecting
 * uncertainty (R18) rather than resolved.
 */
public final class BaziEngine implements MetaphysicalEngine<BaziInput, BaziChart> {

    public static final String ENGINE_ID = "BAZI";
    public static final String METHODOLOGY_ID = "BAZI_TUBINH_CHART";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Tử Bình / Tứ Trụ — ranh giới năm tại Lập Xuân, tháng theo Tiết Khí";

    /** R3's own methodology (Rule D decision #4, {@code docs/DECISION_LOG.md}) — a named school, not this engine's. */
    public static final String DAY_MASTER_STRENGTH_METHODOLOGY_ID = "BAZI_DAY_MASTER_STRENGTH_TVH";
    public static final String DAY_MASTER_STRENGTH_SCHOOL =
            "Thiệu Vĩ Hoa & Trần Viên — phương pháp tính điểm độ vượng Ngũ Hành "
                    + "(\"Dự đoán theo Tứ Trụ\", Chương 11)";

    public static final String SOURCE =
            "Pillar arithmetic reused from destiny-calendar (Ngu Ho Don month stem, Ngu Thu "
                    + "Don hour stem, continuous 60-day cycle), itself golden-tested against "
                    + "Ho Ngoc Duc's published tables. Bat Tu-specific boundaries (Lap Xuan "
                    + "year, solar-term month) verified against published Four Pillars tables "
                    + "for the 1984-02-04/05 Lap Xuan transition, 2000-01-01, 1990-03-15 and "
                    + "2024-02-04, cross-checked between smxs.com and k366.com (see "
                    + "BaziEngineGoldenTest). Tang Can table cross-checked between "
                    + "4thuman.com (VN) and imperialharvest.com (EN), both retrieved "
                    + "2026-08-22; the role-ordering disagreement between them is recorded, "
                    + "not resolved. Thap Than derivation rule from phongthuykhaitoan.com "
                    + "(VN) and oracleeast.com/bazi-web.com (EN), same date. Year-boundary "
                    + "school split recorded as R18; solar-term instant precision limit as R19.";

    /**
     * How close to a Tiết Khí boundary a birth must be before the month (and
     * possibly the year) pillar is flagged as boundary-sensitive.
     *
     * <p>Not an arbitrary round number. The solar longitude series this project
     * uses is Meeus's low-precision one — the cited, golden-tested function
     * {@code destiny-calendar} shares with the Vietnamese lunar calendar — and
     * it omits nutation and aberration, so its solar-term instants run early
     * against published Purple Mountain Observatory tables. Measured while
     * building this engine: Lập Xuân 1984 computed 23:11 against a published
     * 23:18:44, and Lập Xuân 2024 computed 16:11 against a published 16:27
     * (both Beijing time). 40 minutes covers the largest observed deviation
     * with margin. Research item R19 records the option of adopting an
     * apparent-longitude correction to shrink this window; until then the
     * window is the honest statement of what the model knows.
     */
    static final double SOLAR_TERM_GUARD_MINUTES = 40.0;

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Bát Tự — Lá số Tứ Trụ",
            METHODOLOGY_ID,
            METHODOLOGY_VERSION,
            "1.0",
            SCHOOL,
            SOURCE,
            // Chart construction is verified; the interpretive content that
            // would turn it into a reading is not (R1/R2/R3). That is exactly
            // what CONTENT_REQUIRED means, and it is the status Tarot carried
            // before its own meaning corpus was authored.
            MethodologyStatus.CONTENT_REQUIRED
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            // Dimensions this engine *could* speak to once R1/R3 resolve. It
            // declares them so the Applicability layer keeps scheduling it -
            // the chart itself is useful output today - but it contributes no
            // signal to any of them yet.
            .dimensions(Dimension.CAREER, Dimension.FINANCE, Dimension.RELATIONSHIP,
                    Dimension.TIMING, Dimension.DECISION, Dimension.DAILY, Dimension.OTHER)
            // False on purpose: without an exact hour this engine still produces
            // real year and month pillars. It degrades and says which parts are
            // missing, rather than declining outright.
            .requiresBirthTime(false)
            // Region is required (R14a/R14b); longitude is optional (R10).
            .requiresLocation(true)
            .requiresName(false)
            .requiresCalendar(true)
            .deterministic(true)
            .requiresSeed(false)
            .supportedDateRange(SupportedDateRange.of(LocalDate.of(1900, 1, 1),
                    LocalDate.of(2100, 12, 31)))
            .build();

    /** The three sections R1/R2/R3 gate, reported as blocked rather than omitted. */
    private static final List<BlockedSection> BLOCKED_SECTIONS = List.of(
            new BlockedSection("DUNG_THAN", "Dụng Thần / Hỷ Thần / Kỵ Thần", "R1",
                    "Các trường phái Bát Tự chọn Dụng Thần theo những cách khác nhau và cho ra "
                            + "kết quả trái ngược nhau trên cùng một lá số. Hệ thống chưa chọn "
                            + "trường phái nào, nên không đưa ra Dụng Thần thay vì đưa ra một "
                            + "đáp án nghe có lý.",
                    List.of("Phù ức Nhật Chủ", "Điều hậu (khí hậu theo mùa)",
                            "Thông quan (hòa giải xung khắc)", "Chuyên vượng / tòng cách")),
            new BlockedSection("NHAT_CHU_CUONG_DO",
                    "Cường độ Nhật Chủ — thang điểm liên trường phái", "R3",
                    "Cường độ Nhật Chủ đã được tính, nhưng theo đúng một trường phái có tên: "
                            + "phép tính điểm độ vượng của Thiệu Vĩ Hoa & Trần Viên, ship dưới "
                            + "methodology riêng BAZI_DAY_MASTER_STRENGTH_TVH và có golden test "
                            + "theo chính các ví dụ trong sách (R3 chốt 2026-08-24). Cái chưa có "
                            + "là một thang điểm dùng chung được giữa các trường phái: các "
                            + "trường phái khác cân đo vượng/suy theo cách khác — có phái không "
                            + "quy về điểm số — nên con số của phái này không so sánh được với "
                            + "phái kia, và không tồn tại 'cường độ Nhật Chủ' chung để hiển thị "
                            + "như một sự thật của lá số. Hai giới hạn phải đọc kèm kết quả đang "
                            + "có: phương pháp này không phủ cách cục đặc biệt (chính sách gốc "
                            + "loại trừ, và engine chưa nhận ra lá số nào thuộc loại đó), và tự "
                            + "nó không mở được R1 — biết thân vượng hay nhược vẫn chưa suy ra "
                            + "Dụng Thần. Bảng đếm Ngũ Hành ở trên vẫn là số đếm thô, không phải "
                            + "điểm độ vượng.",
                    List.of("Tính điểm định lượng (Thiệu Vĩ Hoa) — phái đang dùng",
                            "Vượng suy định tính, không quy về điểm (Trích Thiên Tủy / "
                                    + "Nhậm Thiết Tiều)",
                            "Nhị phân vượng/nhược hay có thêm bậc trung hòa",
                            "Có/không xử lý cách cục đặc biệt")),
            // R20-R22 were added on 2026-08-23. Before that they were absent
            // from this list entirely - not because the engine computed them,
            // but because no research id existed to name them, so a reader
            // had no way to learn they were missing. That is a quieter failure
            // than an admitted gap, and the reason this list is now checked
            // against Master Spec section 13 rather than against itself.
            new BlockedSection("HOP_XUNG_HINH_HAI_PHA",
                    "Quan hệ Hợp / Xung / Hình / Hại / Phá giữa các trụ", "R20",
                    "Một phần tầng này đã được tính, nhưng chỉ ở bên trong: Tam Hội, Tam Hợp, "
                            + "Bán Tam Hợp, Lục Hợp (kèm điều kiện hóa và xử lý tranh hợp), Lục "
                            + "Xung và Thiên Can Ngũ Hợp được tính trong phạm vi nội bộ để phục "
                            + "vụ phép tính cường độ Nhật Chủ (R3) — cùng đúng thứ tự ưu tiên "
                            + "Tam Hội > Tam Hợp > Bán Tam Hợp > Lục Hợp và luật 'tham hợp quên "
                            + "xung' mà phương pháp đó dùng. Đó là một tập con hẹp chứ chưa phải "
                            + "tầng phân tích quan hệ: Hình và Hại chưa tính; Phá đã được khai "
                            + "báo ngoài phạm vi vì không có mặt trong hai cổ thư tham chiếu; "
                            + "quan hệ giữa các trụ không liền kề chưa được xét (R20 chốt ghi "
                            + "khoảng cách là một thuộc tính chứ không phải bộ lọc, nhưng không "
                            + "nguồn nào cho hệ số suy giảm); và lá số có một cặp Lục Xung không "
                            + "được hóa giải thì chính phép tính R3 cũng dừng thay vì đoán. Quan "
                            + "trọng hơn: nửa đánh giá của tầng này — một quan hệ cụ thể là tốt "
                            + "hay xấu cho lá số này — không chặn ở R20 mà chặn ở R1, vì cổ thư "
                            + "phát biểu mọi phán xét đó bằng từ vựng Hỷ Thần / Kỵ Thần. Nên "
                            + "phần đã tính vẫn nằm bên trong engine, chưa lộ ra thành mục đọc "
                            + "riêng, và các bảng đếm Ngũ Hành ở trên vẫn là số đếm thô.",
                    List.of("Hợp giải được xung / xung phá được hợp",
                            "Chỉ trụ liền kề mới tác dụng / trụ cách xa vẫn tác dụng yếu hơn",
                            "Tam hội > Tam hợp > Lục hợp khi cùng xuất hiện",
                            "Hợp có làm đổi Ngũ Hành của chi (hóa) hay không",
                            "Tự hình gồm ba chi (Thìn, Ngọ, Dậu) hay bốn (thêm Hợi)")),
            new BlockedSection("LUU_NIEN",
                    "Lưu Niên / Lưu Nguyệt / Lưu Nhật (vận theo năm, tháng, ngày)", "R21",
                    "Đây là tầng nằm ngay trên Đại Vận: Đại Vận cho biết giai đoạn 10 năm, "
                            + "Lưu Niên cho biết một năm cụ thể trong giai đoạn đó tương tác thế "
                            + "nào. Đã nghiên cứu xong tầng năm, chưa có gì cho tầng tháng và "
                            + "tầng ngày. Tầng năm: bộ quy tắc Chương 7 của sách Thiệu Vĩ Hoa đã "
                            + "được đọc và dựng lại đúng cấu trúc ba danh sách (khi không có "
                            + "tương tác / khi đang tương tác / sau khi tương tác xong), và thứ "
                            + "tự đọc Lưu Niên → Đại Vận → Tiểu Vận đã được chốt. Nhưng gần như "
                            + "mọi quy tắc trong đó kết luận bằng 'cát/hung theo Hỷ - Dụng - Kỵ "
                            + "Thần', tức lấy đầu ra của R1 làm đầu vào; đúng một quy tắc (đủ "
                            + "bốn chi Tý, Ngọ, Mão, Dậu) tính được mà không cần R1, và một quy "
                            + "tắc lẻ thì chưa thành một tầng phân tích. Ngoài ra tương tác đi "
                            + "qua các quan hệ Hợp/Xung mà R20 mới chỉ đặc tả xong ở tầng cơ "
                            + "học. Tầng tháng và tầng ngày: bốn vòng nghiên cứu không tìm được "
                            + "nguồn nào, và suy quy tắc của tầng năm xuống tháng/ngày chính là "
                            + "bước nghe-có-lý mà mục này tồn tại để chặn. Can chi của một "
                            + "năm/tháng/ngày thì hệ thống tính được từ lâu; cái thiếu là luật "
                            + "đọc nó.",
                    List.of("Ba tầng năm/tháng/ngày đọc độc lập hay phân cấp",
                            "Tương tác với lá số gốc, với Đại Vận, hay với cả hai")),
            new BlockedSection("THAN_SAT",
                    "Thần Sát (các sao phụ: Đào Hoa, Dịch Mã, Thiên Ất Quý Nhân…)", "R22",
                    "Khác các mục trên, câu hỏi đầu tiên ở đây là có dùng Thần Sát hay không, "
                            + "trước cả câu hỏi tính thế nào — Master Spec ghi Thần Sát kèm điều "
                            + "kiện 'nếu methodology hỗ trợ', vì một số trường phái dùng rất "
                            + "nhiều còn một số coi đây là phần thêm về sau và gần như không "
                            + "dùng. Câu hỏi đó đã được đặt và đã có câu trả lời cho trường phái "
                            + "đang chọn: có dùng. Điều kiện tiên quyết Thai Nguyên / Cung Mệnh "
                            + "đã có công thức và đã dò lại đúng theo ví dụ trong sách. Cái chặn "
                            + "không còn là phương pháp mà là nền dẫn nguồn: 31 trong 33 bảng "
                            + "sao vẫn chỉ dựa vào một nguồn duy nhất, mà nguồn đó dự án chỉ có "
                            + "Quyển 1 nên 13 sao tra không ra bảng. Chỉ hai sao — Thiên Ất Quý "
                            + "Nhân và Kình Dương — đủ điều kiện ship, và riêng bảng Kình Dương "
                            + "là một biến thể thiểu số phải đi kèm quyết định chiều Trường Sinh "
                            + "của can âm. Hai sao thì chưa thành một tầng Thần Sát, nên engine "
                            + "chưa hiển thị tầng này. Với một trường phái khác, 'không dùng "
                            + "Thần Sát' vẫn là một kết luận hợp lệ.",
                    List.of("Trường phái có dùng Thần Sát / không dùng",
                            "Tập sao thay đổi theo nguồn, từ vài sao tới hơn một trăm",
                            "Kình Dương của can âm: theo chiều nghịch (Ất → Dần) hay theo bảng "
                                    + "phổ thông (Ất → Thìn)")));

    @Override
    public EngineResult<BaziChart> calculate(BaziInput input, CalculationContext context) {
        Objects.requireNonNull(input, "input");

        LocalDate utcDate = input.utcInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        var ruleOpt = HistoricalTimezoneRuleTable.resolve(utcDate, input.region());
        if (ruleOpt.isEmpty()) {
            // R14b: no sourced rule covers this (date, region). The calendar
            // module refuses to fabricate an offset, and every pillar depends
            // on one, so there is nothing partial to salvage here.
            return EngineResult.researchRequired(new ResearchReference(
                    "R14b", "Calendar",
                    "Không có quy tắc múi giờ lịch sử được xác minh cho (" + utcDate + ", "
                            + input.region() + "). Không thể lập lá số mà không suy đoán "
                            + "múi giờ.",
                    "docs/RESEARCH_BLOCKERS.md R14b",
                    List.of("Bắc (VNDCCH)", "Nam (VNCH)")));
        }

        HistoricalTimezoneRule rule = ruleOpt.get();
        double utcOffsetHours = rule.utcOffsetHours();
        List<Uncertainty> uncertainties = new ArrayList<>();
        List<EngineWarning> warnings = new ArrayList<>();

        uncertainties.add(Uncertainty.informational(UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN,
                "Múi giờ lịch sử dựa trên trích dẫn Công Báo qua nguồn thứ cấp; văn bản gốc "
                        + "chưa được đối chiếu trực tiếp (R14a)."));

        LocalDateTime civilLocal = LocalDateTime.ofInstant(input.utcInstant(),
                ZoneOffset.ofTotalSeconds((int) Math.round(utcOffsetHours * 3600)));
        LocalDateTime solarLocal = civilLocal;
        if (input.longitudeDegreesIfKnown() != null) {
            solarLocal = civilLocal.plus(SolarTimeCorrection.meanSolarTimeCorrection(
                    input.longitudeDegreesIfKnown(), utcOffsetHours));
        } else {
            uncertainties.add(Uncertainty.of(UncertaintyKind.LONGITUDE_UNKNOWN,
                    "Chưa có kinh độ nơi sinh nên dùng giờ đồng hồ hành chính thay cho giờ mặt "
                            + "trời (R10). Chỉ ảnh hưởng khi giờ sinh sát ranh giới canh giờ.",
                    "R10"));
        }

        double julianDateUt = JulianDay.fromLocalDateTime(solarLocal, utcOffsetHours);
        int solarMonthIndex = BaziPillarResolver.solarMonthIndex(julianDateUt);
        int baziYear = BaziPillarResolver.baziYear(solarLocal.toLocalDate(), solarMonthIndex);

        CanChiPillar yearCanChi = BaziPillarResolver.yearPillar(baziYear);
        CanChiPillar monthCanChi = BaziPillarResolver.monthPillar(baziYear, solarMonthIndex);

        CanChiPillar dayCanChi = null;
        CanChiPillar hourCanChi = null;
        if (input.precision().supportsHourPrecision()) {
            dayCanChi = BaziPillarResolver.dayPillar(solarLocal, utcOffsetHours);
            hourCanChi = BaziPillarResolver.hourPillar(dayCanChi, solarLocal);
        } else {
            uncertainties.add(Uncertainty.of(UncertaintyKind.BIRTH_TIME_IMPRECISE,
                    "Độ chính xác giờ sinh là " + input.precision() + ". Trụ Ngày và Trụ Giờ "
                            + "cần giờ sinh chính xác nên bị bỏ trống — và cùng với chúng là "
                            + "Nhật Chủ, tức toàn bộ phần Thập Thần.",
                    null));
            warnings.add(EngineWarning.critical("BAZI_NO_DAY_MASTER",
                    "Không có Nhật Chủ vì giờ sinh không chính xác; lá số chỉ gồm Trụ Năm và "
                            + "Trụ Tháng."));
        }

        addSolarTermBoundaryUncertainty(julianDateUt, uncertainties, warnings);
        addYearBoundaryDisagreement(solarLocal.toLocalDate(), utcOffsetHours, baziYear,
                uncertainties, warnings);

        HeavenlyStem dayMaster = dayCanChi == null ? null : dayCanChi.stem();

        BaziPillar yearPillar = pillar(PillarPosition.YEAR, yearCanChi, dayMaster);
        BaziPillar monthPillar = pillar(PillarPosition.MONTH, monthCanChi, dayMaster);
        BaziPillar dayPillar = dayCanChi == null
                ? null : pillar(PillarPosition.DAY, dayCanChi, dayMaster);
        BaziPillar hourPillar = hourCanChi == null
                ? null : pillar(PillarPosition.HOUR, hourCanChi, dayMaster);

        LuckCycles luckCycles = resolveLuckCycles(input, yearCanChi, baziYear, solarMonthIndex,
                julianDateUt, solarLocal, utcOffsetHours, uncertainties, warnings);

        DayMasterStrength dayMasterStrength = dayPillar == null ? null
                : resolveDayMasterStrength(yearPillar, monthPillar, dayPillar, hourPillar,
                        uncertainties, warnings);

        BaziChart chart = new BaziChart(
                yearPillar, monthPillar, dayPillar, hourPillar,
                BaziYearBoundary.LAP_XUAN,
                baziYear,
                SolarTerm.atJulianDate(julianDateUt),
                solarMonthIndex,
                solarLocal,
                tally(yearPillar, monthPillar, dayPillar, hourPillar),
                luckCycles,
                dayMasterStrength,
                BLOCKED_SECTIONS,
                uncertainties);

        for (BlockedSection blocked : BLOCKED_SECTIONS) {
            warnings.add(EngineWarning.critical(
                    "BAZI_SECTION_BLOCKED_" + blocked.sectionId(),
                    blocked.displayNameVi() + ": " + blocked.reasonVi()
                            + " (" + blocked.researchId() + ")"));
        }

        return new EngineResult<>(
                EngineStatus.PARTIAL,
                chart,
                buildEvidence(chart),
                // No signals: see this class's Javadoc. Not an oversight -
                // BaziEngineTest asserts this list stays empty until R1/R3
                // resolve, so filling it becomes a deliberate act.
                List.of(),
                List.copyOf(warnings),
                List.of(),
                new ResearchReference("R1", "Bát Tự",
                        "Lá số Tứ Trụ và chuỗi Đại Vận đã lập xong và là dữ liệu thật. Phần "
                                + "luận giải (Dụng Thần R1, cường độ Nhật Chủ R3) chưa có "
                                + "trường phái được chọn, nên engine không phát sinh tín hiệu "
                                + "nào cho Fusion — kể cả về Đại Vận, vì một vận chỉ tốt hay "
                                + "xấu khi đã có Dụng Thần.",
                        "docs/RESEARCH_BLOCKERS.md R1/R3",
                        List.of("R1 Dụng Thần", "R3 cường độ Nhật Chủ")),
                Map.of("methodologyId", METHODOLOGY_ID,
                        "yearBoundary", BaziYearBoundary.LAP_XUAN.name(),
                        "utcOffsetHours", String.valueOf(utcOffsetHours)));
    }

    /**
     * The Đại Vận sequence, or {@code null} plus a stated reason.
     *
     * <p>Two degradations, and they differ in kind. <strong>No gender</strong>
     * means no direction, and a guessed direction produces a sequence that is
     * wrong from the first period onward while looking exactly like a right
     * one — so nothing is produced. <strong>No exact hour</strong> only blurs
     * the distance to the term boundary, by at most half a day, which the
     * conversion turns into about two months of start age against periods
     * lasting ten years — so the sequence is produced and the blur is stated.
     */
    private static LuckCycles resolveLuckCycles(BaziInput input, CanChiPillar yearCanChi,
                                                int baziYear, int solarMonthIndex,
                                                double julianDateUt, LocalDateTime solarLocal,
                                                double utcOffsetHours,
                                                List<Uncertainty> uncertainties,
                                                List<EngineWarning> warnings) {
        if (input.gender() == null) {
            uncertainties.add(Uncertainty.of(UncertaintyKind.REQUIRED_INPUT_MISSING,
                    "Chưa có giới tính nên không tính được Đại Vận: chiều thuận/nghịch của "
                            + "chuỗi vận phụ thuộc giới tính kết hợp với âm dương can năm, và "
                            + "đoán chiều sẽ cho ra một chuỗi sai ngay từ vận đầu nhưng trông "
                            + "y hệt một chuỗi đúng. Lá số Tứ Trụ không bị ảnh hưởng.",
                    "R2"));
            warnings.add(EngineWarning.critical("BAZI_NO_LUCK_CYCLES",
                    "Không có Đại Vận vì thiếu giới tính."));
            return null;
        }

        if (!input.precision().supportsHourPrecision()) {
            uncertainties.add(Uncertainty.of(UncertaintyKind.BIRTH_TIME_IMPRECISE,
                    "Giờ sinh không chính xác nên tuổi khởi vận Đại Vận chỉ là ước lượng: "
                            + "khoảng cách tới Tiết được đo từ một giờ danh nghĩa, sai lệch tối "
                            + "đa nửa ngày, tương đương khoảng hai tháng tuổi khởi vận. Thứ tự "
                            + "và can chi các vận không bị ảnh hưởng.",
                    "R2"));
        }

        return LuckCycleResolver.resolve(yearCanChi, baziYear, solarMonthIndex,
                input.gender(), julianDateUt, solarLocal, utcOffsetHours);
    }

    /**
     * Thiệu Vĩ Hoa's Day Master strength verdict (R3), or {@code null} for a
     * chart {@link DayMasterStrengthResolver} declines rather than guesses at
     * (an unmitigated Lục Xung with no fractional-loss table sourced — see
     * its Javadoc). A named school's own computed answer, independent of and
     * not a resolution of the "no consensus" gap {@code BLOCKED_SECTIONS}'
     * {@code NHAT_CHU_CUONG_DO} entry still reports (Rule D,
     * {@code docs/DECISION_LOG.md}'s R3 decision #4).
     */
    private static DayMasterStrength resolveDayMasterStrength(BaziPillar year, BaziPillar month,
                                                              BaziPillar day, BaziPillar hour,
                                                              List<Uncertainty> uncertainties,
                                                              List<EngineWarning> warnings) {
        Optional<DayMasterStrength> result = DayMasterStrengthResolver.resolve(year, month, day, hour);
        if (result.isEmpty()) {
            uncertainties.add(Uncertainty.of(UncertaintyKind.METHODOLOGY_UNRESOLVED,
                    "Lá số có một cặp Địa Chi Lục Xung không được hóa giải bởi bất kỳ tổ hợp "
                            + "nào khác, và bảng tra tổn thất chính xác cho trường hợp này (theo "
                            + "Thiệu Vĩ Hoa) chưa được số hóa. Không có cường độ Nhật Chủ theo "
                            + "phương pháp này cho lá số này.",
                    "R3"));
            warnings.add(EngineWarning.critical("BAZI_DAY_MASTER_STRENGTH_UNAVAILABLE",
                    "Không tính được cường độ Nhật Chủ (Thiệu Vĩ Hoa) vì lá số có Lục Xung "
                            + "chưa hóa giải."));
            return null;
        }
        uncertainties.add(Uncertainty.informational(UncertaintyKind.METHODOLOGY_UNRESOLVED,
                "Cường độ Nhật Chủ dưới đây theo phương pháp tính điểm của Thiệu Vĩ Hoa — một "
                        + "trường phái cụ thể, không phải sự đồng thuận chung. Phương pháp giả "
                        + "định lá số thuộc dạng bình thường; các cách cục đặc biệt (tòng cách…) "
                        + "chưa được hệ thống này nhận diện."));
        return result.get();
    }

    /**
     * A birth close to a Tiết Khí instant can fall in either of two months once
     * the model's own precision limit is admitted (R19). The pillar is still
     * reported — it is the model's best answer — but flagged so no layer above
     * can present it as settled.
     */
    private static void addSolarTermBoundaryUncertainty(double julianDateUt,
                                                        List<Uncertainty> uncertainties,
                                                        List<EngineWarning> warnings) {
        double minutes = SolarTerm.minutesToNearestTermBoundary(julianDateUt);
        if (minutes > SOLAR_TERM_GUARD_MINUTES) {
            return;
        }
        uncertainties.add(Uncertainty.of(UncertaintyKind.SOLAR_TERM_BOUNDARY,
                "Giờ sinh cách ranh giới Tiết Khí khoảng " + Math.round(minutes)
                        + " phút, nằm trong sai số của phép tính thời điểm Tiết Khí (khoảng "
                        + Math.round(SOLAR_TERM_GUARD_MINUTES) + " phút, xem R19). Trụ Tháng — "
                        + "và nếu đây là ranh giới Lập Xuân thì cả Trụ Năm — có thể thuộc "
                        + "tháng liền kề.",
                "R19"));
        warnings.add(EngineWarning.critical("BAZI_SOLAR_TERM_BOUNDARY",
                "Sinh sát ranh giới Tiết Khí: Trụ Tháng chưa thể coi là chắc chắn."));
    }

    /**
     * The R18 case, stated as a fact about this specific birth rather than as a
     * general caveat: compute what the Tết convention would give, and if it
     * differs from the Lập Xuân answer, report both.
     */
    private static void addYearBoundaryDisagreement(LocalDate solarDate, double utcOffsetHours,
                                                    int baziYear, List<Uncertainty> uncertainties,
                                                    List<EngineWarning> warnings) {
        LunarDate lunar = LunarCalendar.toLunar(solarDate.getDayOfMonth(),
                solarDate.getMonthValue(), solarDate.getYear(), utcOffsetHours);
        if (lunar.year() == baziYear) {
            return;
        }
        CanChiPillar lapXuanBased = CanChi.yearPillar(baziYear);
        CanChiPillar tetBased = CanChi.yearPillar(lunar.year());
        uncertainties.add(Uncertainty.of(UncertaintyKind.METHODOLOGY_UNRESOLVED,
                "Ngày sinh nằm trong khoảng giữa Tết và Lập Xuân, là quãng thời gian hai quy "
                        + "ước cho ra Trụ Năm khác nhau. Theo Lập Xuân (quy ước engine này "
                        + "dùng): " + lapXuanBased.stem() + " " + lapXuanBased.branch()
                        + ". Theo Tết: " + tetBased.stem() + " " + tetBased.branch()
                        + ". Hệ thống không tự chọn giúp bạn.",
                "R18"));
        warnings.add(EngineWarning.critical("BAZI_YEAR_BOUNDARY_SCHOOLS_DISAGREE",
                "Trụ Năm phụ thuộc quy ước Lập Xuân hay Tết, và hai quy ước cho kết quả khác "
                        + "nhau trong trường hợp này (R18)."));
    }

    private static BaziPillar pillar(PillarPosition position, CanChiPillar canChi,
                                     HeavenlyStem dayMaster) {
        HiddenStems.HiddenStemSet hidden = HiddenStems.of(canChi.branch());

        TenGod stemTenGod = null;
        List<TenGod> hiddenTenGods = List.of();
        if (dayMaster != null) {
            // The Day Master has no Thap Than relative to itself: the day stem
            // slot is the reference point, not a role. Reporting TY_KIEN there
            // would be arithmetically true and conventionally wrong.
            if (position != PillarPosition.DAY) {
                stemTenGod = TenGods.of(dayMaster, canChi.stem());
            }
            hiddenTenGods = hidden.all().stream()
                    .map(stem -> TenGods.of(dayMaster, stem))
                    .toList();
        }

        return new BaziPillar(position, canChi.stem(), canChi.branch(), hidden,
                stemTenGod, hiddenTenGods);
    }

    private static ElementTally tally(BaziPillar... pillars) {
        ElementTally.Builder builder = ElementTally.builder();
        for (BaziPillar pillar : pillars) {
            if (pillar == null) {
                continue;
            }
            builder.addStem(pillar.stemElement());
            builder.addBranch(pillar.branchElement());
            for (HeavenlyStem hidden : pillar.hiddenStems().all()) {
                builder.addHiddenStem(hidden.element());
            }
        }
        return builder.build();
    }

    private static List<Evidence> buildEvidence(BaziChart chart) {
        List<Evidence> evidence = new ArrayList<>();
        String groupId = "BAZI_CHART";

        for (BaziPillar pillar : chart.pillars()) {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("position", pillar.position().name());
            fact.put("stem", pillar.stem().name());
            fact.put("branch", pillar.branch().name());
            fact.put("stemElement", pillar.stemElement().name());
            fact.put("stemPolarity", pillar.stemPolarity().name());
            fact.put("branchElement", pillar.branchElement().name());
            fact.put("hiddenStems", pillar.hiddenStems().all().stream().map(Enum::name).toList());
            fact.put("hiddenStemRoleOrderingDisputed",
                    pillar.hiddenStems().roleOrderingDisputed());
            if (pillar.stemTenGod() != null) {
                fact.put("stemTenGod", pillar.stemTenGod().name());
            }
            if (!pillar.hiddenStemTenGods().isEmpty()) {
                fact.put("hiddenStemTenGods",
                        pillar.hiddenStemTenGods().stream().map(Enum::name).toList());
            }
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "BAZI_PILLAR_" + pillar.position().name(), RULE_VERSION,
                    Dimension.OTHER, fact, "tu-binh-pillar-construction", groupId, null));
        }

        Map<String, Object> boundaryFact = new LinkedHashMap<>();
        boundaryFact.put("yearBoundary", chart.yearBoundary().name());
        boundaryFact.put("baziYear", chart.baziYear());
        boundaryFact.put("solarTermAtBirth", chart.solarTermAtBirth().name());
        boundaryFact.put("solarMonthIndex", chart.solarMonthIndex());
        boundaryFact.put("solarMonthBranch", chart.solarMonthBranch().name());
        boundaryFact.put("localSolarDateTime", chart.localSolarDateTime().toString());
        boundaryFact.put("hasHourPrecision", chart.hasHourPrecision());
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "BAZI_BOUNDARY", RULE_VERSION, Dimension.TIMING, boundaryFact,
                "solar-term-boundary", groupId, null));

        Map<String, Object> tallyFact = new LinkedHashMap<>();
        tallyFact.put("stems", nameKeyed(chart.elementTally().stems()));
        tallyFact.put("branches", nameKeyed(chart.elementTally().branches()));
        tallyFact.put("hiddenStems", nameKeyed(chart.elementTally().hiddenStems()));
        // Stated in the data itself, not only in a Javadoc: whoever renders this
        // must not turn three counts into one verdict.
        tallyFact.put("note", "Số đếm thô theo từng nhóm. Không phải đánh giá cường độ Ngũ "
                + "Hành (R3) và không được cộng gộp thành một con số.");
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "BAZI_ELEMENT_TALLY", RULE_VERSION, Dimension.OTHER, tallyFact,
                "element-count", groupId, null));

        if (chart.luckCycles() != null) {
            LuckCycles cycles = chart.luckCycles();
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("direction", cycles.direction().name());
            fact.put("boundaryTerm", cycles.boundaryTerm().name());
            fact.put("boundaryInstant", cycles.boundaryInstant().toString());
            // The day count is what every Bát Tự text states, so it is what a
            // reader can check this against by hand. Publishing only the start
            // age would make the result unverifiable against any source.
            fact.put("distanceDays", cycles.distanceToBoundary().toDays());
            fact.put("distanceHours", cycles.distanceToBoundary().toHours() % 24);
            fact.put("startAgeYears", cycles.startAge().getYears());
            fact.put("startAgeMonths", cycles.startAge().getMonths());
            fact.put("startAgeDays", cycles.startAge().getDays());
            fact.put("startDate", cycles.startDate().toString());
            fact.put("pillars", cycles.pillars().stream().map(pillar -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("ordinal", pillar.ordinal());
                entry.put("stem", pillar.stem().name());
                entry.put("branch", pillar.branch().name());
                entry.put("startAgeYears", pillar.startAgeYears());
                entry.put("startDate", pillar.startDate().toString());
                return entry;
            }).toList());
            fact.put("note", "Chuỗi vận và tuổi khởi vận là dữ liệu lập được. Việc một vận "
                    + "tốt hay xấu cần Dụng Thần (R1) và cường độ Nhật Chủ (R3), đều chưa "
                    + "có trường phái được chọn.");
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "BAZI_LUCK_CYCLES", RULE_VERSION, Dimension.TIMING, fact,
                    "dai-van-construction", groupId, null));
        }

        if (chart.dayMasterStrength() != null) {
            DayMasterStrength strength = chart.dayMasterStrength();
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("vuong", strength.vuong());
            fact.put("elementDegrees", nameKeyed(strength.elementDegrees()));
            fact.put("ownSideDegrees", strength.ownSideDegrees());
            fact.put("totalDegrees", strength.totalDegrees());
            fact.put("seasonalElement", strength.seasonalElement().name());
            fact.put("note", "Kết quả theo phương pháp tính điểm của Thiệu Vĩ Hoa — một "
                    + "trường phái cụ thể, giả định lá số thuộc dạng bình thường (không phải "
                    + "cách cục đặc biệt). Không phải sự đồng thuận chung giữa các trường phái "
                    + "Bát Tự (xem mục Cường độ Nhật Chủ trong danh sách bị chặn).");
            // A DIFFERENT school string than ENGINE_ID's SCHOOL, on purpose - this
            // is Thieu Vi Hoa's own named method, not a Tu Binh chart-construction
            // fact (Rule D decision #4, DECISION_LOG.md).
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID,
                    DAY_MASTER_STRENGTH_SCHOOL, "BAZI_DAY_MASTER_STRENGTH",
                    DayMasterStrengthResolver.RULE_VERSION, Dimension.OTHER, fact,
                    "day-master-strength-tvh", groupId, null));
        }

        for (BlockedSection blocked : chart.blockedSections()) {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("sectionId", blocked.sectionId());
            fact.put("displayNameVi", blocked.displayNameVi());
            fact.put("researchId", blocked.researchId());
            fact.put("reasonVi", blocked.reasonVi());
            fact.put("knownVariants", blocked.knownVariants());
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "BAZI_BLOCKED_" + blocked.sectionId(), RULE_VERSION,
                    Dimension.OTHER, fact, "research-blocker", groupId, null));
        }

        return List.copyOf(evidence);
    }

    private static Map<String, Integer> nameKeyed(Map<FiveElement, Integer> source) {
        Map<String, Integer> named = new LinkedHashMap<>();
        source.forEach((element, count) -> named.put(element.name(), count));
        return Map.copyOf(named);
    }

    @Override
    public ValidationResult validateInput(BaziInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "Bát Tự input is required.", ENGINE_ID);
        }
        LocalDate utcDate = input.utcInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        if (!CAPABILITY.supportedDateRange().covers(utcDate)) {
            return ValidationResult.failed("OUTSIDE_SUPPORTED_RANGE",
                    "Birth date " + utcDate + " is outside the supported range "
                            + CAPABILITY.supportedDateRange().describe()
                            + "; extrapolating past a sourced dataset is forbidden "
                            + "(CLAUDE.md Rule C).", ENGINE_ID);
        }
        if (input.longitudeDegreesIfKnown() != null) {
            double longitude = input.longitudeDegreesIfKnown();
            if (longitude < -180 || longitude > 180) {
                return ValidationResult.failed("INVALID_LONGITUDE",
                        "Longitude must be within [-180, 180]; got " + longitude + ".",
                        ENGINE_ID);
            }
        }
        return ValidationResult.ok();
    }

    @Override
    public EngineCapability capability() {
        return CAPABILITY;
    }

    @Override
    public EngineMetadata metadata() {
        return METADATA;
    }
}
