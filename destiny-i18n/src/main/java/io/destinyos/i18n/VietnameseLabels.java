package io.destinyos.i18n;

import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativeSource;
import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.SolarTerm;
import io.destinyos.calendar.YinYang;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.evidence.DataConfidence;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.retention.RetentionClass;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engines.bazi.BaziYearBoundary;
import io.destinyos.engines.bazi.PillarPosition;
import io.destinyos.engines.bazi.TenGod;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.DimensionState;
import io.destinyos.fusion.FusionOutcome;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Vietnamese labels for every enum that can reach a user
 * (UI_UX_VIETNAMESE_SPEC sections 1 and 6, CLAUDE.md section 9).
 *
 * <p>UI_UX_VIETNAMESE_SPEC section 1 forbids bare technical enums appearing
 * alone in the UI. A registry alone would not enforce that - someone adds an
 * enum constant, forgets the label, and a raw {@code MAJOR_CONFLICT} reaches a
 * user. {@code LabelCoverageTest} closes that gap by failing the build when any
 * constant lacks a label.
 *
 * <p>Technical names stay available for tooltips and technical-detail views,
 * which section 1 explicitly permits.
 *
 * <p>Deliberately absent: any percentage or probability wording. Magnitude uses
 * the vocabulary FUSION_ENGINE_SPEC section 11 prescribes - Yeu / Vua / Manh /
 * Dang chu y - never a number (ADR D6).
 */
public final class VietnameseLabels {

    private VietnameseLabels() {
    }

    private static final Map<EngineStatus, String> ENGINE_STATUS = ordered(map -> {
        map.put(EngineStatus.SUCCESS, "Thành công");
        map.put(EngineStatus.PARTIAL, "Một phần");
        map.put(EngineStatus.NOT_APPLICABLE, "Không áp dụng");
        map.put(EngineStatus.RESEARCH_REQUIRED, "Cần xác minh thuật toán");
        map.put(EngineStatus.NOT_IMPLEMENTED, "Chưa triển khai");
        map.put(EngineStatus.INVALID_INPUT, "Dữ liệu nhập chưa hợp lệ");
        map.put(EngineStatus.FAILED_RECOVERABLE, "Lỗi tạm thời");
        map.put(EngineStatus.FAILED_FATAL, "Lỗi nghiêm trọng");
    });

    private static final Map<Polarity, String> POLARITY = ordered(map -> {
        map.put(Polarity.SUPPORT, "Thuận lợi");
        map.put(Polarity.CAUTION, "Cần thận trọng");
        map.put(Polarity.NEGATIVE, "Không thuận lợi");
        map.put(Polarity.NEUTRAL, "Trung tính");
    });

    // FUSION_ENGINE_SPEC section 11: magnitude words, never percentages.
    private static final Map<Strength, String> STRENGTH = ordered(map -> {
        map.put(Strength.WEAK, "Yếu");
        map.put(Strength.MEDIUM, "Vừa");
        map.put(Strength.STRONG, "Mạnh");
    });

    private static final Map<Applicability, String> APPLICABILITY = ordered(map -> {
        map.put(Applicability.HIGH, "Rất liên quan");
        map.put(Applicability.MEDIUM, "Có liên quan");
        map.put(Applicability.LOW, "Ít liên quan");
        map.put(Applicability.NOT_APPLICABLE, "Không áp dụng");
    });

    private static final Map<Dimension, String> DIMENSION = ordered(map -> {
        map.put(Dimension.FINANCE, "Tài chính");
        map.put(Dimension.CAREER, "Sự nghiệp");
        map.put(Dimension.RELATIONSHIP, "Quan hệ");
        // CLAUDE.md section 10 forbids diagnosis. The label says reflection,
        // not health, so the UI never implies a medical reading.
        map.put(Dimension.HEALTH_REFLECTION, "Sức khỏe - góc nhìn tham khảo");
        map.put(Dimension.TIMING, "Thời điểm");
        map.put(Dimension.TRAVEL, "Di chuyển");
        map.put(Dimension.DECISION, "Quyết định");
        map.put(Dimension.HOME, "Nhà cửa");
        map.put(Dimension.DAILY, "Hằng ngày");
        map.put(Dimension.OTHER, "Khác");
    });

