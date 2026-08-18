package io.destinyos.engines.numerology;

import java.util.Objects;

/**
 * The full set of numbers computed for one person (Master Spec section 16).
 *
 * @param normalizedName the name actually used for the name-based numbers —
 *                       always reported, per "không silently thay đổi tên"
 * @param lifePath       from the birth date only
 * @param expression     sum of every letter in {@code normalizedName}
 * @param soulUrge       sum of the vowels
 * @param personality    sum of the consonants
 * @param birthday       the day-of-month component
 */
public record NumerologyProfile(
        NormalizedName normalizedName,
        NumerologyResult lifePath,
        NumerologyResult expression,
        NumerologyResult soulUrge,
        NumerologyResult personality,
        NumerologyResult birthday
) {
    public NumerologyProfile {
        Objects.requireNonNull(normalizedName, "normalizedName");
        Objects.requireNonNull(lifePath, "lifePath");
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(soulUrge, "soulUrge");
        Objects.requireNonNull(personality, "personality");
        Objects.requireNonNull(birthday, "birthday");
    }
}
