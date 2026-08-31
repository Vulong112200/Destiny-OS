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

/** AI_NARRATIVE_SPEC.md sections 2 and 4. */
class NarrativePromptBuilderTest {

    @Test
    void systemMessageCarriesEveryForbiddenAction() {
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
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
    @DisplayName("The system message tells the model to answer the question, and to admit when it cannot")
    void systemMessageCarriesTheQuestionAnsweringContract() {
        // Feeding the question through without instructing the model to use it
        // would change nothing observable: a model handed a question and twenty
        // signals answers the signals. These three clauses are what make the
        // new field do any work, so they are asserted rather than assumed.
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        String system = NarrativePromptBuilder.build(input).messages().get(0).content();

        assertThat(system)
                .as("must name the question field and require a direct answer")
                .contains("\"question\"")
                .contains("trả lời thẳng vào câu hỏi")
                .as("authored meaning may be restated, never extended (Rule B/C)")
                .contains("không được viết thêm ý nghĩa mới")
                .as("\"the data cannot answer that\" must stay an allowed answer")
                .contains("dữ liệu hiện có không trả lời được điều đó");
    }

    @Test
    @DisplayName("The system message forbids returning the four arrays as arrays of objects")
    void systemMessageRequiresPlainStringArrays() {
        // Measured, not hypothetical: free models mirror the input payload's
        // shape back and return conflicts as [{"type":..,"description":..}].
        // NarrativeResponse declares List<String>, so Jackson rejects it and a
        // usable narrative is thrown away as MALFORMED_JSON. The example object
        // alone did not prevent this; naming the wrong shape did.
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());

        String system = NarrativePromptBuilder.build(input).messages().get(0).content();

        assertThat(system)
                .contains("mảng các CHUỖI thuần")
                .contains("không phải mảng đối tượng")
                .as("naming the exact wrong shape is what made this stick")
                .contains("\"type\"/\"dimension\"/\"description\"");
    }

    @Test
    void userMessageIsJsonContainingOnlyPrunedFields() {
        var signal = new NarrativeSignalItem("TAROT", Dimension.CAREER, "Su nghiep", Polarity.SUPPORT, "Ung ho",
                Strength.STRONG, "Manh", false, "the-fool", "The Fool (ngược)", "Y nghia da soan san.");
        var conflict = new NarrativeConflictItem("Xung dot truc tiep", "CAREER", List.of("TAROT", "BAZI"), "mo ta");
        NarrativeInput input = new NarrativeInput("Mo rong kinh doanh", "Toi co nen mo rong khong?",
                "Mo chi nhanh moi", Set.of(Dimension.CAREER),
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

        // The four fields that did not previously survive the trip. Without the
        // card name and its authored meaning the model can only write about a
        // category ("a favourable career signal"), which is exactly the
        // generic output this payload change exists to stop.
        assertThat(user)
                .contains("\"question\":\"Toi co nen mo rong khong?\"")
                .contains("\"focus\":\"Mo chi nhanh moi\"")
                .contains("\"title\":\"The Fool (ngược)\"")
                .contains("\"meaning\":\"Y nghia da soan san.\"");
    }

    @Test
    @DisplayName("An engine that authored no text sends null, never a filled-in substitute")
    void unauthoredSignalTextIsSentAsNull() {
        // Rule C at the last stage before the model: an absent meaning has to
        // arrive as an absence. A placeholder here would be indistinguishable
        // from authored content on the far side of the API call.
        var signal = new NarrativeSignalItem("BAZI", Dimension.CAREER, "Su nghiep", Polarity.CAUTION, "Than trong",
                Strength.STRONG, "Manh", false, "tag", null, null);
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(signal),
                List.of(), List.of(), List.of(), Map.of());

        String user = NarrativePromptBuilder.build(input).messages().get(1).content();

        assertThat(user)
                .contains("\"title\":null")
                .contains("\"meaning\":null")
                .contains("\"question\":null")
                .contains("\"focus\":null");
    }

    @Test
    void userMessageNeverLeaksRawCalculationTreeKeys() {
        NarrativeInput input = new NarrativeInput("Kich ban", null, null, Set.of(), Map.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());
        String user = NarrativePromptBuilder.build(input).messages().get(1).content();

        // Master Spec section 22: "Khong gui toan bo raw chart neu AI khong
        // can" - the pruned payload has exactly the seven top-level keys
        // AI_NARRATIVE_SPEC.md section 2 names, nothing broader like a full
        // Evidence.fact() tree or engine-internal identifiers.
        assertThat(user).doesNotContain("evidenceId").doesNotContain("ruleVersion").doesNotContain("evidenceGroupId");
    }
}
