package io.destinyos.ai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parses and schema-validates a provider's raw text into a
 * {@link NarrativeResponse} (AI_NARRATIVE_SPEC.md section 5: "Validate
 * schema truoc khi render"). Never throws - any problem (unparsable JSON,
 * missing/blank {@code summary}) yields {@link Optional#empty()}, which
 * {@link NarrativeService} treats as {@link FallbackReason#MALFORMED_JSON}.
 */
final class NarrativeResponseParser {

    /**
     * Free LLM providers routinely wrap JSON in a markdown code fence even
     * when told not to ("```json ... ```" or bare "``` ... ```"). Stripping
     * this is a tolerance for provider behaviour, not a relaxation of the
     * schema check that follows - the content inside must still parse and
     * validate.
     */
    private static final Pattern CODE_FENCE = Pattern.compile(
            "^```(?:json)?\\s*|\\s*```$", Pattern.CASE_INSENSITIVE);

    /**
     * Reads exactly one JSON value and rejects anything after it.
     *
     * <p>{@code FAIL_ON_TRAILING_TOKENS} is off in Jackson by default, which
     * means a plain {@code readValue} on {@code {"summary":"draft"} {"summary":"final"}}
     * returns the <em>first</em> object and silently discards the rest. That is
     * the exact "parsing the wrong thing silently" failure this class has
     * always meant to avoid, and it was reachable before
     * {@link #outermostJsonObject} existed too - a model that emits a draft and
     * then a corrected answer would have had its draft rendered as the reading.
     * Turning the check on is what makes that method's safety argument true
     * rather than merely intended; a test pins it.
     *
     * <p>Derived with {@code readerFor} instead of configuring the injected
     * {@link ObjectMapper}: the mapper belongs to the caller and may be shared,
     * and quietly changing its global deserialization behaviour to suit this
     * one parse would be a side effect nobody at the call site can see.
     */
    private final ObjectReader reader;

    NarrativeResponseParser(ObjectMapper mapper) {
        this.reader = mapper.readerFor(NarrativeResponse.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    Optional<NarrativeResponse> parse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return Optional.empty();
        }

        String cleaned = CODE_FENCE.matcher(rawContent.strip()).replaceAll("").strip();

        try {
            NarrativeResponse response = reader.readValue(outermostJsonObject(cleaned));
            return response.isWellFormed() ? Optional.of(response) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * The span from the first {@code &#123;} to the last {@code &#125;}, or the
     * input unchanged when there is no such span.
     *
     * <p>This class previously refused any response with text around the JSON,
     * on the reasoning that salvaging embedded JSON "would risk parsing the
     * wrong thing silently". That reasoning was re-examined against real free
     * models, which routinely prepend a lead-in sentence or a
     * {@code <think>...</think>} block, and it does not hold: the extracted
     * span is not trusted, it is fed through exactly the same
     * {@code readValue} and {@link NarrativeResponse#isWellFormed()} gates as
     * before. Nothing is accepted here that would not have been accepted on
     * its own.
     *
     * <p>The "wrong thing" case is what makes this safe rather than merely
     * convenient. If the model emits prose containing a stray brace, the
     * widened span is not valid JSON at all and the caller returns empty,
     * precisely as it did before; if it emits two separate objects, the
     * trailing-token check on {@link #reader} rejects it rather than picking
     * one. To be salvaged, a response must contain exactly one syntactically
     * valid object carrying a non-blank {@code summary}; there is no input for
     * which this returns a <em>different</em> narrative rather than the same
     * one or none.
     *
     * <p>Deliberately still not tolerated: a response whose JSON is truncated
     * mid-string. That has no closing brace to find, and it must stay a
     * fallback - the fix for truncation is
     * {@code destiny.ai.openrouter.max-tokens}, not a parser that guesses at
     * how a sentence was going to end.
     */
    private static String outermostJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            // Leave it alone and let Jackson report the failure, rather than
            // inventing a substring that was never there.
            return text;
        }
        return text.substring(start, end + 1);
    }
}
