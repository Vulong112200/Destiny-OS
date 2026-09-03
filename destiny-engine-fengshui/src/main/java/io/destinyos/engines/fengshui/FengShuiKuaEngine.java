package io.destinyos.engines.fengshui;

import io.destinyos.calendar.HistoricalTimezoneRule;
import io.destinyos.calendar.HistoricalTimezoneRuleTable;
import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.LunarCalendar;
import io.destinyos.calendar.SolarTimeCorrection;
import io.destinyos.calendar.SolarYear;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.context.Uncertainty;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineError;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.result.EngineWarning;
import io.destinyos.core.result.ResearchReference;
import io.destinyos.core.signal.Applicability;
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
import java.util.UUID;

/**
 * Phong Thủy Bát Trạch — Kua number and the eight Bát Biến Du Niên directions
 * (Phase 10, research item R7).
 *
 * <p><strong>School (Rule D).</strong> Bát Trạch (Eight Mansions) only. Master
 * Spec §20 and command §12 both forbid blending Bát Trạch with Phi Tinh or
 * Huyền Không, so nothing here reaches for a Flying Star or a 24-mountain
 * subdivision.
 *
 * <p><strong>What is resolved and what is not.</strong> R7 named five things
 * that had to be determined. Four now are: the school, the male and female
 * formulas ({@link KuaNumber}), the "5" substitution, and the direction mapping
 * ({@link BatTrachTable}, derived from a cited rule and verified against three
 * published tables). The fifth — the year boundary — is genuinely disputed
 * between Vietnamese and classical practice with no source arbitrating, so this
 * engine computes <strong>both</strong> and reports both when they differ. See
 * {@link KuaYearBoundary}.
 *
 * <p><strong>When this engine emits signals, and when it does not.</strong> Bát
 * Trạch is a relation between a person and a direction; a Kua number on its own
 * is a profile, not a judgement. So:
 * <ul>
 *   <li>no {@code facingDirection} supplied → the profile as evidence, no
 *       signals. There is nothing to be favourable or unfavourable
 *       <em>about</em>;</li>
 *   <li>direction supplied and the two year conventions agree → real signals,
 *       with polarity and strength read off the tradition's own cát/hung and
 *       thượng/trung/tiểu classification, not assigned by this project;</li>
 *   <li>direction supplied but the conventions disagree → no signals, and the
 *       disagreement reported. Two candidate Kua numbers mean two different
 *       readings of the same direction, and picking one would be exactly the
 *       choice R7 says nobody has earned yet.</li>
 * </ul>
 */
public final class FengShuiKuaEngine implements MetaphysicalEngine<FengShuiKuaInput, KuaProfile> {

    public static final String ENGINE_ID = "FENGSHUI_KUA";
    public static final String METHODOLOGY_ID = "FENGSHUI_KUA";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String RULE_VERSION = "1.0";
    public static final String SCHOOL =
            "Bát Trạch (Bát Biến Du Niên) — cả hai quy ước ranh giới năm được tính, "
                    + "không tự chọn một";

    public static final String SOURCE =
            "Kua formula and the 5-substitution cross-checked between hoc.kabala.vn (VN) and "
                    + "wofs.com + fengshuimall.com (EN), which agree exactly including the "
                    + "discontinuity at year 2000; spot-checked against the independently stated "
                    + "fact that a male born 1990 is cung Kham. Direction table DERIVED from the "
                    + "Bat Bien Du Nien line-change rule (Chinese 8-mansions mnemonic reported by "
                    + "blog.sina.com.cn and zhuanlan.zhihu.com; Vietnamese 'Bat Bien Du Nien' at "
                    + "lyhocphuongdong.vn, which names the Tuyet Menh pairs Can-Ly, Khon-Kham, "
                    + "Can-Ton, Doai-Chan) and verified against three published tables: "
                    + "masterseanchan.com's 8x8 matrix (60/64 cells agree; the 4 that do not are "
                    + "shown to be that source's error by symmetry, by a 6:2 majority within its "
                    + "own table, and by direct contradiction from the Vietnamese source), "
                    + "nguyenthehoa.com's Can page (8/8) and phongthuykhaitoan.com's Chan page. "
                    + "Life-area mapping authored from the descriptions at kasai.com.vn, "
                    + "xaydung365.com.vn and nguyenthehoa.com. All retrieved 2026-08-22. "
                    + "Year-boundary dispute recorded as R7's remaining open item.";

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Phong Thủy — Bát Trạch (Cung Phi)",
            METHODOLOGY_ID,
            METHODOLOGY_VERSION,
            "1.0",
            SCHOOL,
            SOURCE,
            MethodologyStatus.PRODUCTION_READY
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            .dimensions(Dimension.HOME, Dimension.FINANCE, Dimension.CAREER,
                    Dimension.RELATIONSHIP, Dimension.HEALTH_REFLECTION, Dimension.DECISION)
            // The Kua year turns in early February, so a January or
            // early-February birth cannot be resolved from a date alone - but a
            // time of day is only decisive within minutes of the Lap Xuan
            // instant, so this does not require EXACT precision.
            .requiresBirthTime(false)
            .requiresLocation(true)
            .requiresName(false)
            .requiresCalendar(true)
            .deterministic(true)
            .requiresSeed(false)
            .supportedDateRange(SupportedDateRange.of(LocalDate.of(1900, 1, 1),
                    LocalDate.of(2100, 12, 31)))
            .build();

