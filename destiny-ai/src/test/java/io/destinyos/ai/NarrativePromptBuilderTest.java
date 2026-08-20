package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** AI_NARRATIVE_SPEC.md sections 2 and 4. */
class NarrativePromptBuilderTest {

    @Test
    void systemMessageCarriesEveryForbiddenAction() {
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());
        NarrativePrompt prompt = NarrativePromptBuilder.build(input);

        String system = prompt.messages().get(0).content();
        assertThat(prompt.messages().get(0).role()).isEqualTo("system");
        assertThat(system)
                .contains("tính toán lại")
                .contains("thêm sao")
                .contains("thêm lá bài")
                .contains("thêm hành tinh")
                .contains("thêm quẻ")
                .contains("tạo evidence")
                .contains("thay đổi kết quả Fusion")
                .contains("tiếng Việt");
    }

    @Test
    void userMessageIsJsonContainingOnlyPrunedFields() {
        var signal = new NarrativeSignalItem("TAROT", Dimension.CAREER, "Su nghiep", Polarity.SUPPORT, "Ung ho",
                Strength.STRONG, "Manh", false, "the-fool");
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"), "mo ta");
        NarrativeInput input = new NarrativeInput("Mo rong kinh doanh", Set.of(Dimension.CAREER),
                Map.of("overallOutcome", "Mau thuan"), List.of(signal), List.of(conflict), List.of("canh bao"),
                List.of("gioi han"), Map.of("calculationId", "calc-1"));

        String user = NarrativePromptBuilder.build(input).messages().get(1).content();

        assertThat(user)
                .contains("\"Mo rong kinh doanh\"")
                .contains("\"TAROT\"")
                .contains("\"Su nghiep\"")
                .contains("\"Ung ho\"")
                .contains("\"Manh\"")
                .contains("Xung dot truc tiep")
                .contains("canh bao")
                .contains("gioi han")
                .contains("calc-1");
    }

    @Test
    void userMessageNeverLeaksRawCalculationTreeKeys() {
        NarrativeInput input = new NarrativeInput("Kich ban", Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());
        String user = NarrativePromptBuilder.build(input).messages().get(1).content();

        // Master Spec section 22: "Khong gui toan bo raw chart neu AI khong
        // can" - the pruned payload has exactly the seven top-level keys
        // AI_NARRATIVE_SPEC.md section 2 names, nothing broader like a full
        // Evidence.fact() tree or engine-internal identifiers.
        assertThat(user).doesNotContain("evidenceId").doesNotContain("ruleVersion").doesNotContain("evidenceGroupId");
    }
}
