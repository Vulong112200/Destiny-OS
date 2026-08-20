package io.destinyos.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper mapper;

    NarrativeResponseParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    Optional<NarrativeResponse> parse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return Optional.empty();
        }

        String cleaned = CODE_FENCE.matcher(rawContent.strip()).replaceAll("").strip();

        try {
            NarrativeResponse response = mapper.readValue(cleaned, NarrativeResponse.class);
            return response.isWellFormed() ? Optional.of(response) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
