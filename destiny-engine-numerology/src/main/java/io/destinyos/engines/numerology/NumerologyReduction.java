package io.destinyos.engines.numerology;

/**
 * The reduction rule shared by every Pythagorean number: keep summing digits
 * until the result is a single digit (1-9) or a master number (11, 22, 33),
 * which are preserved rather than reduced further.
 *
 * <p>Used both for the final reduction of any sum, and — per R8's adopted
 * policy — for reducing the month, day and year of a birth date
 * <em>separately</em> before summing them for Life Path, since reducing the
 * full date as one combined sum can lose a master number that only appears
 * when a component is reduced on its own.
 */
public final class NumerologyReduction {

    private NumerologyReduction() {
    }

    public static boolean isMasterNumber(int n) {
        return n == 11 || n == 22 || n == 33;
    }

    /** Reduces a non-negative integer to a single digit or a master number. */
    public static int reduce(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Cannot reduce a negative value: " + value);
        }
        int n = value;
        while (n > 9 && !isMasterNumber(n)) {
            n = sumOfDigits(n);
        }
        return n;
    }

    private static int sumOfDigits(int n) {
        int sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }
}
