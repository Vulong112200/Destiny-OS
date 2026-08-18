package io.destinyos.engines.numerology;

import java.time.LocalDate;

/**
 * Pure computation of the five numbers in {@link NumerologyProfile}
 * (research item R8, Master Spec section 16). Separated from
 * {@link NumerologyEngine} so the arithmetic can be tested without
 * constructing a {@code CalculationContext}.
 */
public final class PythagoreanCalculator {

    private static final String VOWELS = "AEIOU";

    private PythagoreanCalculator() {
    }

    public static NumerologyProfile compute(String fullName, LocalDate birthDate) {
        NormalizedName name = VietnameseNameNormalizer.normalize(fullName);

        return new NumerologyProfile(
                name,
                NumerologyResult.of(NumerologyNumberType.LIFE_PATH, lifePath(birthDate)),
                NumerologyResult.of(NumerologyNumberType.EXPRESSION, sumAllLetters(name.lettersOnly())),
                NumerologyResult.of(NumerologyNumberType.SOUL_URGE, sumVowels(name.lettersOnly())),
                NumerologyResult.of(NumerologyNumberType.PERSONALITY, sumConsonants(name.lettersOnly())),
                NumerologyResult.of(NumerologyNumberType.BIRTHDAY,
                        NumerologyReduction.reduce(birthDate.getDayOfMonth()))
        );
    }

    /**
     * Reduces month, day and year <em>separately</em>, then sums and reduces
     * once more (R8's adopted policy) — reducing the whole date as one
     * combined sum can lose a master number that only appears when a
     * component is reduced on its own.
     */
    static int lifePath(LocalDate birthDate) {
        int month = NumerologyReduction.reduce(birthDate.getMonthValue());
        int day = NumerologyReduction.reduce(birthDate.getDayOfMonth());
        int year = NumerologyReduction.reduce(sumOfDigits(birthDate.getYear()));

        return NumerologyReduction.reduce(month + day + year);
    }

    static int sumAllLetters(String lettersOnly) {
        int sum = 0;
        for (char c : lettersOnly.toCharArray()) {
            sum += PythagoreanLetterTable.valueOf(c);
        }
        return NumerologyReduction.reduce(sum);
    }

    static int sumVowels(String lettersOnly) {
        int sum = 0;
        for (char c : lettersOnly.toCharArray()) {
            if (VOWELS.indexOf(c) >= 0) {
                sum += PythagoreanLetterTable.valueOf(c);
            }
        }
        return NumerologyReduction.reduce(sum);
    }

    static int sumConsonants(String lettersOnly) {
        int sum = 0;
        for (char c : lettersOnly.toCharArray()) {
            // Y is always a consonant here: the syllable-dependent vowel/consonant
            // rule for Y has no sourced basis for a Latin-normalized Vietnamese
            // name (see RESEARCH_BLOCKERS.md R8 addendum). This is a labelled
            // simplification, not a researched conclusion.
            if (VOWELS.indexOf(c) < 0) {
                sum += PythagoreanLetterTable.valueOf(c);
            }
        }
        return NumerologyReduction.reduce(sum);
    }

    private static int sumOfDigits(int n) {
        int sum = 0;
        n = Math.abs(n);
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }
}
