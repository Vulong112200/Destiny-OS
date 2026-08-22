package io.destinyos.engines.bazi;

import io.destinyos.calendar.FiveElement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Integer counts of the Five Elements in a chart, kept in three separate
 * groups.
 *
 * <p><strong>The grouping is the point, and so is the absence of a total.</strong>
 * Every school counts elements; they do <em>not</em> agree on what to count.
 * Some count the four stems only, some add the four branches, some add the
 * hidden stems, some weight by pillar position or by season. Merging the three
 * groups into one number would silently pick one of those schools, and
 * weighting them would be the fabricated scoring scheme research item R3
 * exists to refuse (and that {@code ArchitectureRulesTest.noProbabilityInTheDomain}
 * would reject on sight, since any weighting needs a {@code double}).
 *
 * <p>So: three honest tallies, each a plain count, and no derived
 * "dominant element" or "missing element" verdict. A caller that wants
 * "chart is Fire-strong" needs R3 resolved first, not arithmetic performed
 * here.
 *
 * @param stems       one count per element across the four pillar stems
 * @param branches    one count per element across the four pillar branches
 * @param hiddenStems one count per element across every Tàng Can stem
 */
public record ElementTally(
        Map<FiveElement, Integer> stems,
        Map<FiveElement, Integer> branches,
        Map<FiveElement, Integer> hiddenStems
) {
    public ElementTally {
        stems = normalize(stems);
        branches = normalize(branches);
        hiddenStems = normalize(hiddenStems);
    }

    /** Zero-filled so that "absent" is representable as 0, not as a missing key. */
    private static Map<FiveElement, Integer> normalize(Map<FiveElement, Integer> source) {
        Objects.requireNonNull(source, "tally group");
        Map<FiveElement, Integer> filled = new EnumMap<>(FiveElement.class);
        for (FiveElement element : FiveElement.values()) {
            filled.put(element, source.getOrDefault(element, 0));
        }
        return Map.copyOf(filled);
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private final Map<FiveElement, Integer> stems = new EnumMap<>(FiveElement.class);
        private final Map<FiveElement, Integer> branches = new EnumMap<>(FiveElement.class);
        private final Map<FiveElement, Integer> hidden = new EnumMap<>(FiveElement.class);

        Builder addStem(FiveElement element) {
            stems.merge(element, 1, Integer::sum);
            return this;
        }

        Builder addBranch(FiveElement element) {
            branches.merge(element, 1, Integer::sum);
            return this;
        }

        Builder addHiddenStem(FiveElement element) {
            hidden.merge(element, 1, Integer::sum);
            return this;
        }

        ElementTally build() {
            return new ElementTally(stems, branches, hidden);
        }
    }
}
