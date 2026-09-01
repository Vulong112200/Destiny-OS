package io.destinyos.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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
import io.destinyos.engines.astrology.AstrologicalHouse;
import io.destinyos.engines.astrology.ZodiacSign;
import io.destinyos.engines.iching.IChingTrigram;
import io.destinyos.engines.iching.LineValue;
import io.destinyos.engines.bazi.BaziYearBoundary;
import io.destinyos.engines.bazi.LuckCycleDirection;
import io.destinyos.engines.bazi.PillarPosition;
import io.destinyos.engines.bazi.TenGod;
import io.destinyos.engines.fengshui.BatTrachRelation;
import io.destinyos.engines.fengshui.CompassDirection;
import io.destinyos.core.context.Gender;
import io.destinyos.engines.fengshui.KuaYearBoundary;
import io.destinyos.engines.fengshui.Trigram;
import io.destinyos.engines.fengshui.TrigramGroup;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.DimensionState;
import io.destinyos.fusion.FusionOutcome;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Every user-facing enum constant must have a Vietnamese label
 * (UI_UX_VIETNAMESE_SPEC sections 1 and 6, CLAUDE.md section 9).
 *
 * <p>Without this test the rule decays predictably: someone adds an enum
 * constant, forgets the label, and a raw {@code MAJOR_CONFLICT} reaches a
 * Vietnamese user months later. The build is the only reviewer that never
 * forgets.
 */
class LabelCoverageTest {

    /**
     * Enums that can reach a user. Adding a user-facing enum without adding it
     * here is itself a gap, so the list is deliberately explicit rather than
     * discovered by reflection.
     */
    private static final List<Class<? extends Enum<?>>> USER_FACING = List.of(
            EngineStatus.class,
            Polarity.class,
            Strength.class,
            Applicability.class,
            Dimension.class,
            DataConfidence.class,
            UncertaintyKind.class,
            DimensionState.class,
            FusionOutcome.class,
            ConflictType.class,
            MethodologyStatus.class,
            // Phase 8a. Can Chi identity enums carry no display names of their
            // own by design, so the label registry is the only thing standing
            // between a Vietnamese reader and a chart that says "GIAP RAT".
            HeavenlyStem.class,
            EarthlyBranch.class,
            FiveElement.class,
            YinYang.class,
            SolarTerm.class,
            TenGod.class,
            PillarPosition.class,
            BaziYearBoundary.class,
            // Phase 8b, Đại Vận (R2). "THUAN" on a results page is
            // exactly the bare technical enum section 1 forbids.
            LuckCycleDirection.class,
            // CLAUDE.md section 7 retention. Reaches the user directly - the
            // result page tells them whether their reading will be deleted.
            RetentionClass.class,
            // Phase 10, Bát Trạch. A direction shown as "SOUTHEAST" to a
            // Vietnamese reader is exactly what section 1 forbids.
            Trigram.class,
            CompassDirection.class,
            TrigramGroup.class,
            BatTrachRelation.class,
            KuaYearBoundary.class,
            Gender.class,
            // Phase 11, Western Astrology. "CANCER" or "HOUSE_10" on a
            // results page is exactly the bare technical enum section 1
            // forbids.
            ZodiacSign.class,
            AstrologicalHouse.class,
            IChingTrigram.class,
            LineValue.class);

    @Test
    @DisplayName("Every constant of every user-facing enum has a Vietnamese label")
    void everyConstantHasALabel() {
        List<String> missing = new ArrayList<>();

        for (Class<? extends Enum<?>> type : USER_FACING) {
            for (Enum<?> constant : type.getEnumConstants()) {
                if (VietnameseLabels.lookup(constant).isEmpty()) {
                    missing.add(type.getSimpleName() + "." + constant.name());
                }
            }
        }

        assertThat(missing)
                .as("Enum constants with no Vietnamese label. UI_UX_VIETNAMESE_SPEC "
                        + "section 1 forbids bare technical enums in the UI.")
                .isEmpty();
    }

    @Test
    @DisplayName("Every engine the modules declare has a Vietnamese name")
    void everyEngineHasAVietnameseName() {
        // Engine ids are Strings, not an enum, so they cannot ride the registry
        // walk above — which is precisely how a raw id reached the user: a
        // conflict's involvedEngines was rendered verbatim as
        // "Liên quan: ICHING, WESTERN_ASTROLOGY".
        //
        // Pinned by hand against the ENGINE_ID constants the engine modules
        // actually declare. Adding a seventh engine without a Vietnamese name
        // fails here rather than surfacing on a results page.
        List<String> declaredEngineIds = List.of(
                "BAZI", "TAROT", "NUMEROLOGY_PYTHAGOREAN",
                "FENGSHUI_KUA", "WESTERN_ASTROLOGY", "ICHING");

        assertThat(VietnameseLabels.engineNames().keySet())
                .as("engine ids carrying a Vietnamese name")
                .containsExactlyInAnyOrderElementsOf(declaredEngineIds);

        for (String engineId : declaredEngineIds) {
            String name = VietnameseLabels.engineName(engineId);
            assertThat(name).as("name for %s", engineId).isNotBlank();
            // engineName() falls back to the id itself when unlabelled, so an
            // equal value means the label is missing rather than translated.
            assertThat(name).as("name for %s must not be the id itself", engineId)
                    .isNotEqualTo(engineId);
        }

        // The frontend reads labels.Engine the same way it reads
        // labels.HeavenlyStem, so the registry map must carry it too.
        assertThat(VietnameseLabels.asStringRegistries())
                .containsKey("Engine");
    }