    @Override
    public EngineResult<KuaProfile> calculate(FengShuiKuaInput input, CalculationContext context) {
        Objects.requireNonNull(input, "input");

        if (input.gender() == null) {
            // Not a NOT_APPLICABLE and not a guess: the male and female formulas
            // are different and asymmetric, so there is no neutral answer to
            // give (CLAUDE.md Rule C).
            return EngineResult.invalidInput(List.of(EngineError.of("KUA_GENDER_REQUIRED",
                    "Bát Trạch cần biết giới tính: công thức cung phi cho nam và nữ khác nhau "
                            + "và không đối xứng, nên không có giá trị mặc định nào trung lập.",
                    ENGINE_ID)));
        }

        LocalDate utcDate = input.utcInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        var ruleOpt = HistoricalTimezoneRuleTable.resolve(utcDate, input.region());
        if (ruleOpt.isEmpty()) {
            return EngineResult.researchRequired(new ResearchReference(
                    "R14b", "Calendar",
                    "Không có quy tắc múi giờ lịch sử được xác minh cho (" + utcDate + ", "
                            + input.region() + "). Ranh giới năm của cung phi nằm đầu tháng 2 nên "
                            + "phụ thuộc múi giờ; không thể tính mà không suy đoán.",
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
        LocalDateTime local = civilLocal;
        if (input.longitudeDegreesIfKnown() != null) {
            local = civilLocal.plus(SolarTimeCorrection.meanSolarTimeCorrection(
                    input.longitudeDegreesIfKnown(), utcOffsetHours));
        }

        double julianDateUt = JulianDay.fromLocalDateTime(local, utcOffsetHours);
        int lapXuanYear = SolarYear.lapXuanBasedYear(local.toLocalDate(),
                SolarYear.solarMonthIndex(julianDateUt));
        int tetYear = LunarCalendar.toLunar(local.getDayOfMonth(), local.getMonthValue(),
                local.getYear(), utcOffsetHours).year();

        Trigram byLapXuan = KuaNumber.forYear(lapXuanYear, input.gender());
        Trigram byTet = KuaNumber.forYear(tetYear, input.gender());
        boolean agree = byLapXuan == byTet;

        if (!agree) {
            uncertainties.add(Uncertainty.of(UncertaintyKind.METHODOLOGY_UNRESOLVED,
                    "Ngày sinh nằm trong khoảng hai quy ước ranh giới năm cho ra cung phi khác "
                            + "nhau. Theo Lập Xuân (cách cổ điển): cung " + byLapXuan
                            + " (số " + byLapXuan.kuaNumber() + "). Theo Tết (cách phổ biến ở "
                            + "Việt Nam): cung " + byTet + " (số " + byTet.kuaNumber()
                            + "). Chưa có nguồn nào phân định, nên hệ thống không tự chọn giúp "
                            + "bạn và không đưa ra tín hiệu nào cho lần chạy này.",
                    "R7"));
            warnings.add(EngineWarning.critical("KUA_YEAR_BOUNDARY_SCHOOLS_DISAGREE",
                    "Cung phi phụ thuộc quy ước Lập Xuân hay Tết, và hai quy ước cho kết quả "
                            + "khác nhau trong trường hợp này (R7)."));
        }

        BatTrachRelation facingRelation = null;
        if (input.facingDirection() != null && agree) {
            facingRelation = BatTrachTable.relation(byLapXuan, input.facingDirection());
        } else if (input.facingDirection() == null) {
            warnings.add(EngineWarning.of("KUA_NO_FACING_DIRECTION",
                    "Chưa có hướng nhà/phòng để đối chiếu, nên chỉ lập được bảng tám hướng của "
                            + "bạn, chưa đánh giá được hướng cụ thể nào."));
        }

        var profile = new KuaProfile(byLapXuan, byTet, lapXuanYear, tetYear,
                BatTrachTable.allDirections(byLapXuan), input.facingDirection(),
                facingRelation, uncertainties);

        List<Signal> signals = facingRelation == null
                ? List.of()
                : buildSignals(profile, facingRelation);

        List<Evidence> evidence = buildEvidence(profile, agree);

        if (agree && facingRelation != null) {
            return EngineResult.success(profile, evidence, signals);
        }
        // PARTIAL, not SUCCESS: either there was no direction to judge, or the
        // year conventions disagreed. Both are real results, and both are less
        // than this engine can do when the input allows it.
        return new EngineResult<>(EngineStatus.PARTIAL, profile, evidence, signals,
                List.copyOf(warnings), List.of(),
                agree ? null : new ResearchReference("R7", "Phong Thủy",
                        "Ranh giới năm của cung phi (Lập Xuân hay Tết) chưa có nguồn phân định. "
                                + "Cả hai đáp án đều được trả về; engine không phát sinh tín "
                                + "hiệu khi hai quy ước khác nhau.",
                        "docs/RESEARCH_BLOCKERS.md R7",
                        List.of("Lập Xuân (cổ điển)", "Tết (phổ biến ở Việt Nam)")),
                Map.of("methodologyId", METHODOLOGY_ID,
                        "contentVersion", BatTrachMeanings.CONTENT_VERSION,
                        "boundaryConventionsAgree", String.valueOf(agree)));
    }

    /**
     * One signal per life area the relation speaks to, all sharing an evidence
     * group so Fusion's deduplication counts them as one finding rather than
     * several (FUSION_ENGINE_SPEC §5).
     */
    private static List<Signal> buildSignals(KuaProfile profile, BatTrachRelation relation) {
        String evidenceGroupId = "FENGSHUI_KUA_FACING";
        List<Signal> signals = new ArrayList<>();
        for (Dimension dimension : BatTrachMeanings.dimensionsOf(relation)) {
            signals.add(new Signal(
                    UUID.randomUUID().toString(),
                    ENGINE_ID,
                    SCHOOL,
                    dimension,
                    "FENGSHUI_" + relation.name(),
                    BatTrachMeanings.polarityOf(relation),
                    BatTrachMeanings.strengthOf(relation),
                    Applicability.HIGH,
                    // Not critical: an unfavourable facing direction is a
                    // traditional caution about a building, not a warning that
                    // must survive a majority vote against it. Reserving
                    // `critical` for methodology limits keeps it meaningful.
                    false,
                    List.of(),
                    evidenceGroupId));
        }
        return List.copyOf(signals);
    }

    private static List<Evidence> buildEvidence(KuaProfile profile, boolean agree) {
        List<Evidence> evidence = new ArrayList<>();
        String groupId = "FENGSHUI_KUA";

        Map<String, Object> kuaFact = new LinkedHashMap<>();
        kuaFact.put("trigram", profile.trigram().name());
        kuaFact.put("kuaNumber", profile.trigram().kuaNumber());
        kuaFact.put("group", profile.trigram().group().name());
        kuaFact.put("element", profile.trigram().element().name());
        kuaFact.put("lapXuanYear", profile.lapXuanYear());
        kuaFact.put("tetYear", profile.tetYear());
        kuaFact.put("boundaryConventionsAgree", agree);
        if (!agree) {
            kuaFact.put("trigramByTet", profile.trigramByTet().name());
            kuaFact.put("kuaNumberByTet", profile.trigramByTet().kuaNumber());
        }
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "FENGSHUI_KUA_NUMBER", RULE_VERSION, Dimension.HOME, kuaFact,
                "kua-formula", groupId, null));

        // The eight directions are only meaningful for one trigram, so they are
        // omitted rather than duplicated when the two conventions disagree -
        // publishing one set would present the Lap Xuan answer as the answer.
        if (agree) {
            Map<String, Object> directionsFact = new LinkedHashMap<>();
            profile.directions().forEach((direction, relation) ->
                    directionsFact.put(direction.name(), relation.name()));
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "FENGSHUI_BAT_TRACH_DIRECTIONS", RULE_VERSION, Dimension.HOME,
                    directionsFact, "bat-bien-du-nien-derivation", groupId, null));

            // The authored meaning of each relation, so the reader is told what
            // Sinh Khí actually is rather than being shown a coloured badge and
            // left to search the web for it. Same route the Tarot card meanings
            // already take: authored content, versioned, carried on evidence -
            // never generated, and never through the AI narrative stage.
            //
            // Gated on `agree` along with the table above, and that matters:
            // publishing the wording for relations the engine is deliberately
            // withholding would leak the Lap Xuan answer's interpretation while
            // pretending to withhold the answer.
            Map<String, Object> relationMeanings = new LinkedHashMap<>();
            profile.directions().values().stream().distinct().forEach(relation -> {
                BatTrachRelationMeanings.Meaning meaning = BatTrachRelationMeanings.of(relation);
                if (meaning == null) {
                    return;
                }
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("natureVi", meaning.natureVi());
                entry.put("tendencyVi", meaning.tendencyVi());
                entry.put("domainsVi", meaning.domainsVi());
                relationMeanings.put(relation.name(), entry);
            });
            Map<String, Object> meaningsFact = new LinkedHashMap<>();
            meaningsFact.put("relationMeanings", relationMeanings);
            meaningsFact.put("contentVersion", BatTrachRelationMeanings.CONTENT_VERSION);
            meaningsFact.put("sourceNoteVi", BatTrachRelationMeanings.SOURCE_NOTE_VI);
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "FENGSHUI_RELATION_MEANINGS", RULE_VERSION, Dimension.HOME,
                    meaningsFact, "bat-trach-relation-meanings", groupId, null));
        }

        // The applied half of Bát Trạch - which direction to sleep facing, to
        // put a desk in, to point a front door - is not implemented and has no
        // verified source in this repository. Master Spec §20 files it under
        // "Advanced" and, until 2026-09-03, assigned it no research id at all,
        // so it was invisible in every status table this project keeps: this
        // was the only chart engine with no blocked section, which read as
        // "nothing is missing here". Reporting it is ADR D7 - a gap is
        // displayed with its reason, never omitted.
        Map<String, Object> appliedFact = new LinkedHashMap<>();
        appliedFact.put("sectionId", "BAT_TRACH_APPLICATION");
        appliedFact.put("displayNameVi",
                "Ứng dụng theo phòng và vật dụng (hướng ngủ, hướng bàn làm việc, hướng cửa chính)");
        appliedFact.put("researchId", "R26");
        appliedFact.put("reasonVi",
                "Chưa có nguồn Bát Trạch nào được xác minh cho việc gán từng du niên vào công năng "
                        + "cụ thể của phòng hay vật dụng. Riêng việc phân biệt tọa và hướng đã đủ "
                        + "để một bảng nghe hợp lý sai một nửa số dòng, nên phần này bỏ trống có "
                        + "chủ đích thay vì đoán.");
        appliedFact.put("knownVariants", List.of(
                "Quy ước tọa (hướng lưng tựa) và quy ước hướng (hướng mặt nhìn) cho ra kết quả ngược nhau",
                "Có nguồn gán quy tắc cho vật dụng, có nguồn gán cho người ngồi hoặc nằm",
                "Chưa thống nhất lấy cung phi của ai làm chuẩn cho một không gian dùng chung"));
        evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                "FENGSHUI_BLOCKED_BAT_TRACH_APPLICATION", RULE_VERSION, Dimension.HOME,
                appliedFact, "research-blocker", groupId, null));

        if (profile.facingRelation() != null) {
            Map<String, Object> facingFact = new LinkedHashMap<>();
            facingFact.put("facingDirection", profile.facingDirection().name());
            facingFact.put("relation", profile.facingRelation().name());
            facingFact.put("auspicious", profile.facingRelation().auspicious());
            facingFact.put("rank", profile.facingRelation().rank().name());
            evidence.add(new Evidence(UUID.randomUUID().toString(), ENGINE_ID, SCHOOL,
                    "FENGSHUI_FACING_ASSESSMENT", RULE_VERSION, Dimension.HOME, facingFact,
                    "bat-bien-du-nien-derivation", groupId, null));
        }

        return List.copyOf(evidence);
    }

    @Override
    public ValidationResult validateInput(FengShuiKuaInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "Phong Thủy input is required.", ENGINE_ID);
        }
        if (input.gender() == null) {
            return ValidationResult.failed("KUA_GENDER_REQUIRED",
                    "Gender is required: the male and female Kua formulas differ and are not "
                            + "symmetric (research item R7), so no neutral default exists.",
                    ENGINE_ID);
        }
        LocalDate utcDate = input.utcInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        if (!CAPABILITY.supportedDateRange().covers(utcDate)) {
            return ValidationResult.failed("OUTSIDE_SUPPORTED_RANGE",
                    "Birth date " + utcDate + " is outside the supported range "
                            + CAPABILITY.supportedDateRange().describe() + ".", ENGINE_ID);
        }
        if (input.longitudeDegreesIfKnown() != null) {
            double longitude = input.longitudeDegreesIfKnown();
            if (longitude < -180 || longitude > 180) {
                return ValidationResult.failed("INVALID_LONGITUDE",
                        "Longitude must be within [-180, 180]; got " + longitude + ".", ENGINE_ID);
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
