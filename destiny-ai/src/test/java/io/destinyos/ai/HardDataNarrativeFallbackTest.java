package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** ADR D8: the system stays fully usable with the LLM disabled or failing. */
class HardDataNarrativeFallbackTest {

    @Test
    void isWellFormedSoItNeverFailsItsOwnSchemaCheck() {
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.isWellFormed()).isTrue();
    }

    @Test
    void disabledCaseUsesANeutralTonRatherThanAnErrorMessage() {
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.summary()).doesNotContain("Không thể tạo");
    }

    @Test
    void failureCaseUsesTheAdrD8RequiredSentence() {
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.MALFORMED_JSON);

        assertThat(response.summary())
                .contains("Không thể tạo phần diễn giải AI lúc này")
                .contains("Dữ liệu tính toán gốc vẫn được giữ nguyên");
    }

    @Test
    void surfacesEverySignalConflictWarningAndLimitation() {
        var signal = new NarrativeSignalItem("TAROT", Dimension.CAREER, "Su nghiep", Polarity.SUPPORT, "Ung ho",
                Strength.STRONG, "Manh", true, "the-fool", null, null);
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"),
                "Mo ta xung dot");
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(signal), List.of(conflict),
                List.of("Canh bao A"), List.of("Gioi han B"), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.keySignals()).hasSize(1).allSatisfy(text ->
                assertThat(text).contains("TAROT").contains("Su nghiep").contains("Ung ho").contains("đáng chú ý"));
        assertThat(response.conflicts()).hasSize(1).allSatisfy(text ->
                assertThat(text).contains("Xung dot truc tiep").contains("Mo ta xung dot"));
        assertThat(response.cautions()).contains("Canh bao A", "Gioi han B");
    }

    @Test
    @DisplayName("The summary restates the question and the focus the user chose")
    void summaryRestatesTheUsersOwnQuestion() {
        // This is the live production narrative: destiny.ai.enabled defaults to
        // false (ADR D8), so for most users this text IS the reading. Before
        // the question reached this far, a user who typed a specific question
        // got back a page that never acknowledged it.
        NarrativeInput input = new NarrativeInput("Su nghiep", "Toi co nen doi viec khong?",
                "Doi viec / nhay viec", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.summary())
                .contains("Toi co nen doi viec khong?")
                .contains("Doi viec / nhay viec")
                .as("this class assembles hard data by template and cannot reason about a "
                        + "question - it must not imply otherwise")
                .contains("chưa phải câu trả lời trực tiếp");
    }

    @Test
    @DisplayName("With no question asked, the summary says nothing about one")
    void summaryOmitsTheQuestionBlockWhenNoneWasAsked() {
        NarrativeInput input = new NarrativeInput("Su nghiep", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.summary())
                .doesNotContain("Câu hỏi của bạn")
                .doesNotContain("Hướng quan tâm")
                .doesNotContain("chưa phải câu trả lời trực tiếp");
    }

    @Test
    @DisplayName("A key signal carries the engine's authored card name and meaning, not just its categories")
    void keySignalsCarryAuthoredTitleAndMeaning() {
        // The whole point of the change: "TAROT: Sự nghiệp - Thuận lợi (Mạnh)"
        // tells a reader that something was favourable but not what it was.
        // The card name and the authored text already existed in the engine;
        // this layer used to drop them.
        var signal = new NarrativeSignalItem("TAROT", Dimension.CAREER, "Su nghiep", Polarity.SUPPORT, "Ung ho",
                Strength.STRONG, "Manh", false, "the-fool", "The Fool (ngược)",
                "Khoi dau moi, nhung can chuan bi ky hon.");
        NarrativeInput input = new NarrativeInput("Su nghiep", null, null, Set.of(), Map.of(), List.of(signal),
                List.of(), List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.keySignals()).singleElement().satisfies(text -> assertThat(text)
                .contains("TAROT")
                .contains("The Fool (ngược)")
                .contains("Khoi dau moi, nhung can chuan bi ky hon.")
                .contains("Su nghiep")
                .contains("Ung ho"));
    }

    @Test
    @DisplayName("A signal with no authored text renders as its categories alone, never padded out")
    void keySignalWithoutAuthoredTextIsNotFabricated() {
        // Rule C. An engine with no interpretive corpus must produce a shorter
        // line, not an invented one, and must not print a dangling separator
        // where the missing text would have gone.
        var signal = new NarrativeSignalItem("BAZI", Dimension.CAREER, "Su nghiep", Polarity.CAUTION, "Than trong",
                Strength.STRONG, "Manh", false, "tag", null, null);
        NarrativeInput input = new NarrativeInput("Su nghiep", null, null, Set.of(), Map.of(), List.of(signal),
                List.of(), List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.AI_DISABLED);

        assertThat(response.keySignals()).singleElement().satisfies(text -> assertThat(text)
                .isEqualTo("BAZI: Su nghiep - Than trong (Manh)"));
    }

    @Test
    void asksAReflectionQuestionPerConflictInvolvingTwoOrMoreEngines() {
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"), "mo ta");
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(conflict),
                List.of(), List.of(), Map.of());

        NarrativeResponse response = HardDataNarrativeFallback.build(input, FallbackReason.TIMEOUT);

        assertThat(response.reflectionQuestions()).anySatisfy(q ->
                assertThat(q).contains("TAROT").contains("BAZI"));
    }
}