    @Test
    @DisplayName("Labels are non-empty and actually Vietnamese, not the enum name")
    void labelsAreRealTranslations() {
        for (Class<? extends Enum<?>> type : USER_FACING) {
            for (Enum<?> constant : type.getEnumConstants()) {
                String label = VietnameseLabels.lookup(constant).orElseThrow();

                assertThat(label).as("Label for %s", constant).isNotBlank();

                // A label identical to the constant name means someone pasted
                // the enum in as a placeholder rather than translating it.
                assertThat(label)
                        .as("Label for %s must not be the enum name itself", constant)
                        .isNotEqualTo(constant.name());
            }
        }
    }

    @Test
    @DisplayName("No label implies a probability or percentage (ADR D6)")
    void labelsCarryNoProbability() {
        // FUSION_ENGINE_SPEC section 11 forbids output like "72% tot", and
        // AI_NARRATIVE_SPEC section 8 gives the same instruction in prose.
        // Magnitude words are permitted; numbers dressed as likelihood are not.
        Pattern percentageLike = Pattern.compile("\\d+\\s*%|xác suất|phần trăm",
                Pattern.CASE_INSENSITIVE);

        for (Class<? extends Enum<?>> type : USER_FACING) {
            for (Enum<?> constant : type.getEnumConstants()) {
                String label = VietnameseLabels.lookup(constant).orElseThrow();

                assertThat(percentageLike.matcher(label).find())
                        .as("Label for %s must not imply probability: '%s'", constant, label)
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("Strength labels use the FUSION_ENGINE_SPEC section 11 vocabulary")
    void strengthUsesPrescribedWords() {
        assertThat(VietnameseLabels.of(Strength.WEAK)).isEqualTo("Yếu");
        assertThat(VietnameseLabels.of(Strength.MEDIUM)).isEqualTo("Vừa");
        assertThat(VietnameseLabels.of(Strength.STRONG)).isEqualTo("Mạnh");
    }

    @Test
    @DisplayName("The UI_UX_VIETNAMESE_SPEC section 6 terminology table is honoured exactly")
    void specifiedTerminologyMatches() {
        assertThat(VietnameseLabels.of(EngineStatus.SUCCESS)).isEqualTo("Thành công");
        assertThat(VietnameseLabels.of(EngineStatus.PARTIAL)).isEqualTo("Một phần");
        assertThat(VietnameseLabels.of(EngineStatus.NOT_APPLICABLE)).isEqualTo("Không áp dụng");
        assertThat(VietnameseLabels.of(EngineStatus.RESEARCH_REQUIRED))
                .isEqualTo("Cần xác minh thuật toán");
        assertThat(VietnameseLabels.of(EngineStatus.NOT_IMPLEMENTED)).isEqualTo("Chưa triển khai");
        assertThat(VietnameseLabels.of(Polarity.SUPPORT)).isEqualTo("Thuận lợi");
        assertThat(VietnameseLabels.of(Polarity.CAUTION)).isEqualTo("Cần thận trọng");
    }

    @Test
    @DisplayName("Can Chi labels use the toned Vietnamese syllables, and Ty/Ty stay distinct")
    void canChiLabelsAreCorrectlyToned() {
        // The whole reason EarthlyBranch is named by animal: stripping tones
        // collides the rat and the snake. If these two labels are ever equal,
        // that protection has been undone in the one place it mattered.
        assertThat(VietnameseLabels.of(EarthlyBranch.RAT)).isEqualTo("Tý");
        assertThat(VietnameseLabels.of(EarthlyBranch.SNAKE)).isEqualTo("Tỵ");
        assertThat(VietnameseLabels.of(EarthlyBranch.RAT))
                .isNotEqualTo(VietnameseLabels.of(EarthlyBranch.SNAKE));

        assertThat(VietnameseLabels.of(HeavenlyStem.GIAP)).isEqualTo("Giáp");
        assertThat(VietnameseLabels.of(FiveElement.WOOD)).isEqualTo("Mộc");
        assertThat(VietnameseLabels.of(YinYang.YANG)).isEqualTo("Dương");
        assertThat(VietnameseLabels.pillar(HeavenlyStem.GIAP, EarthlyBranch.RAT))
                .isEqualTo("Giáp Tý");
    }

    @Test
    @DisplayName("No Thap Than label carries an interpretation (R1/R3 are still open)")
    void tenGodLabelsAreNamesNotReadings() {
        // A label like "Chính Tài — tài lộc ổn định" would be a reading, and a
        // reading needs Day Master strength (R3) and a Dụng Thần school (R1).
        // Keeping these short is what keeps the interpretation out.
        for (TenGod god : TenGod.values()) {
            assertThat(VietnameseLabels.of(god))
                    .as("Thập Thần label for %s", god)
                    .hasSizeLessThan(30)
                    .doesNotContain("tốt")
                    .doesNotContain("xấu")
                    .doesNotContain("thuận lợi")
                    .doesNotContain("bất lợi");
        }
    }

    @Test
    @DisplayName("The string registry view exposes every labelled enum type by name")
    void stringRegistryViewCoversEveryType() {
        // The frontend renders Bát Tự charts from Evidence.fact maps holding
        // technical names, so this view is the only thing that lets it show
        // Vietnamese at all. A missing type here is an unlabelled UI.
        var registries = VietnameseLabels.asStringRegistries();

        assertThat(registries)
                .containsKeys("HeavenlyStem", "EarthlyBranch", "FiveElement", "YinYang",
                        "SolarTerm", "TenGod", "PillarPosition", "BaziYearBoundary",
                        "EngineStatus", "Polarity", "Dimension");
        assertThat(registries.get("HeavenlyStem")).hasSize(10)
                .containsEntry("GIAP", "Giáp");
        assertThat(registries.get("EarthlyBranch")).hasSize(12);
        assertThat(registries.get("SolarTerm")).hasSize(24);

        // "Engine" is the one registry keyed by String rather than by an enum,
        // so it is not in allRegistries() and has to be counted separately. It
        // is here because a conflict's involvedEngines used to reach the page
        // as raw ids ("Liên quan: ICHING, WESTERN_ASTROLOGY").
        assertThat(registries).containsKey("Engine");
        assertThat(registries.get("Engine")).containsEntry("ICHING", "Kinh Dịch");
        assertThat(registries).hasSize(VietnameseLabels.allRegistries().size() + 1);
    }

    @Test
    @DisplayName("Retention labels tell the reader plainly whether their result will be deleted")
    void retentionLabelsAreNotEuphemisms() {
        // The failure mode this guards is a polite label. "Tạm thời" is
        // technically true of an EPHEMERAL result and tells the reader nothing
        // about the scheduled deletion, so the label has to name it.
        assertThat(VietnameseLabels.of(RetentionClass.EPHEMERAL))
                .contains("tự động xóa");
        assertThat(VietnameseLabels.of(RetentionClass.USER_SAVED))
                .contains("không tự động xóa");
        assertThat(VietnameseLabels.of(RetentionClass.AUDIT))
                .contains("không tự động xóa");
    }

    @Test
    @DisplayName("Every Bát Trạch relation label carries the tradition's own severity word")
    void batTrachLabelsCarryTheirRanking() {
        // The signal strength is read off thượng/trung/tiểu cát and
        // đại/thứ/tiểu hung, so a label that drops the qualifier hides why two
        // "bad" directions produce different polarities.
        assertThat(VietnameseLabels.of(BatTrachRelation.SINH_KHI)).contains("thượng cát");
        assertThat(VietnameseLabels.of(BatTrachRelation.THIEN_Y)).contains("trung cát");
        assertThat(VietnameseLabels.of(BatTrachRelation.PHUC_VI)).contains("tiểu cát");
        assertThat(VietnameseLabels.of(BatTrachRelation.HOA_HAI)).contains("tiểu hung");
        assertThat(VietnameseLabels.of(BatTrachRelation.LUC_SAT)).contains("thứ hung");
        assertThat(VietnameseLabels.of(BatTrachRelation.TUYET_MENH)).contains("đại hung");
    }

    @Test
    @DisplayName("Compass labels are Vietnamese, and the eight are all distinct")
    void compassLabelsAreDistinct() {
        assertThat(VietnameseLabels.of(CompassDirection.SOUTHEAST)).isEqualTo("Đông Nam");
        assertThat(VietnameseLabels.of(CompassDirection.NORTHWEST)).isEqualTo("Tây Bắc");

        var labels = new java.util.HashSet<String>();
        for (CompassDirection direction : CompassDirection.values()) {
            assertThat(labels.add(VietnameseLabels.of(direction)))
                    .as("duplicate label at %s", direction)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Every uncertainty kind explains itself to an ordinary reader (ADR D3)")
    void uncertaintyMessagesAreExplanatory() {
        for (UncertaintyKind kind : UncertaintyKind.values()) {
            String label = VietnameseLabels.of(kind);

            // A user whose birth falls in an unresolved window needs a sentence,
            // not a term. Terse labels here would defeat the purpose of
            // preserving the uncertainty at all.
            assertThat(label)
                    .as("Uncertainty message for %s should be a full explanation", kind)
                    .hasSizeGreaterThan(40);
        }
    }

    @Test
    @DisplayName("Requesting a label for null fails loudly rather than rendering blank")
    void nullIsRejected() {
        assertThatCode(() -> VietnameseLabels.of((EngineStatus) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