    private static final Map<DataConfidence, String> DATA_CONFIDENCE = ordered(map -> {
        map.put(DataConfidence.EXACT, "Dữ liệu chính xác");
        map.put(DataConfidence.APPROXIMATE, "Dữ liệu gần đúng");
        map.put(DataConfidence.BOUNDARY, "Sát ranh giới");
        map.put(DataConfidence.ASSUMED, "Dựa trên giả định");
    });

    /**
     * Uncertainty messages (ADR D3).
     *
     * <p>These are written for an ordinary reader, not a developer. A user
     * whose birth falls in an unresolved historical window needs to understand
     * that the result could change - that is the whole purpose of preserving
     * the uncertainty rather than resolving it silently.
     */
    private static final Map<UncertaintyKind, String> UNCERTAINTY = ordered(map -> {
        map.put(UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN,
                "Thời điểm sinh của bạn rơi vào giai đoạn lịch sử chưa xác minh được "
                        + "quy tắc múi giờ. Kết quả có thể thay đổi tùy theo quy tắc được áp dụng.");
        map.put(UncertaintyKind.BIRTH_REGION_UNKNOWN,
                "Chưa xác định được vùng sinh. Với một số giai đoạn lịch sử, quy tắc "
                        + "múi giờ khác nhau theo vùng nên kết quả có thể thay đổi.");
        map.put(UncertaintyKind.BIRTH_TIME_IMPRECISE,
                "Giờ sinh chưa chính xác. Một số kết quả phụ thuộc vào giờ sinh "
                        + "nên có thể thay đổi.");
        map.put(UncertaintyKind.SOLAR_TERM_BOUNDARY,
                "Thời điểm sinh nằm sát ranh giới tiết khí. Sai lệch nhỏ về giờ sinh "
                        + "có thể làm thay đổi kết quả.");
        map.put(UncertaintyKind.DAY_BOUNDARY,
                "Thời điểm sinh nằm sát ranh giới chuyển ngày. Tùy quy ước được chọn, "
                        + "kết quả có thể khác nhau.");
        map.put(UncertaintyKind.OUTSIDE_DATASET_RANGE,
                "Ngày yêu cầu nằm ngoài phạm vi dữ liệu đã được kiểm chứng. "
                        + "Hệ thống không suy đoán ngoài phạm vi này.");
        map.put(UncertaintyKind.METHODOLOGY_UNRESOLVED,
                "Phương pháp tính cho trường hợp này chưa được xác minh. "
                        + "Hệ thống không đưa ra kết quả thay vì đoán.");
        map.put(UncertaintyKind.LONGITUDE_UNKNOWN,
                "Chưa có kinh độ nơi sinh nên hệ thống dùng giờ đồng hồ dân sự "
                        + "thay vì giờ mặt trời thực. Kết quả có thể thay đổi nếu bổ sung kinh độ.");
    });

    /**
     * Per-dimension state (DECISION_LOG C5 — {@code CLAUDE.md} Rule E's
     * vocabulary). Labels reuse the exact wording already prescribed in
     * UI_UX_VIETNAMESE_SPEC section 6 for the shared members (CONFLICT,
     * MAJOR_CONFLICT, INSUFFICIENT_EVIDENCE) so a Vietnamese reader never
     * sees the same underlying idea worded two different ways depending on
     * which layer produced it.
     */
    private static final Map<DimensionState, String> DIMENSION_STATE = ordered(map -> {
        map.put(DimensionState.POSITIVE, "Thuận lợi");
        map.put(DimensionState.NEUTRAL, "Trung tính");
        map.put(DimensionState.CAUTION, "Cần thận trọng");
        map.put(DimensionState.NEGATIVE, "Không thuận lợi");
        map.put(DimensionState.MIXED, "Trái chiều");
        map.put(DimensionState.CONFLICT, "Có mâu thuẫn");
        map.put(DimensionState.MAJOR_CONFLICT, "Mâu thuẫn đáng chú ý");
        map.put(DimensionState.INSUFFICIENT_EVIDENCE, "Chưa đủ dữ liệu");
    });

