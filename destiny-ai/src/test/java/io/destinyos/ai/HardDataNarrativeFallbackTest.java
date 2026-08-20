package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** ADR D8: the system stays fully usable with the LLM disabled or failing. */
class HardDataNarrativeFallbackTest {

    @Test
    void isWellFormedSoItNeverFailsItsOwnSchemaCheck() {
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.isWellFormed()).isTrue();
    }

    @Test
    void disabledCaseUsesANeutralTonRatherThanAnErrorMessage() {
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.summary()).doesNotContain("Không thể tạo");
    }

    @Test
    void failureCaseUsesTheAdrD8RequiredSentence() {
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.MALFORMED_JSON);

        assertThat(response.summary())
                .contains("Không thể tạo phần diễn giải AI lúc này")
                .contains("Dữ liệu tính toán gốc vẫn được giữ nguyên");
    }

    @Test
    void surfacesEverySignalConflictWarningAndLimitation() {
        var signal = new NarrativeSignalItem("TAROT", Dimension.CAREER, "Su nghiep", Polarity.SUPPORT, "Ung ho",
                Strength.STRONG, "Manh", true, "the-fool");
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"),
                "Mo ta xung dot");
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(signal), List.of(conflict),
                List.of("Canh bao A"), List.of("Gioi han B"), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.keySignals()).hasSize(1).allSatisfy(text ->
                assertThat(text).contains("TAROT").contains("Su nghiep").contains("Ung ho").contains("đáng chú ý"));
        assertThat(response.conflicts()).hasSize(1).allSatisfy(text ->
                assertThat(text).contains("Xung dot truc tiep").contains("Mo ta xung dot"));
        assertThat(response.cautions()).contains("Canh bao A", "Gioi han B");
    }

    @Test
    void asksAReflectionQuestionPerConflictInvolvingTwoOrMoreEngines() {
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"), "mo ta");
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(conflict),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.reflectionQuestions()).anySatisfy(q ->
                assertThat(q).contains("TAROT").contains("BAZI"));
    }
}
