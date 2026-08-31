package io.destinyos.engines.iching;

import io.destinyos.calendar.CanChi;
import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.HourBranchResolver;
import io.destinyos.calendar.LunarDate;
import io.destinyos.calendar.LunarCalendar;
import io.destinyos.calendar.ZiHourBoundaryPolicy;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.EngineWarning;
import io.destinyos.core.result.ResearchReference;
import io.destinyos.core.signal.Dimension;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.SupportedDateRange;
import io.destinyos.engine.ValidationResult;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Kinh Dịch (I Ching) hexagram casting — research item R12, resolved
 * 2026-08-24 for the mechanical layer after Opus verification found no error
 * in the Sonnet-gathered research and independently derived the Yarrow
 * method's own probability distribution from first principles.
 *
 * <p><strong>Four casting methods, a real methodological choice, never
 * defaulted silently (Rule D)</strong> — see {@link CastingMethod}. Three
 * Coins and Yarrow are fully primary-sourced and unblocked outright: both
 * determine each line's old/young status directly from that line's own
 * drawn value, never via the mod-6 division the two Mai Hoa methods use. The
 * two Mai Hoa methods needed one further decision before they could ship —
 * see below.
 *
 * <p><strong>One declared convention, not a classical citation (Rule D,
 * {@code docs/DECISION_LOG.md}, 2026-08-24).</strong> Mai Hoa's own text
 * (梅花易數 卷一) is silent about what happens when the moving-line total
 * divides evenly by 6 — a case that arises in roughly one casting in six for
 * the Year-Month-Day-Hour method, not a rare corner. This project decided
 * remainder 0 → line 6, by analogy with the same text's own explicit rule
 * for the *other* division it defines (dividing by 8 with no remainder is
 * stated outright to mean 8, not 0) and a second independent precedent for
 * the same convention in the Yarrow method. See {@link MaiHoaCasting}'s
 * Javadoc and the decision log entry for what this does and does not commit
 * to.
 *
 * <p><strong>Quẻ từ (卦辭) và hào từ (爻辭) — R24/R25, added 2026-08-31.</strong>
 * The interpretive text itself is now real content, not a stub: all 64
 * hexagrams' judgment text and all 384 (+2 classical "dụng cửu"/"dụng lục")
 * line texts, sourced from Ngô Tất Tố's "Kinh Dịch Trọn Bộ" (NXB Văn Học),
 * supplied by the project owner and extracted with page citations — see
 * {@link HexagramJudgment}/{@link LineJudgment} and
 * {@code docs/research_drafts/R24_iching_hexagram_judgments.md}. Only
 * hexagrams 1-8's Chinese text has an independent second-source cross-check
 * (the R24 pilot, pre-dating the full book); none of this content has had
 * the project's standard Opus verification pass yet — treat it as real but
 * not yet fully settled.
 *
 * <p><strong>Deliberately not shipped in this version</strong> (ADR D7 —
 * reported as a {@link BlockedSection}, not silently absent):
 * <ul>
 *   <li>which line's text to treat as "the" reading when several lines move
 *       at once — Chu Hi's seven-rule scheme (易學啟蒙) found in R12 §7 has
 *       low source confidence (one un-fetched secondary summary) and is not
 *       applied here; this engine instead surfaces every moving line's text
 *       and lets the AI Narrative layer (Rule B) present them together,
 *       rather than silently picking one;</li>
 *   <li>any cát/hung (favourable/unfavourable) polarity extracted from the
 *       judgment or line text — the classical text sometimes states this
 *       outright (吉/凶/无咎…) and sometimes does not, and no rule for
 *       reading it computably has been researched, so this stays evidence
 *       only, never a {@link io.destinyos.core.signal.Signal};</li>
 *   <li>splitting a single multi-digit number into upper and lower trigrams
 *       for {@link CastingMethod#MAI_HOA_NUMBER} — this rule was found only
 *       in secondary sources with two-source consensus, and the primary text
 *       fetched for this project does not contain it (confirmed by direct
 *       re-fetch, see {@code VERIFICATION_OPUS_R12.md} §D2). This version's
 *       Number method requires the caller to supply two numbers directly,
 *       which the primary text does describe.</li>
 * </ul>
 *
 * <p>Emits no signals, for the same reason Bát Tự's chart half and Western
 * astrology's chart half do not: a signal needs a resolved favourable/
 * unfavourable judgement, which is exactly what the blocked section above
 * withholds — real judgment/line text is not the same thing as a resolved
 * polarity.
 */
public final class IChingEngine implements MetaphysicalEngine<IChingCastInput, IChingReading> {

    public static final String ENGINE_ID = "ICHING";
    public static final String METHODOLOGY_ID = "ICHING_HEXAGRAM_CASTING";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL = "Kinh Dịch — gieo quẻ (Tam Tiền, Thi Thảo, Mai Hoa Dịch Số)";

    /** R24/R25's own methodology (Rule D) — a named translator's text, not this engine's casting rule. */
    public static final String JUDGMENT_METHODOLOGY_ID = "ICHING_HEXAGRAM_JUDGMENT_NGOTATTO";
    public static final String JUDGMENT_RULE_VERSION = "1.0";
    public static final String JUDGMENT_SCHOOL =
            "Ngô Tất Tố (dịch và chú giải) — \"Kinh Dịch Trọn Bộ\", NXB Văn Học";
    public static final String JUDGMENT_SOURCE =
            "Bản số hóa khoahoctamlinh.vn, do chủ dự án cung cấp 2026-08-31. Ngô Tất Tố mất "
                    + "1954 nên bản dịch đã vào phạm vi công cộng tại Việt Nam từ 2005 (Điều 27 "
                    + "Luật SHTT). Trích xuất 64 quẻ từ + 386 hào từ (gồm Dụng Cửu/Dụng Lục) "
                    + "kèm trang trích dẫn; Hán văn của 8 quẻ đầu đã đối chiếu ≥2 nguồn độc lập "
                    + "(zh.wikisource.org, ctext.org) từ đợt thí điểm R24, 56 quẻ còn lại lấy "
                    + "trực tiếp từ đúng một cuốn sách này, CHƯA đối chiếu nguồn thứ hai. Chi "
                    + "tiết: docs/research_drafts/R24_iching_hexagram_judgments.md. CHƯA qua "
                    + "xác minh Opus.";

    public static final String SOURCE =
            "8 quẻ đơn và số Tiên Thiên: 說卦傳 (Thuyết Quái truyện) ch.3/5, cross-checked "
                    + "across independent hosts. Bảng 64 quẻ Văn Vương: thứ tự King Wen, kiểm "
                    + "chứng bằng 3 phép độc lập (song ánh, quy tắc cặp 綜卦/錯卦, quy ước đặt "
                    + "tên). Tam Tiền: quy ước 3/2, phân phối 1:3:3:1 tự suy ra bằng tổ hợp "
                    + "học. Thi Thảo: 繫辭上傳 (Hệ Từ), quy trình 18 biến; phân phối 1:5:7:3 "
                    + "tự suy ra từ chính quy trình. Mai Hoa: 梅花易數 卷一, trích nguyên văn "
                    + "trực tiếp cho quy tắc chia-8 và năm-tháng-ngày-giờ. Quy ước dư-0-chia-6: "
                    + "quyết định Rule D, docs/DECISION_LOG.md 2026-08-24, không phải trích "
                    + "dẫn cổ văn trực tiếp. Chi tiết đầy đủ và toàn bộ phép kiểm chứng: "
                    + "docs/research_drafts/R12_iching_maihoa.md và VERIFICATION_OPUS_R12.md.";

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Kinh Dịch — Gieo quẻ",
            METHODOLOGY_ID,
            METHODOLOGY_VERSION,
            RULE_VERSION,
            SCHOOL,
            SOURCE,
            MethodologyStatus.CONTENT_REQUIRED
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            .dimensions(Dimension.CAREER, Dimension.FINANCE, Dimension.RELATIONSHIP,
                    Dimension.DECISION, Dimension.OTHER)
            .requiresBirthTime(false)
            .requiresLocation(false)
            .requiresName(false)
            .requiresCalendar(false)
            .deterministic(true)
            .requiresSeed(true)
            .supportedDateRange(SupportedDateRange.unbounded())
            .build();

    private static final List<BlockedSection> BLOCKED_SECTIONS = List.of(
            new BlockedSection("LINE_SELECTION_RULE",
                    "Chọn lời hào/lời quẻ nào làm 'lời đoán chính' khi nhiều hào động", "R12",
                    "Quẻ từ và hào từ nay đã có nội dung thật (R24/R25), nhưng việc CHỌN một "
                            + "lời làm câu trả lời chính khi có từ 2 hào động trở lên vẫn thuộc "
                            + "tầng diễn giải/nghĩa lý. Bộ quy tắc của Chu Hy (Dịch Học Khải "
                            + "Mông) tìm được có độ tin cậy thấp (một tóm tắt thứ cấp, chưa fetch "
                            + "trực tiếp văn bản gốc). Engine trả về TẤT CẢ lời hào động, không "
                            + "tự chọn một lời làm chính.",
                    List.of("1 hào động: đọc lời hào đó",
                            "2 hào động: đọc lời hào cao hơn làm chính",
                            "3 hào động: đọc lời quẻ gốc và quẻ biến",
                            "4-6 hào động: quy tắc riêng theo Dịch Học Khải Mông (nguồn chưa xác minh đủ)")
            ),
            new BlockedSection("CAT_HUNG_POLARITY",
                    "Suy ra tốt/xấu (cát/hung) từ lời quẻ/lời hào", "R24",
                    "Cổ văn đôi khi nêu thẳng cát/hung/vô cữu/hối/lận, đôi khi không — nhưng "
                            + "chưa có quy tắc đã nghiên cứu để suy ra một cực tính (Polarity) "
                            + "máy tính được cho mọi trường hợp. Vì vậy quẻ từ/hào từ chỉ là "
                            + "Evidence, không phát sinh Signal nào cho Fusion.",
                    List.of("Chỉ dùng các từ khóa tường minh (吉/凶/无咎…), bỏ qua trường hợp mơ hồ",
                            "Suy luận theo truyền thống chú giải (Trình Di, Chu Hy) — cần chọn trường phái")
            )
    );

    private static final ZiHourBoundaryPolicy HOUR_POLICY = ZiHourBoundaryPolicy.ZI_HOUR_23_00;

    @Override
    public EngineResult<IChingReading> calculate(IChingCastInput input, CalculationContext context) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(context, "context");

        IChingReading reading = switch (input.method()) {
            case THREE_COINS -> castFromLines(input, CastingMethod.THREE_COINS, ThreeCoinsCasting::cast);
            case YARROW -> castFromLines(input, CastingMethod.YARROW, YarrowCasting::cast);
            case MAI_HOA_NUMBER -> castFromNumbers(input);
            case MAI_HOA_TIME -> castFromDateTime(input, context);
        };

        List<EngineWarning> warnings = new ArrayList<>();
        for (BlockedSection blocked : BLOCKED_SECTIONS) {
            warnings.add(EngineWarning.critical(
                    "ICHING_SECTION_BLOCKED_" + blocked.sectionId(),
                    blocked.displayNameVi() + ": " + blocked.reasonVi()));
        }

        return new EngineResult<>(
                EngineStatus.PARTIAL,
                reading,
                buildEvidence(reading),
                List.of(),
                warnings,
                List.of(),
                new ResearchReference("R12", "Kinh Dịch",
                        "Quẻ đã dựng và quẻ từ/hào từ đã có nội dung thật (R24/R25). Chưa có "
                                + "quy tắc chọn lời chính khi nhiều hào động, và chưa suy ra được "
                                + "cực tính cát/hung máy tính được, nên engine không phát sinh "
                                + "tín hiệu nào cho Fusion.",
                        "docs/RESEARCH_BLOCKERS.md R12; docs/research_drafts/R24_iching_hexagram_judgments.md",
                        List.of("Quy tắc chọn lời hào chính (Chu Hy)", "Suy ra cát/hung máy tính được")),
                Map.of("methodologyId", METHODOLOGY_ID, "judgmentMethodologyId", JUDGMENT_METHODOLOGY_ID));
    }

    private interface LineCaster {
        List<LineValue> cast(Random random);
    }

    private static IChingReading castFromLines(IChingCastInput input, CastingMethod method, LineCaster caster) {
        long seed = input.seedIfPresent().orElseGet(IChingEngine::generateSeed);
        Random random = new Random(seed);
        List<LineValue> lines = caster.cast(random);

        return new IChingReading(
                method,
                HexagramLines.original(lines),
                HexagramLines.changed(lines),
                HexagramLines.movingPositions(lines),
                lines,
                seed);
    }

    private static IChingReading castFromNumbers(IChingCastInput input) {
        if (input.upperNumber() == null || input.lowerNumber() == null) {
            throw new IllegalArgumentException(
                    "MAI_HOA_NUMBER requires both upperNumber and lowerNumber");
        }
        MaiHoaCasting.MaiHoaCast cast = MaiHoaCasting.fromNumbers(input.upperNumber(), input.lowerNumber());
        return fromMaiHoaCast(CastingMethod.MAI_HOA_NUMBER, cast);
    }

    private static IChingReading castFromDateTime(IChingCastInput input, CalculationContext context) {
        Instant instant = input.instantIfPresent().orElse(context.calculatedAt());
        ZoneOffset offset = context.timezone().getRules().getOffset(instant);
        double timezoneOffsetHours = offset.getTotalSeconds() / 3600.0;

        LocalDate localDate = instant.atOffset(offset).toLocalDate();
        LocalTime localTime = instant.atOffset(offset).toLocalTime();

        LunarDate lunar = LunarCalendar.toLunar(
                localDate.getDayOfMonth(), localDate.getMonthValue(), localDate.getYear(), timezoneOffsetHours);
        EarthlyBranch yearBranch = CanChi.yearPillar(lunar.year()).branch();
        EarthlyBranch hourBranch = HourBranchResolver.branchAt(localTime, HOUR_POLICY);

        MaiHoaCasting.MaiHoaCast cast = MaiHoaCasting.fromDateTime(
                yearBranch.index(), lunar.month(), lunar.day(), hourBranch.index());
        return fromMaiHoaCast(CastingMethod.MAI_HOA_TIME, cast);
    }

    private static IChingReading fromMaiHoaCast(CastingMethod method, MaiHoaCasting.MaiHoaCast cast) {
        Hexagram original = HexagramTable.of(cast.upper(), cast.lower());
        Hexagram changed = HexagramLines.flipOneLine(original, cast.movingLinePosition());
        return new IChingReading(method, original, changed, List.of(cast.movingLinePosition()), List.of(), null);
    }

    private static List<Evidence> buildEvidence(IChingReading reading) {
        List<Evidence> evidence = new ArrayList<>();
        String groupId = UUID.randomUUID().toString();

        Map<String, Object> castFact = new LinkedHashMap<>();
        castFact.put("method", reading.method().name());
        if (reading.seed() != null) {
            castFact.put("seed", reading.seed());
        }
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ICHING_CAST", RULE_VERSION, Dimension.OTHER, castFact, "hexagram-casting", groupId, null));

        evidence.add(hexagramEvidence("ICHING_ORIGINAL_HEXAGRAM", reading.originalHexagram(), groupId));
        if (reading.changedHexagram() != null) {
            evidence.add(hexagramEvidence("ICHING_CHANGED_HEXAGRAM", reading.changedHexagram(), groupId));
        }

        judgmentEvidence("ICHING_JUDGMENT_ORIGINAL", reading.originalHexagram(), groupId)
                .ifPresent(evidence::add);
        if (reading.changedHexagram() != null) {
            judgmentEvidence("ICHING_JUDGMENT_CHANGED", reading.changedHexagram(), groupId)
                    .ifPresent(evidence::add);
        }

        int hexagramNumber = reading.originalHexagram().number();
        if (reading.movingLinePositions().size() == 6 && (hexagramNumber == 1 || hexagramNumber == 2)) {
            // The classical special case (R12): all six lines moving in a
            // pure Kiền or Khôn hexagram reads "dụng cửu"/"dụng lục" (用九/
            // 用六) instead of any of the six ordinary line texts.
            lineJudgmentEvidence(hexagramNumber, 0, "ICHING_LINE_JUDGMENT_DUNG", groupId)
                    .ifPresent(evidence::add);
        } else {
            for (int position : reading.movingLinePositions()) {
                lineJudgmentEvidence(hexagramNumber, position,
                        "ICHING_LINE_JUDGMENT_" + position, groupId).ifPresent(evidence::add);
            }
        }

        Map<String, Object> movingFact = new LinkedHashMap<>();
        movingFact.put("positions", reading.movingLinePositions());
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "ICHING_MOVING_LINES", RULE_VERSION, Dimension.OTHER, movingFact,
                "moving-line-rule", groupId, null));

        if (!reading.lines().isEmpty()) {
            Map<String, Object> linesFact = new LinkedHashMap<>();
            List<String> lineNames = new ArrayList<>();
            for (LineValue line : reading.lines()) {
                lineNames.add(line.name());
            }
            linesFact.put("lines", lineNames);
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "ICHING_DRAWN_LINES", RULE_VERSION, Dimension.OTHER, linesFact,
                    "line-by-line-draw", groupId, null));
        }

        for (BlockedSection blocked : BLOCKED_SECTIONS) {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("sectionId", blocked.sectionId());
            fact.put("displayNameVi", blocked.displayNameVi());
            fact.put("researchId", blocked.researchId());
            fact.put("reasonVi", blocked.reasonVi());
            fact.put("knownVariants", blocked.knownVariants());
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "ICHING_BLOCKED_" + blocked.sectionId(), RULE_VERSION,
                    Dimension.OTHER, fact, "research-blocker", groupId, null));
        }

        return List.copyOf(evidence);
    }

    /** Quẻ từ (卦辭) evidence for one hexagram, or empty if R24's table has no entry for it. */
    private static Optional<Evidence> judgmentEvidence(String ruleId, Hexagram hexagram, String groupId) {
        return HexagramJudgmentTable.byNumber(hexagram.number()).map(judgment -> {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("number", judgment.number());
            fact.put("hanTu", judgment.hanTu());
            fact.put("hanViet", judgment.hanViet());
            fact.put("nghia", judgment.nghia());
            fact.put("sourcePage", judgment.sourcePage());
            fact.put("hanTuCrossChecked", judgment.hanTuCrossChecked());
            judgment.noteIfPresent().ifPresent(note -> fact.put("note", note));
            return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, JUDGMENT_SCHOOL, ruleId,
                    JUDGMENT_RULE_VERSION, Dimension.OTHER, fact, "ngo-tat-to-kinh-dich-tron-bo",
                    groupId, null);
        });
    }

    /** Hào từ (爻辭) evidence for one line, or empty if R25's table has no entry for it. */
    private static Optional<Evidence> lineJudgmentEvidence(int hexagramNumber, int position,
                                                           String ruleId, String groupId) {
        Optional<LineJudgment> lineJudgment = position == 0
                ? LineJudgmentTable.dungLine(hexagramNumber)
                : LineJudgmentTable.at(hexagramNumber, position);
        return lineJudgment.map(judgment -> {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("hexagramNumber", judgment.hexagramNumber());
            fact.put("position", judgment.position());
            fact.put("label", judgment.label());
            fact.put("hanTu", judgment.hanTu());
            fact.put("hanViet", judgment.hanViet());
            fact.put("nghia", judgment.nghia());
            if (judgment.sourcePage() != null) {
                fact.put("sourcePage", judgment.sourcePage());
            }
            judgment.noteIfPresent().ifPresent(note -> fact.put("note", note));
            return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, JUDGMENT_SCHOOL, ruleId,
                    JUDGMENT_RULE_VERSION, Dimension.OTHER, fact, "ngo-tat-to-kinh-dich-tron-bo",
                    groupId, null);
        });
    }

    private static Evidence hexagramEvidence(String ruleId, Hexagram hexagram, String groupId) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("number", hexagram.number());
        fact.put("upperTrigram", hexagram.upper().name());
        fact.put("lowerTrigram", hexagram.lower().name());
        return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL, ruleId, RULE_VERSION,
                Dimension.OTHER, fact, "hexagram-table", groupId, null);
    }

    private static long generateSeed() {
        return new SecureRandom().nextLong();
    }

    @Override
    public ValidationResult validateInput(IChingCastInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "I Ching casting input is required.", ENGINE_ID);
        }
        if (input.method() == CastingMethod.MAI_HOA_NUMBER
                && (input.upperNumber() == null || input.lowerNumber() == null)) {
            return ValidationResult.failed("MISSING_NUMBERS",
                    "MAI_HOA_NUMBER requires both upperNumber and lowerNumber.", ENGINE_ID);
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
