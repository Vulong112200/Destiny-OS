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
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
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
 * {@code docs/research_drafts/R24_iching_hexagram_judgments.md}. This content
 * <strong>has now had the project's standard Opus verification pass</strong>
 * (2026-09-01, {@code VERIFICATION_OPUS_R24.md}), which rejected the book's
 * own Chinese text outright — 34/64 quẻ từ and 280/386 hào từ differed from
 * the classical text through OCR damage — and replaced it with
 * {@code zh.wikisource.org} raw wikitext while keeping Ngô Tất Tố's
 * translation untouched. Only hexagrams 1-8's Chinese has an independent
 * second-source cross-check (the R24 pilot); the remaining 56 rest on the
 * single wikisource edition, so a ctext.org cross-check is still owed.
 *
 * <p><strong>27 extraction defects repaired, 2026-09-01.</strong> A follow-up
 * audit found the shipped text was complete in <em>count</em> but wrong in
 * <em>content</em> for 27 entries: 26 had the book's own GIẢI NGHĨA
 * commentary run into the {@code nghia} field with no separator (hexagram 1
 * line 1 held ~2,000 characters where the translation is 32), and one held
 * Tượng truyện text instead of the line's own gloss. Every repair carries a
 * {@code note} saying what was cut, and {@code HexagramJudgmentTableTest} now
 * asserts the absence of those markers — the earlier suite passed throughout
 * because it only ever asserted the field was non-blank.
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
 *   <li>splitting a single multi-digit number into upper and lower trigrams
 *       for {@link CastingMethod#MAI_HOA_NUMBER} — this rule was found only
 *       in secondary sources with two-source consensus, and the primary text
 *       fetched for this project does not contain it (confirmed by direct
 *       re-fetch, see {@code VERIFICATION_OPUS_R12.md} §D2). This version's
 *       Number method requires the caller to supply two numbers directly,
 *       which the primary text does describe.</li>
 * </ul>
 *
 * <p><strong>Now emits Signals — {@code CAT_HUNG_POLARITY} closed
 * 2026-09-01.</strong> Until this date the engine returned an empty signal
 * list for the same reason Bát Tự's chart half still does: a Signal needs a
 * resolved favourable/unfavourable judgement, and real judgment text is not
 * the same thing as a resolved polarity. What closed it was not a new formula
 * but a <em>reading</em>: {@link CatHungLexicon} scans the Chinese text that
 * was already shipped for the judgment vocabulary physically present in it
 * (吉/凶/悔/吝/无咎), with the five valences taken verbatim from Nguyễn Hiến
 * Lê's own glossary (<em>Kinh Dịch — Đạo Của Người Quân Tử</em>, NXB Văn Học,
 * tr.92). 65% of the shipped texts carry such a term; the other 35% report
 * NEUTRAL, which is the text declining to pronounce rather than a gap.
 *
 * <p>The position-based alternative — đắc trung / đắc chính — was examined
 * and <em>rejected</em>, because that same source disowns it on tr.101 and
 * supplies the counterexamples itself. {@link CatHungLexicon} records the
 * reasoning; the short version is that a rule its own source publishes
 * counterexamples to is what Rule C forbids.
 *
 * <p><strong>Hào làm chủ (the governing line) — new, Evidence only.</strong>
 * {@link HaoLamChu} computes it from the classical 眾以寡為主 rule, which is
 * fully determined by the hexagram's structure. It emits no Signal on
 * purpose: the source states twice, on tr.102 and again on tr.103, that being
 * the governing line says nothing whatever about good or bad.
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
            "Hán văn: 周易 (zh.wikisource.org); dịch âm/dịch nghĩa: Ngô Tất Tố — "
                    + "\"Kinh Dịch Trọn Bộ\", NXB Văn Học";
    public static final String JUDGMENT_SOURCE =
            "Hai nguồn cho hai tầng khác nhau (Rule D). (1) HÁN VĂN 卦辭/爻辭 lấy từ "
                    + "zh.wikisource.org/wiki/周易/*, đọc bằng wikitext thô (action=raw) để "
                    + "không có model tóm tắt nào chuẩn hóa ký tự — cổ văn là văn bản chung của "
                    + "mọi ấn bản, không phải lựa chọn dịch thuật, nên chọn bản có chứng cứ tốt "
                    + "nhất. (2) DỊCH ÂM và DỊCH NGHĨA lấy từ Ngô Tất Tố (\"Kinh Dịch Trọn Bộ\", "
                    + "NXB Văn Học, bản số hóa khoahoctamlinh.vn, chủ dự án cung cấp "
                    + "2026-08-31) — đây MỚI là lựa chọn dịch thuật nên ghi rõ tên dịch giả và "
                    + "không sửa lời văn của ông. Ngô Tất Tố mất 1954, bản dịch vào phạm vi "
                    + "công cộng tại Việt Nam từ 2005 (Điều 27 Luật SHTT). Xác minh 2026-09-01: "
                    + "thứ tự chương của sách được xác nhận là thứ tự Văn Vương bằng cách đối "
                    + "chiếu quái tượng in trong sách với HexagramTable (59/60 khớp); toàn bộ "
                    + "386 nhãn hào được suy dẫn lại từ cấu trúc âm/dương của chính quẻ, bắt "
                    + "được 2 lỗi thật của sách (quẻ 45 hào 1, quẻ 51 hào 4); ghép cặp Việt-Hán "
                    + "kiểm bằng số âm tiết Hán-Việt so với số chữ cổ văn. Hán văn của chính "
                    + "cuốn sách bị loại vì sai khác cổ văn ở 34/64 quẻ từ và 280/386 hào từ "
                    + "(hỏng OCR, 15% ký tự là Kangxi Radical codepoint). Chi tiết: "
                    + "docs/research_drafts/R24_iching_hexagram_judgments.md.";

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
                    "Quẻ từ và hào từ nay đã có nội dung thật (R24/R25) và mỗi lời đã có cực "
                            + "tính cát/hung đọc được (CatHungLexicon), nhưng việc CHỌN một lời "
                            + "làm câu trả lời chính khi có từ 2 hào động trở lên vẫn chưa có "
                            + "nguồn. Bộ quy tắc của Chu Hy (Dịch Học Khải Mông) tìm được có độ "
                            + "tin cậy thấp (một tóm tắt thứ cấp, chưa fetch trực tiếp văn bản "
                            + "gốc). Đã kiểm thêm một nguồn nữa và nguồn đó KHÔNG đóng được mục "
                            + "này: Nguyễn Hiến Lê, \"Kinh Dịch — Đạo Của Người Quân Tử\", NXB "
                            + "Văn Học — tr.104 ông tự tuyên bố \"Đoạn này liên quan tới việc "
                            + "bói, chúng tôi không có ý khảo về môn bói, nên chỉ giảng qua "
                            + "thôi\"; tr.106-107 ông chỉ nói mọi hào động đổi CÙNG MỘT LƯỢT và "
                            + "cho ĐÚNG MỘT quẻ biến, đếm tới \"hai, ba hào cùng biến\" rồi dừng, "
                            + "không hề nhắc 4/5/6 hào động và không một chữ nào về quy tắc chọn "
                            + "lời chính. Ghi lại để vòng sau không mở lại nguồn này. Engine trả "
                            + "về TẤT CẢ lời hào động, không tự chọn một lời làm chính; và vì "
                            + "chưa biết lời nào là chính, Signal của các hào động bị hạ "
                            + "Applicability xuống MEDIUM khi có nhiều hơn một hào động.",
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

        Analysis analysis = analyse(reading);

        return new EngineResult<>(
                EngineStatus.PARTIAL,
                reading,
                analysis.evidence(),
                analysis.signals(),
                warnings,
                List.of(),
                new ResearchReference("R12", "Kinh Dịch",
                        "Quẻ đã dựng, quẻ từ/hào từ đã có nội dung thật (R24/R25), và cực tính "
                                + "cát/hung nay đọc được từ chính từ vựng phán định trong Hán văn "
                                + "(CatHungLexicon, nghĩa theo Nguyễn Hiến Lê tr.92) nên engine "
                                + "đã phát Signal vào Fusion. Vẫn còn treo: quy tắc chọn lời nào "
                                + "làm chính khi nhiều hào động.",
                        "docs/RESEARCH_BLOCKERS.md R12; docs/research_drafts/R24_iching_hexagram_judgments.md",
                        List.of("Quy tắc chọn lời hào chính (Chu Hy)")),
                Map.of("methodologyId", METHODOLOGY_ID,
                        "judgmentMethodologyId", JUDGMENT_METHODOLOGY_ID,
                        "catHungMethodologyId", CatHungLexicon.METHODOLOGY_ID,
                        "haoLamChuMethodologyId", HaoLamChu.METHODOLOGY_ID));
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

    /**
     * Evidence and Signals built together, because a Signal must name the
     * exact Evidence it was read off. Splitting the two passes would mean
     * re-deriving evidence ids or handing Fusion a Signal whose
     * {@code evidenceIds} point at nothing.
     */
    private record Analysis(List<Evidence> evidence, List<Signal> signals) {
    }

    private static Analysis analyse(IChingReading reading) {
        List<Evidence> evidence = new ArrayList<>();
        List<Signal> signals = new ArrayList<>();
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

        // The original hexagram's own 卦辭 always applies — it is the judgment
        // on the situation the cast produced, not one of several candidates —
        // so its polarity carries full Applicability.
        judgmentEvidence("ICHING_JUDGMENT_ORIGINAL", reading.originalHexagram(), groupId)
                .ifPresent(found -> {
                    evidence.add(found);
                    signals.addAll(catHungSignals(found, "QUE_GOC", Applicability.HIGH, groupId));
                });
        if (reading.changedHexagram() != null) {
            // The changed hexagram is where the situation is heading rather
            // than where it is, and no consulted source weighs the two against
            // each other, so it participates at reduced applicability.
            judgmentEvidence("ICHING_JUDGMENT_CHANGED", reading.changedHexagram(), groupId)
                    .ifPresent(found -> {
                        evidence.add(found);
                        signals.addAll(catHungSignals(found, "QUE_BIEN",
                                Applicability.MEDIUM, groupId));
                    });
        }

        int hexagramNumber = reading.originalHexagram().number();
        // With two or more moving lines, LINE_SELECTION_RULE is still open —
        // the engine cannot say which line's text is "the" answer. Every
        // moving line therefore participates, but at reduced applicability;
        // that is the open blocker being represented rather than an invented
        // weight, and it is stated in the blocked section's own reason.
        Applicability lineApplicability = reading.movingLinePositions().size() > 1
                ? Applicability.MEDIUM
                : Applicability.HIGH;
        if (reading.movingLinePositions().size() == 6 && (hexagramNumber == 1 || hexagramNumber == 2)) {
            // The classical special case (R12): all six lines moving in a
            // pure Kiền or Khôn hexagram reads "dụng cửu"/"dụng lục" (用九/
            // 用六) instead of any of the six ordinary line texts. Because it
            // replaces all six rather than competing with them, it is the
            // single applicable line text and gets full applicability.
            lineJudgmentEvidence(hexagramNumber, 0, "ICHING_LINE_JUDGMENT_DUNG", groupId)
                    .ifPresent(found -> {
                        evidence.add(found);
                        signals.addAll(catHungSignals(found, "HAO_DUNG",
                                Applicability.HIGH, groupId));
                    });
        } else {
            for (int position : reading.movingLinePositions()) {
                lineJudgmentEvidence(hexagramNumber, position,
                        "ICHING_LINE_JUDGMENT_" + position, groupId)
                        .ifPresent(found -> {
                            evidence.add(found);
                            signals.addAll(catHungSignals(found, "HAO_" + position,
                                    lineApplicability, groupId));
                        });
            }
        }

        haoLamChuEvidence(reading.originalHexagram(), groupId).ifPresent(evidence::add);

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

        return new Analysis(List.copyOf(evidence), List.copyOf(signals));
    }

    /**
     * Signals read off one judgment text's Chinese by {@link CatHungLexicon}.
     *
     * <p>Every term found becomes its own Signal. A text carrying both a
     * favourable and an unfavourable term therefore yields several Signals of
     * opposing polarity, deliberately: Rule E makes conflict a valid result,
     * and the existing consensus machinery is what should notice it. Averaging
     * them here would destroy the finding before Fusion ever saw it.
     *
     * <p>Nothing is marked {@code critical}, including 凶. The reason is the
     * still-open {@code LINE_SELECTION_RULE}: with several moving lines the
     * engine cannot assert that any one 凶 is <em>the</em> answer to the
     * question asked, and {@code critical} in this project means a signal that
     * should dominate. Escalating an unresolved candidate would be exactly the
     * confident-but-unverifiable output the blocked section exists to prevent.
     */
    private static List<Signal> catHungSignals(Evidence source, String tag,
                                               Applicability applicability, String groupId) {
        String hanTu = String.valueOf(source.fact().get("hanTu"));
        List<CatHungLexicon.Match> matches = CatHungLexicon.scan(hanTu);
        List<Signal> signals = new ArrayList<>();
        if (matches.isEmpty()) {
            // 35% of the shipped texts carry no judgment vocabulary at all.
            // That is a real reading — the text declines to pronounce — so it
            // is reported as NEUTRAL rather than left as a silent gap.
            signals.add(new Signal(UUID.randomUUID().toString(), ENGINE_ID, CatHungLexicon.SCHOOL,
                    Dimension.OTHER, "ICHING_CAT_HUNG_" + tag + "_KHONG_PHAN_DINH",
                    Polarity.NEUTRAL, Strength.WEAK, applicability, false,
                    List.of(source.evidenceId()), groupId));
            return signals;
        }
        for (CatHungLexicon.Match match : matches) {
            signals.add(new Signal(UUID.randomUUID().toString(), ENGINE_ID, CatHungLexicon.SCHOOL,
                    Dimension.OTHER, "ICHING_CAT_HUNG_" + tag + "_" + match.code(),
                    match.polarity(), match.strength(), applicability, false,
                    List.of(source.evidenceId()), groupId));
        }
        return signals;
    }

    /**
     * Hào làm chủ (the governing line) for the original hexagram, or empty
     * when the rule singles no line out.
     *
     * <p>Evidence only, never a Signal — see {@link HaoLamChu} for the two
     * passages in which the source states that being the governing line says
     * nothing about good or bad.
     */
    private static Optional<Evidence> haoLamChuEvidence(Hexagram hexagram, String groupId) {
        return HaoLamChu.of(hexagram).map(position -> {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("hexagramNumber", hexagram.number());
            fact.put("position", position);
            fact.put("isYang", HaoLamChu.isYang(hexagram, position));
            fact.put("rule", "眾以寡為主，多以少為尊 (chúng dĩ quả vi chủ, đa dĩ thiểu vi tôn)");
            fact.put("sourcePage", 101);
            fact.put("neutralityNoteVi", HaoLamChu.NEUTRALITY_NOTE_VI);
            if (HaoLamChu.isSourceNamedException(hexagram.number())) {
                fact.put("sourceNamedExceptionVi", HaoLamChu.EXCEPTION_NOTE_VI);
            }
            return new Evidence(UUID.randomUUID().toString(), ENGINE_ID, HaoLamChu.SCHOOL,
                    "ICHING_HAO_LAM_CHU", HaoLamChu.RULE_VERSION, Dimension.OTHER, fact,
                    "nguyen-hien-le-kinh-dich-dao-cua-nguoi-quan-tu", groupId, null);
        });
    }

    /** Quẻ từ (卦辭) evidence for one hexagram, or empty if R24's table has no entry for it. */
    private static Optional<Evidence> judgmentEvidence(String ruleId, Hexagram hexagram, String groupId) {
        return HexagramJudgmentTable.byNumber(hexagram.number()).map(judgment -> {
            Map<String, Object> fact = new LinkedHashMap<>();
            fact.put("number", judgment.number());
            fact.put("chineseName", judgment.chineseName());
            fact.put("hanTu", judgment.hanTu());
            fact.put("hanViet", judgment.hanViet());
            fact.put("nghia", judgment.nghia());
            fact.put("sourcePage", judgment.sourcePage());
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
