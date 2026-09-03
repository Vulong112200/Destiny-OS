package io.destinyos.engines.astrology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds every aspect (of the five Ptolemaic aspects, R6) between pairs of
 * chart points whose angular separation falls within the applicable orb.
 *
 * <p><strong>Applying vs. separating is computed, not looked up.</strong> R6's
 * decision text: "Whether an aspect is closing or separating is computed and
 * stored — it is pure geometry, no convention involved." This class takes two
 * longitude snapshots (now, and a short time later) for every body and checks
 * whether the orb (distance from exact) is shrinking or growing between them
 * — a first-principles finite-difference derivative, not a source lookup.
 */
final class AspectFinder {

    private AspectFinder() {
    }

    /**
     * @param longitudesNow      body name -&gt; tropical ecliptic longitude
     *                           (degrees) at the chart's instant
     * @param longitudesShortlyAfter same bodies' longitudes a short, fixed
     *                           time later (used only to sign the applying/
     *                           separating direction, never to change which
     *                           aspects are found)
     * @param luminaries         names to treat as luminaries for the wider
     *                           orb (Sun, Moon)
     */
    static List<Aspect> findAll(Map<String, Double> longitudesNow,
                                 Map<String, Double> longitudesShortlyAfter,
                                 Set<String> luminaries) {
        List<String> bodies = List.copyOf(longitudesNow.keySet());
        List<Aspect> aspects = new ArrayList<>();
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                String a = bodies.get(i);
                String b = bodies.get(j);
                double sepNow = separationDegrees(longitudesNow.get(a), longitudesNow.get(b));
                double sepAfter = separationDegrees(longitudesShortlyAfter.get(a),
                        longitudesShortlyAfter.get(b));
                boolean involvesLuminary = luminaries.contains(a) || luminaries.contains(b);
                for (AspectType type : AspectType.values()) {
                    double orbLimit = type.orbDegrees(involvesLuminary);
                    double orbNow = Math.abs(sepNow - type.exactAngleDegrees());
                    if (orbNow > orbLimit) {
                        continue;
                    }
                    double orbAfter = Math.abs(sepAfter - type.exactAngleDegrees());
                    boolean applying = orbAfter < orbNow;
                    aspects.add(new Aspect(a, b, type, type.exactAngleDegrees(), sepNow, orbNow,
                            orbLimit, applying));
                }
            }
        }
        return List.copyOf(aspects);
    }

    /** Angular separation between two ecliptic longitudes, in [0, 180]. */
    private static double separationDegrees(double lonA, double lonB) {
        double diff = Math.abs(lonA - lonB) % 360.0;
        return diff > 180.0 ? 360.0 - diff : diff;
    }
}
