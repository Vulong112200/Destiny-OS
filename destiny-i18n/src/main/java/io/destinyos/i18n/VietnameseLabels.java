package io.destinyos.i18n;

import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.evidence.DataConfidence;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import io.destinyos.engine.MethodologyStatus;
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
                CONFLICT_TYPE, METHODOLOGY_STATUS);
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