    /**
     * Overall scenario outcome (DECISION_LOG C2, the union of Master Spec
     * section 9 and FUSION_ENGINE_SPEC.md section 7). None of these imply a
     * probability (ADR D6, Fusion section 11) — they name which rule fired,
     * not a likelihood.
     */
    private static final Map<FusionOutcome, String> FUSION_OUTCOME = ordered(map -> {
        map.put(FusionOutcome.CONSENSUS_SUPPORT, "Đồng thuận thuận lợi");
        map.put(FusionOutcome.CONSENSUS_CAUTION, "Đồng thuận cần thận trọng");
        map.put(FusionOutcome.CONSENSUS_NEGATIVE, "Đồng thuận không thuận lợi");
        map.put(FusionOutcome.SUPPORT_WITH_CAUTION, "Thuận lợi, kèm điểm cần lưu ý");
        map.put(FusionOutcome.SUPPORT_WITH_CRITICAL_CAUTION,
                "Thuận lợi, nhưng có cảnh báo quan trọng cần chú ý");
        map.put(FusionOutcome.CAUTION_WITH_SUPPORT, "Cần thận trọng, kèm điểm thuận lợi");
        map.put(FusionOutcome.CAUTION_WITH_CRITICAL_SUPPORT,
                "Cần thận trọng, nhưng có điểm thuận lợi đáng chú ý");
        map.put(FusionOutcome.MIXED, "Kết quả trái chiều giữa các phương pháp");
        map.put(FusionOutcome.MAJOR_CONFLICT, "Mâu thuẫn đáng chú ý");
        map.put(FusionOutcome.METHODOLOGY_CONFLICT, "Khác biệt giữa các trường phái");
        map.put(FusionOutcome.INSUFFICIENT_EVIDENCE, "Chưa đủ dữ liệu");
        map.put(FusionOutcome.NOT_APPLICABLE, "Không áp dụng");
    });

    /** The five conflict categories (FUSION_ENGINE_SPEC.md section 8). */
    private static final Map<ConflictType, String> CONFLICT_TYPE = ordered(map -> {
        map.put(ConflictType.DIRECT_CONFLICT, "Mâu thuẫn trực tiếp");
        map.put(ConflictType.SCOPE_CONFLICT, "Khác phạm vi, không phải mâu thuẫn thật sự");
        map.put(ConflictType.METHODOLOGY_CONFLICT, "Khác biệt giữa các trường phái");
        map.put(ConflictType.INPUT_SENSITIVITY_CONFLICT, "Do dữ liệu đầu vào chưa chắc chắn");
        map.put(ConflictType.TEMPORAL_CONFLICT, "Khác thời điểm");
    });

    /**
     * Methodology registry lifecycle (ADR D7) — distinct from
     * {@link EngineStatus}, which is a per-calculation result. This is the
     * status of a whole methodology as registered, e.g. what
     * {@code GET /api/v1/methodologies} reports.
     */
    private static final Map<MethodologyStatus, String> METHODOLOGY_STATUS = ordered(map -> {
        map.put(MethodologyStatus.PRODUCTION_READY, "Đã sẵn sàng");
        map.put(MethodologyStatus.RESEARCH_REQUIRED, "Cần xác minh thuật toán");
        map.put(MethodologyStatus.DECISION_REQUIRED, "Cần chọn trường phái");
        map.put(MethodologyStatus.CONTENT_REQUIRED, "Thiếu nội dung diễn giải");
        map.put(MethodologyStatus.NOT_IMPLEMENTED, "Chưa triển khai");
        map.put(MethodologyStatus.OUT_OF_SCOPE, "Ngoài phạm vi hiện tại");
    });

