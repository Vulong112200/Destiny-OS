package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AI_NARRATIVE_SPEC.md section 5: "Validate schema truoc khi render." */
class NarrativeResponseParserTest {

    private final NarrativeResponseParser parser = new NarrativeResponseParser(new ObjectMapper());

    @Test
    void parsesWellFormedJson() {
        String json = """
                {"summary": "Tom tat", "keySignals": ["a"], "conflicts": [], "cautions": ["c"],
                 "reflectionQuestions": ["q?"]}
                """;

        Optional<NarrativeResponse> parsed = parser.parse(json);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().summary()).isEqualTo("Tom tat");
        assertThat(parsed.get().keySignals()).containsExactly("a");
        assertThat(parsed.get().cautions()).containsExactly("c");
    }

    @Test
    void stripsMarkdownCodeFenceBeforeParsing() {
        String fenced = "```json\n{\"summary\": \"Tom tat\"}\n```";

        Optional<NarrativeResponse> parsed = parser.parse(fenced);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().summary()).isEqualTo("Tom tat");
    }

    @Test
    void rejectsMissingSummaryAsNotWellFormed() {
        Optional<NarrativeResponse> parsed = parser.parse("{\"keySignals\": [\"a\"]}");

        assertThat(parsed).isEmpty();
    }

    @Test
    void rejectsBlankSummary() {
        Optional<NarrativeResponse> parsed = parser.parse("{\"summary\": \"   \"}");

        assertThat(parsed).isEmpty();
    }

    @Test
    @DisplayName("A summary that echoes the schema template is rejected")
    void rejectsAContentFreeSummary() {
        // Measured in production: a free model returned the response schema
        // back verbatim. "..." is not blank, so the old !isBlank() rule passed
        // it and the user saw dots labelled as an AI reading.
        assertThat(parser.parse("{\"summary\": \"...\"}")).isEmpty();
        // The single-character ellipsis, which a model is just as likely to
        // emit and which no "length >= 3" rule would ever have caught.
        assertThat(parser.parse("{\"summary\": \"…\"}")).isEmpty();
        assertThat(parser.parse("{\"summary\": \"—\"}")).isEmpty();
        assertThat(parser.parse("{\"summary\": \"- - -\"}")).isEmpty();
        assertThat(parser.parse("{\"summary\": \"12 / 34\"}")).isEmpty();
    }

    @Test
    @DisplayName("A real Vietnamese summary is accepted, punctuation and digits included")
    void acceptsRealVietnameseText() {
        // The risk of over-tightening. Vietnamese carries a lot in few words,
        // so a terse but genuine summary must render - which is exactly why
        // the rule is "contains a letter" and not a length floor.
        assertThat(parser.parse("{\"summary\": \"Chưa đủ dữ liệu.\"}"))
                .get().extracting(NarrativeResponse::summary).isEqualTo("Chưa đủ dữ liệu.");
        assertThat(parser.parse("{\"summary\": \"Có 2 tín hiệu thuận lợi, 1 cần lưu ý...\"}")).isPresent();
        // Diacritics must count as letters - a [a-zA-Z] check would reject
        // this and pass "..." only by accident of the opposite kind.
        assertThat(parser.parse("{\"summary\": \"Ừ\"}")).isPresent();
    }

    @Test
    @DisplayName("Placeholder array entries are dropped, real ones kept")
    void dropsPlaceholderArrayEntriesWithoutDiscardingTheResponse() {
        // Dropping rather than rejecting: a model that writes a real summary
        // but pads the arrays has still produced something worth showing. What
        // must not happen is rendering a bullet list of dots.
        String mixed = """
                {"summary": "Tóm tắt thật", "keySignals": ["Tín hiệu thật", "...", "…"],
                 "conflicts": ["..."], "cautions": [], "reflectionQuestions": ["Câu hỏi thật?", "-"]}
                """;

        NarrativeResponse response = parser.parse(mixed).orElseThrow();

        assertThat(response.keySignals()).containsExactly("Tín hiệu thật");
        assertThat(response.conflicts()).isEmpty();
        assertThat(response.reflectionQuestions()).containsExactly("Câu hỏi thật?");
        assertThat(response.summary()).isEqualTo("Tóm tắt thật");
    }

    @Test
    @DisplayName("A null array entry is dropped rather than crashing the parse")
    void dropsNullArrayEntries() {
        // List.copyOf used to throw NullPointerException here, which the
        // parser swallowed as "unparsable" - the right outcome by accident,
        // and one that threw away an otherwise good narrative.
        NarrativeResponse response = parser
                .parse("{\"summary\": \"Tóm tắt thật\", \"keySignals\": [null, \"Thật\"]}")
                .orElseThrow();

        assertThat(response.keySignals()).containsExactly("Thật");
    }

    @Test
    void rejectsUnparsableJson() {
        assertThat(parser.parse("not json at all")).isEmpty();
    }

    @Test
    void rejectsBlankInput() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void salvagesJsonWrappedInProviderProse() {
        // Reversed from the original "reject any surrounding prose" rule after
        // measuring real free models, which routinely prepend a lead-in
        // sentence despite being told not to. The salvaged span still goes
        // through the same parse and isWellFormed gates, so this widens what
        // is *recovered*, not what is *accepted*.
        assertThat(parser.parse("Day la ket qua: {\"summary\": \"x\"}"))
                .get().extracting(NarrativeResponse::summary).isEqualTo("x");
    }

    @Test
    void salvagesJsonAfterAReasoningBlock() {
        String withThinking = "<think>Can nhac cac tin hieu...</think>\n"
                + "{\"summary\": \"Tom tat\", \"keySignals\": [\"a\"]}";

        assertThat(parser.parse(withThinking))
                .get().extracting(NarrativeResponse::summary).isEqualTo("Tom tat");
    }

    @Test
    void doesNotInventAResponseWhenTheWidenedSpanIsNotValidJson() {
        // The safety property that makes salvaging acceptable: widening the
        // span can only turn a rejection into an acceptance when what is
        // inside genuinely parses. Two objects, or prose with a stray brace,
        // produce invalid JSON and stay rejected - never a different, wrong
        // narrative quietly rendered as if the model had produced it.
        assertThat(parser.parse("{\"summary\": \"nhap\"} va {\"summary\": \"that\"}")).isEmpty();
        assertThat(parser.parse("Ket qua { chua xong")).isEmpty();
    }

    @Test
    void rejectsTruncatedJson() {
        // What an 800-token ceiling actually produced in production: the reply
        // stops mid-string with no closing brace. There is nothing to salvage
        // and nothing may be guessed - the fix is the token budget
        // (OpenRouterProperties#maxTokens), not a lenient parser.
        assertThat(parser.parse("{\"summary\": \"Tom tat dai bi cat giua chu")).isEmpty();
    }

    @Test
    void rejectsArrayFieldsReturnedAsObjectsRatherThanStrings() {
        // Measured provider behaviour: models mirror the input payload's shape
        // back, returning conflicts as objects with type/dimension/description.
        // NarrativeResponse declares List<String>, so this must not render.
        // The prompt now forbids it explicitly (NarrativePromptBuilder's
        // SYSTEM_PROMPT); this test pins the parser's half of that contract.
        String objectShaped = """
                {"summary": "Tom tat", "conflicts": [{"type": "DIRECT_CONFLICT", "description": "mo ta"}]}
                """;

        assertThat(parser.parse(objectShaped)).isEmpty();
    }
}
