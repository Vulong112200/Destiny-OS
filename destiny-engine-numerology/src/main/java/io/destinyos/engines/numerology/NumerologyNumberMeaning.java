package io.destinyos.engines.numerology;

import io.destinyos.core.signal.Polarity;
import java.util.List;
import java.util.Objects;

/**
 * Vietnamese interpretive content for one (NumerologyNumberType, value) pair.
 * Content authorship, not algorithm - grounded in the standard, widely
 * converged Pythagorean numerology meaning corpus, authored once as
 * versioned reference data rather than generated at runtime (CLAUDE.md
 * Rule B, the same discipline research item R11 applies to Tarot).
 *
 * @param keywords short Vietnamese keyword phrases capturing this number's core traits
 * @param meaning  one to two sentences interpreting this number in the context of its type
 * @param polarity how this number's traits read, on balance, for this specific type
 */
public record NumerologyNumberMeaning(List<String> keywords, String meaning, Polarity polarity) {
    public NumerologyNumberMeaning {
        Objects.requireNonNull(meaning, "meaning");
        Objects.requireNonNull(polarity, "polarity");
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
    }
}
