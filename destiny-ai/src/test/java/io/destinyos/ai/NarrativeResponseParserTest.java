package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
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
    void rejectsUnparsableJson() {
        assertThat(parser.parse("not json at all")).isEmpty();
    }

    @Test
    void rejectsBlankInput() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void rejectsExtraProseAroundJson() {
        // A provider ignoring "Khong them van ban nao ngoai doi tuong JSON
        // do" is a malformed response, not something this parser should
        // try to salvage by searching for embedded JSON - that would risk
        // parsing the wrong thing silently.
        assertThat(parser.parse("Day la ket qua: {\"summary\": \"x\"}")).isEmpty();
    }
}
