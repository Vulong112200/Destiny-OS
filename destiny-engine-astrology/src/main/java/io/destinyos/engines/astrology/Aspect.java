package io.destinyos.engines.astrology;

import java.util.Objects;

/**
 * One aspect (angular relationship) found between two chart points, within
 * R6's declared orb for that {@link AspectType}.
 *
 * @param bodyA                 first body's name (e.g. {@code "SUN"})
 * @param bodyB                 second body's name — {@code bodyA < bodyB}
 *                              lexically is not guaranteed; pairs are built
 *                              in a fixed enumeration order, not sorted
 * @param type                  which of the five Ptolemaic aspects
 * @param exactAngleDegrees     the aspect's exact angle (0/60/90/120/180)
 * @param actualAngleDegrees    the real angular separation between the two
 *                              bodies right now, in [0, 180]
 * @param orbDegrees            {@code |actualAngleDegrees - exactAngleDegrees|}
 * @param orbLimitDegrees       the orb limit that applied (R6's table,
 *                              widened if either body is the Sun or Moon)
 * @param applying              true if the orb is shrinking (the aspect is
 *                              forming), false if growing (separating) —
 *                              pure geometry per R6's decision text, not
 *                              scored or weighted
 */
public record Aspect(
        String bodyA,
        String bodyB,
        AspectType type,
        double exactAngleDegrees,
        double actualAngleDegrees,
        double orbDegrees,
        double orbLimitDegrees,
        boolean applying
) {
    public Aspect {
        Objects.requireNonNull(bodyA, "bodyA");
        Objects.requireNonNull(bodyB, "bodyB");
        Objects.requireNonNull(type, "type");
    }
}
