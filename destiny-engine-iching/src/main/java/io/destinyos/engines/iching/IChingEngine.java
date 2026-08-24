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
 * <p><strong>Deliberately not shipped in this version</strong> (ADR D7 —
 * reported as a {@link BlockedSection}, not silently absent):
 * <ul>
 *   <li>which line's text (or the hexagram's own judgment text) to read when
 *       several lines move at once — this is the interpretation layer
 *       (Rule B), not chart construction, and no line/judgment text has been
 *       authored yet regardless (mirrors Tarot before its R11 meaning corpus,
 *       and Bát Tự's still-open Dụng Thần);</li>
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
 * astrology's chart half do not: a signal needs interpretive
 * favourable/unfavourable content, which is exactly what the blocked section
 * above withholds.
 */
public final class IChingEngine implements MetaphysicalEngine<IChingCastInput, IChingReading> {

    public static final String ENGINE_ID = "ICHING";
    public static final String METHODOLOGY_ID = "ICHING_HEXAGRAM_CASTING";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL = "Kinh Dịch — gieo quẻ (Tam Tiền, Thi Thảo, Mai Hoa Dịch Số)";

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
            new BlockedSection("LINE_JUDGMENT_TEXT",
                    "Lời đoán theo hào động / quẻ", "R12",
                    "Việc đọc lời hào (爻辭) hay lời quẻ (卦辭) nào khi có nhiều hào động "
                            + "thuộc tầng diễn giải/nghĩa lý, không phải tầng dựng quẻ. Nội "
                            + "dung lời hào/lời quẻ cho 64 quẻ chưa được biên soạn.",
                    List.of("1 hào động: đọc lời hào đó",
                            "2 hào động: đọc lời hào cao hơn làm chính",
                            "3 hào động: đọc lời quẻ gốc và quẻ biến",
                            "4-6 hào động: quy tắc riêng theo Dịch Học Khải Mông (nguồn chưa xác minh đủ)")
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

        return EngineResult.partial(reading, buildEvidence(reading), List.of(), warnings);
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
