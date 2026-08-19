package io.destinyos.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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
            MethodologyStatus.class);

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