    /**
     * Phase 12 (AI Narrative, ADR D8): whether the reader is looking at a
     * real AI-generated narrative or the deterministic hard-data fallback.
     * Neither label implies an error - {@link NarrativeSource#FALLBACK} is a
     * normal, fully-supported state, not a broken one.
     */
    private static final Map<NarrativeSource, String> NARRATIVE_SOURCE = ordered(map -> {
        map.put(NarrativeSource.AI_GENERATED, "Diễn giải bởi AI");
        map.put(NarrativeSource.FALLBACK, "Tóm tắt từ dữ liệu tính toán gốc");
    });

    /**
     * Why a narrative fell back (AI_NARRATIVE_SPEC.md section 6). Written so
     * a curious reader understands the state without needing the technical
     * name - none of these blame the reader or imply their data is at fault.
     */
    private static final Map<FallbackReason, String> FALLBACK_REASON = ordered(map -> {
        map.put(FallbackReason.NONE, "Không áp dụng");
        map.put(FallbackReason.AI_DISABLED, "Phần diễn giải AI đang tắt");
        map.put(FallbackReason.NO_API_KEY, "Chưa cấu hình dịch vụ AI");
        map.put(FallbackReason.TIMEOUT, "Dịch vụ AI phản hồi quá chậm");
        map.put(FallbackReason.RATE_LIMITED, "Dịch vụ AI đang quá tải");
        map.put(FallbackReason.SERVER_ERROR, "Dịch vụ AI gặp lỗi");
        map.put(FallbackReason.PROVIDER_UNAVAILABLE, "Không kết nối được dịch vụ AI");
        map.put(FallbackReason.MALFORMED_JSON, "Phản hồi AI không đúng định dạng");
        map.put(FallbackReason.EMPTY_RESPONSE, "Dịch vụ AI không trả về nội dung");
    });

    /**
     * Thien Can. The enum constants are ASCII transliterations
     * ({@code GIAP}, {@code AT}, ...) because {@code HeavenlyStem} keeps
     * display names out of the identity enum on purpose; this is where they
     * live.
     */
    private static final Map<HeavenlyStem, String> HEAVENLY_STEM = ordered(map -> {
        map.put(HeavenlyStem.GIAP, "Giáp");
        map.put(HeavenlyStem.AT, "Ất");
        map.put(HeavenlyStem.BINH, "Bính");
        map.put(HeavenlyStem.DINH, "Đinh");
        map.put(HeavenlyStem.MAU, "Mậu");
        map.put(HeavenlyStem.KY, "Kỷ");
        map.put(HeavenlyStem.CANH, "Canh");
        map.put(HeavenlyStem.TAN, "Tân");
        map.put(HeavenlyStem.NHAM, "Nhâm");
        map.put(HeavenlyStem.QUY, "Quý");
    });

    /**
     * Dia Chi. {@code EarthlyBranch} is named by zodiac animal precisely so
     * that Ty (rat) and Ty (snake) cannot collide once tone marks are
     * stripped - this map is the only place the toned Vietnamese syllables
     * appear, and getting one wrong here is exactly the bug that naming
     * choice was made to prevent.
     */
    private static final Map<EarthlyBranch, String> EARTHLY_BRANCH = ordered(map -> {
        map.put(EarthlyBranch.RAT, "Tý");
        map.put(EarthlyBranch.OX, "Sửu");
        map.put(EarthlyBranch.TIGER, "Dần");
        map.put(EarthlyBranch.RABBIT, "Mão");
        map.put(EarthlyBranch.DRAGON, "Thìn");
        map.put(EarthlyBranch.SNAKE, "Tỵ");
        map.put(EarthlyBranch.HORSE, "Ngọ");
        map.put(EarthlyBranch.GOAT, "Mùi");
        map.put(EarthlyBranch.MONKEY, "Thân");
        map.put(EarthlyBranch.ROOSTER, "Dậu");
        map.put(EarthlyBranch.DOG, "Tuất");
        map.put(EarthlyBranch.PIG, "Hợi");
    });

