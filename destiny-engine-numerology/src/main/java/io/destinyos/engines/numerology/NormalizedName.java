package io.destinyos.engines.numerology;

import java.util.Objects;

/**
 * The result of normalizing a Vietnamese name for Pythagorean numerology
 * (research item R8).
 *
 * <p>Master Spec section 16: "Không silently thay đổi tên." Carrying both
 * the original and the normalized form is what makes that rule enforceable —
 * a caller that only had {@link #lettersOnly()} would have no way to show
 * the user what happened to their name.
 *
 * @param original    the name exactly as entered
 * @param displayForm diacritics stripped, {@code đ}/{@code Đ} folded to
 *                    {@code d}/{@code D}, spacing and casing otherwise
 *                    preserved — suitable to show the user as "what we used"
 * @param lettersOnly {@code displayForm} with spaces, hyphens and
 *                    apostrophes removed and case folded to uppercase — the
 *                    actual input to {@link PythagoreanLetterTable}
 */
public record NormalizedName(String original, String displayForm, String lettersOnly) {
    public NormalizedName {
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(displayForm, "displayForm");
        Objects.requireNonNull(lettersOnly, "lettersOnly");
    }
}
