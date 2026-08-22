package io.destinyos.engines.fengshui;

import java.util.Objects;

/**
 * The Kua (cung phi) number from a birth year and gender.
 *
 * <p><strong>Sources</strong> (both retrieved 2026-08-22, and agreeing exactly,
 * including the discontinuity at the year 2000):
 * <ul>
 *   <li>Vietnamese: {@code hoc.kabala.vn/cung-phi-la-gi/}</li>
 *   <li>English: {@code wofs.com/how-to-calculate-your-kua-number/} with
 *       {@code fengshuimall.com/blog/feng-shui-kua}</li>
 * </ul>
 *
 * <p>Take the last two digits of the year, add them, and reduce to one digit:
 *
 * <table>
 *   <caption>Kua formula</caption>
 *   <tr><th></th><th>Before 2000</th><th>2000 onwards</th></tr>
 *   <tr><td>Male</td><td>{@code 10 − a}</td><td>{@code 9 − a}</td></tr>
 *   <tr><td>Female</td><td>{@code 5 + a}</td><td>{@code 6 + a}</td></tr>
 * </table>
 *
 * <p>The male and female formulas are <strong>not</strong> mirror images, and
 * research item R7 flags that asymmetry specifically as something a
 * plausible-looking simplification would smooth over. They are written out
 * separately below for that reason.
 *
 * <p><strong>The 5 case.</strong> There is no Kua 5 — 5 is the centre of the
 * Lạc Thư square and has no direction. Both sources give the same substitution:
 * males take Khôn (2), females take Cấn (8). Both are West group.
 *
 * <p>Spot-checked against an independently stated fact: a male born 1990 is
 * cung Khảm. {@code 9 + 0 = 9}, {@code 10 − 9 = 1} = Khảm.
 */
public final class KuaNumber {

    /** No Kua 5 exists; see this class's Javadoc. */
    private static final int CENTRE = 5;

    private KuaNumber() {
    }

    /**
     * @param year   the year whose Kua is wanted. <strong>Which</strong> year
     *               that is — Lập Xuân-based or Tết-based — is the caller's
     *               declared choice, and the one part of R7 still open; see
     *               {@link KuaYearBoundary}
     * @param gender required, and not defaultable: the formulas differ and are
     *               not symmetric
     */
    public static Trigram forYear(int year, Gender gender) {
        Objects.requireNonNull(gender, "gender");

        int a = reduceToSingleDigit(Math.abs(year % 100));
        boolean before2000 = year < 2000;

        int raw = switch (gender) {
            case MALE -> before2000 ? 10 - a : 9 - a;
            case FEMALE -> before2000 ? 5 + a : 6 + a;
        };

        int kua = normalize(raw, gender);
        return Trigram.ofKuaNumber(kua);
    }

    /**
     * Brings a raw formula result into 1-9 and applies the 5 substitution.
     *
     * <p>Three separate adjustments, each from the sources rather than from
     * arithmetic convenience:
     * <ul>
     *   <li>a female result above 9 is digit-reduced again (both sources say
     *       so explicitly);</li>
     *   <li>a male result of 0 is Ly (9) — the Vietnamese source states
     *       "nếu b = 0 thì lấy cung Ly", which arises for the 2000-onwards
     *       formula when {@code a == 9};</li>
     *   <li>5 becomes Khôn (2) for men and Cấn (8) for women.</li>
     * </ul>
     */
    private static int normalize(int raw, Gender gender) {
        int value = raw;
        if (value > 9) {
            value = reduceToSingleDigit(value);
        }
        if (value == 0) {
            value = 9;
        }
        if (value == CENTRE) {
            return gender == Gender.MALE ? 2 : 8;
        }
        return value;
    }

    /** Digit sum, repeated until a single digit remains. */
    private static int reduceToSingleDigit(int value) {
        int current = value;
        while (current > 9) {
            int sum = 0;
            for (int remaining = current; remaining > 0; remaining /= 10) {
                sum += remaining % 10;
            }
            current = sum;
        }
        return current;
    }
}