    /** Ngu Hanh. */
    private static final Map<FiveElement, String> FIVE_ELEMENT = ordered(map -> {
        map.put(FiveElement.WOOD, "Mộc");
        map.put(FiveElement.FIRE, "Hỏa");
        map.put(FiveElement.EARTH, "Thổ");
        map.put(FiveElement.METAL, "Kim");
        map.put(FiveElement.WATER, "Thủy");
    });

    /** Am Duong. */
    private static final Map<YinYang, String> YIN_YANG = ordered(map -> {
        map.put(YinYang.YANG, "Dương");
        map.put(YinYang.YIN, "Âm");
    });

    /**
     * Tiet Khi, in the enum's own solar-longitude order (Xuan phan at 0
     * degrees first) rather than by calendar month - the same order the
     * astronomy uses, so a reader comparing the two never has to re-sort.
     */
    private static final Map<SolarTerm, String> SOLAR_TERM = ordered(map -> {
        map.put(SolarTerm.XUAN_PHAN, "Xuân phân");
        map.put(SolarTerm.THANH_MINH, "Thanh minh");
        map.put(SolarTerm.COC_VU, "Cốc vũ");
        map.put(SolarTerm.LAP_HA, "Lập hạ");
        map.put(SolarTerm.TIEU_MAN, "Tiểu mãn");
        map.put(SolarTerm.MANG_CHUNG, "Mang chủng");
        map.put(SolarTerm.HA_CHI, "Hạ chí");
        map.put(SolarTerm.TIEU_THU, "Tiểu thử");
        map.put(SolarTerm.DAI_THU, "Đại thử");
        map.put(SolarTerm.LAP_THU, "Lập thu");
        map.put(SolarTerm.XU_THU, "Xử thử");
        map.put(SolarTerm.BACH_LO, "Bạch lộ");
        map.put(SolarTerm.THU_PHAN, "Thu phân");
        map.put(SolarTerm.HAN_LO, "Hàn lộ");
        map.put(SolarTerm.SUONG_GIANG, "Sương giáng");
        map.put(SolarTerm.LAP_DONG, "Lập đông");
        map.put(SolarTerm.TIEU_TUYET, "Tiểu tuyết");
        map.put(SolarTerm.DAI_TUYET, "Đại tuyết");
        map.put(SolarTerm.DONG_CHI, "Đông chí");
        map.put(SolarTerm.TIEU_HAN, "Tiểu hàn");
        map.put(SolarTerm.DAI_HAN, "Đại hàn");
        map.put(SolarTerm.LAP_XUAN, "Lập xuân");
        map.put(SolarTerm.VU_THUY, "Vũ thủy");
        map.put(SolarTerm.KINH_TRAP, "Kinh trập");
    });

    /**
     * Thap Than (Phase 8a). Names only - what each role MEANS for a person is
     * gated on research items R1 and R3, so no interpretive wording appears
     * here. Both common alternate names are given where a reader is likely to
     * know the other one.
     */
    private static final Map<TenGod, String> TEN_GOD = ordered(map -> {
        map.put(TenGod.TY_KIEN, "Tỷ Kiên");
        map.put(TenGod.KIEP_TAI, "Kiếp Tài");
        map.put(TenGod.THUC_THAN, "Thực Thần");
        map.put(TenGod.THUONG_QUAN, "Thương Quan");
        map.put(TenGod.THIEN_TAI, "Thiên Tài");
        map.put(TenGod.CHINH_TAI, "Chính Tài");
        map.put(TenGod.THAT_SAT, "Thất Sát (Thiên Quan)");
        map.put(TenGod.CHINH_QUAN, "Chính Quan");
        map.put(TenGod.THIEN_AN, "Thiên Ấn (Kiêu Thần)");
        map.put(TenGod.CHINH_AN, "Chính Ấn");
    });

