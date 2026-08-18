package io.destinyos.engines.numerology;

import java.text.Normalizer;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Vietnamese name normalization for Pythagorean numerology (research item
 * R8, adopted policy per {@code docs/RESEARCH_BLOCKERS.md}).
 *
 * <p>Two-step process, in this order, because the two problems are
 * genuinely different:
 * <ol>
 *   <li><strong>Unicode NFD decomposition + combining-mark strip.</strong>
 *       Handles every Vietnamese diacritic that Unicode encodes as a base
 *       letter plus a combining mark (the tone marks, the circumflex, the
 *       breve, the horn) — e.g. {@code ế → e}.</li>
 *   <li><strong>Explicit {@code đ}/{@code Đ} substitution.</strong> Required
 *       separately because {@code đ} (U+0111) has <em>no</em> canonical
 *       Unicode decomposition — it is encoded as an atomic distinct letter,
 *       the same category as {@code ø} or {@code ł}, not as a base letter
 *       with a combining stroke. Step 1 alone leaves it untouched.</li>
 * </ol>
 *
 * <p>This is standard, uncontroversial practice for Vietnamese text
 * processing generally — confirmed identically across independent Java and
 * Python implementations during R8's research — not a novel policy invented
 * for this project.
 */
public final class VietnameseNameNormalizer {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern ALLOWED_DISPLAY_CHARS =
            Pattern.compile("^[\\p{L} '\\-]*$");

    private VietnameseNameNormalizer() {
    }

    /**
     * Normalizes {@code name}.
     *
     * @throws IllegalArgumentException if, after normalization, the name
     *         still contains a character that is not a letter, space,
     *         hyphen or apostrophe — per the adopted policy, an
     *         unrecognised character is rejected rather than silently
     *         dropped
     */
    public static NormalizedName normalize(String name) {
        Objects.requireNonNull(name, "name");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }

        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        String stripped = COMBINING_MARKS.matcher(decomposed).replaceAll("");
        String displayForm = foldDStroke(stripped);

        if (!ALLOWED_DISPLAY_CHARS.matcher(displayForm).matches()) {
            throw new IllegalArgumentException(
                    "Name '" + name + "' contains a character not recognised after "
                            + "normalization ('" + displayForm + "'). Only letters, spaces, "
                            + "hyphens and apostrophes are accepted (research item R8).");
        }

        String lettersOnly = displayForm
                .replaceAll("[^\\p{L}]", "")
                .toUpperCase(java.util.Locale.ROOT);

        if (lettersOnly.isEmpty()) {
            throw new IllegalArgumentException("Name '" + name + "' contains no letters.");
        }

        return new NormalizedName(name, displayForm, lettersOnly);
    }

    /** Explicit substitution for the one Vietnamese letter NFD cannot touch. */
    private static String foldDStroke(String s) {
        return s.replace('đ', 'd').replace('Đ', 'D');
    }

    /** Non-throwing variant, for callers that validate before normalizing. */
    public static Optional<NormalizedName> tryNormalize(String name) {
        try {
            return Optional.of(normalize(name));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