    /** Tu Tru positions. */
    private static final Map<PillarPosition, String> PILLAR_POSITION = ordered(map -> {
        map.put(PillarPosition.YEAR, "Trụ Năm");
        map.put(PillarPosition.MONTH, "Trụ Tháng");
        map.put(PillarPosition.DAY, "Trụ Ngày");
        map.put(PillarPosition.HOUR, "Trụ Giờ");
    });

    /**
     * Which convention set the Bat Tu year pillar (research item R18). Both
     * labels name the convention rather than endorsing it, because the point
     * of surfacing this at all is to let the reader see that a choice exists.
     */
    private static final Map<BaziYearBoundary, String> BAZI_YEAR_BOUNDARY = ordered(map -> {
        map.put(BaziYearBoundary.LAP_XUAN, "Đổi năm tại Lập Xuân (Tử Bình)");
        map.put(BaziYearBoundary.LUNAR_NEW_YEAR, "Đổi năm tại Tết (chưa triển khai)");
    });

    /**
     * Why a stored result is kept (CLAUDE.md section 7). Worded from the
     * reader's point of view, not the operator's: {@code EPHEMERAL} says what
     * will happen to their result, and says it plainly rather than hiding a
     * scheduled deletion behind a neutral word like "tạm thời".
     */
    private static final Map<RetentionClass, String> RETENTION_CLASS = ordered(map -> {
        map.put(RetentionClass.PERSISTENT, "Lưu lâu dài");
        map.put(RetentionClass.USER_SAVED, "Bạn đã lưu — không tự động xóa");
        map.put(RetentionClass.EPHEMERAL, "Sẽ tự động xóa khi hết hạn lưu");
        map.put(RetentionClass.AUDIT, "Bản ghi kiểm toán — không tự động xóa");
    });

    public static String of(EngineStatus value) {
        return require(ENGINE_STATUS, value);
    }

    public static String of(Polarity value) {
        return require(POLARITY, value);
    }

    public static String of(Strength value) {
        return require(STRENGTH, value);
    }

    public static String of(Applicability value) {
        return require(APPLICABILITY, value);
    }

    public static String of(Dimension value) {
        return require(DIMENSION, value);
    }

    public static String of(DataConfidence value) {
        return require(DATA_CONFIDENCE, value);
    }

    public static String of(UncertaintyKind value) {
        return require(UNCERTAINTY, value);
    }

    public static String of(DimensionState value) {
        return require(DIMENSION_STATE, value);
    }

    public static String of(FusionOutcome value) {
        return require(FUSION_OUTCOME, value);
    }

    public static String of(ConflictType value) {
        return require(CONFLICT_TYPE, value);
    }

    public static String of(MethodologyStatus value) {
        return require(METHODOLOGY_STATUS, value);
    }

    public static String of(NarrativeSource value) {
        return require(NARRATIVE_SOURCE, value);
    }

    public static String of(FallbackReason value) {
        return require(FALLBACK_REASON, value);
    }

    public static String of(HeavenlyStem value) {
        return require(HEAVENLY_STEM, value);
    }

    public static String of(EarthlyBranch value) {
        return require(EARTHLY_BRANCH, value);
    }

    public static String of(FiveElement value) {
        return require(FIVE_ELEMENT, value);
    }

    public static String of(YinYang value) {
        return require(YIN_YANG, value);
    }

    public static String of(SolarTerm value) {
        return require(SOLAR_TERM, value);
    }

    public static String of(TenGod value) {
        return require(TEN_GOD, value);
    }

    public static String of(PillarPosition value) {
        return require(PILLAR_POSITION, value);
    }

    public static String of(BaziYearBoundary value) {
        return require(BAZI_YEAR_BOUNDARY, value);
    }

    public static String of(RetentionClass value) {
        return require(RETENTION_CLASS, value);
    }

    /** One pillar rendered the way a reader expects it, e.g. "Giap Ty". */
    public static String pillar(HeavenlyStem stem, EarthlyBranch branch) {
        return of(stem) + " " + of(branch);
    }

    /** Non-throwing lookup, for the coverage test. */
    public static Optional<String> lookup(Enum<?> value) {
        if (value == null) {
            return Optional.empty();
        }
        for (Map<? extends Enum<?>, String> registry : allRegistries()) {
            String label = registry.get(cast(value));
            if (label != null) {
                return Optional.of(label);
            }
        }
        return Optional.empty();
    }

    /** Every registry, so the coverage test can walk them all. */
    public static java.util.List<Map<? extends Enum<?>, String>> allRegistries() {
        return java.util.List.of(ENGINE_STATUS, POLARITY, STRENGTH, APPLICABILITY,
                DIMENSION, DATA_CONFIDENCE, UNCERTAINTY, DIMENSION_STATE, FUSION_OUTCOME,
                CONFLICT_TYPE, METHODOLOGY_STATUS, NARRATIVE_SOURCE, FALLBACK_REASON,
                HEAVENLY_STEM, EARTHLY_BRANCH, FIVE_ELEMENT, YIN_YANG, SOLAR_TERM,
                TEN_GOD, PILLAR_POSITION, BAZI_YEAR_BOUNDARY, RETENTION_CLASS);
    }

    /**
     * Every registry as plain strings, keyed by enum simple name - the shape a
     * frontend needs.
     *
     * <p>Exists because Phase 8a is the first feature whose payload is
     * structured data rather than a fixed set of DTO fields: a Bat Tu chart
     * arrives as {@code Evidence.fact} maps holding technical names like
     * {@code GIAP} and {@code TY_KIEN}, and a renderer has nowhere to look
     * them up. The alternative was to embed Vietnamese strings in engine
     * output, which would put display text in the one layer that must stay
     * free of it (Evidence's own Javadoc: "structured finding, never prose").
     */
    public static Map<String, Map<String, String>> asStringRegistries() {
        Map<String, Map<String, String>> byType = new LinkedHashMap<>();
        for (Map<? extends Enum<?>, String> registry : allRegistries()) {
            Map<String, String> entries = new LinkedHashMap<>();
            String typeName = null;
            for (Map.Entry<? extends Enum<?>, String> entry : registry.entrySet()) {
                typeName = entry.getKey().getDeclaringClass().getSimpleName();
                entries.put(entry.getKey().name(), entry.getValue());
            }
            if (typeName != null) {
                byType.put(typeName, Map.copyOf(entries));
            }
        }
        return Map.copyOf(byType);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E cast(Enum<?> value) {
        return (E) value;
    }

    private static <K extends Enum<K>> String require(Map<K, String> registry, K value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot label a null value.");
        }
        String label = registry.get(value);
        if (label == null) {
            // Reaching here means an enum constant was added without a label.
            // Failing loudly is better than leaking a raw enum into the UI,
            // which UI_UX_VIETNAMESE_SPEC section 1 forbids.
            throw new IllegalStateException(
                    "No Vietnamese label for " + value.getDeclaringClass().getSimpleName()
                            + "." + value.name()
                            + ". UI_UX_VIETNAMESE_SPEC section 1 forbids bare enums in the UI.");
        }
        return label;
    }

    private static <K extends Enum<K>> Map<K, String> ordered(
            java.util.function.Consumer<Map<K, String>> filler) {
        Map<K, String> map = new LinkedHashMap<>();
        filler.accept(map);
        return java.util.Collections.unmodifiableMap(map);
    }
}
