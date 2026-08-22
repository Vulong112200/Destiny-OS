package io.destinyos.engines.fengshui;

import io.destinyos.core.context.Uncertainty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A Kua profile: the person's life trigram, their eight directions, and — when
 * the two year conventions disagree — the fact that there are two answers.
 *
 * <p>{@code trigram} is the Lập Xuân-based answer and {@code trigramByTet} the
 * Tết-based one. They are <strong>usually identical</strong>, and when they are,
 * {@link #boundaryConventionsAgree()} is true and the profile is as definite as
 * any other engine's. Only a birth in the Tết-to-Lập-Xuân window makes them
 * differ, and then neither is presented as the answer (research item R7).
 *
 * @param trigram          life trigram under {@link KuaYearBoundary#LAP_XUAN}
 * @param trigramByTet     life trigram under {@link KuaYearBoundary#LUNAR_NEW_YEAR}
 * @param lapXuanYear      the year the Lập Xuân convention used
 * @param tetYear          the year the Tết convention used
 * @param directions       all eight directions for {@code trigram}
 * @param facingDirection  the direction assessed, or {@code null} if none given
 * @param facingRelation   the relation of {@code facingDirection} to
 *                         {@code trigram}, or {@code null} when no direction was
 *                         given or the two conventions disagree — in the latter
 *                         case a single relation would have to pick a side
 * @param uncertainties    conditions that must reach the user (ADR D3)
 */
public record KuaProfile(
        Trigram trigram,
        Trigram trigramByTet,
        int lapXuanYear,
        int tetYear,
        Map<CompassDirection, BatTrachRelation> directions,
        CompassDirection facingDirection,
        BatTrachRelation facingRelation,
        List<Uncertainty> uncertainties
) {
    public KuaProfile {
        Objects.requireNonNull(trigram, "trigram");
        Objects.requireNonNull(trigramByTet, "trigramByTet");
        directions = directions == null ? Map.of() : Map.copyOf(directions);
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);

        if (facingRelation != null && facingDirection == null) {
            throw new IllegalArgumentException(
                    "A relation without a direction is meaningless: Bát Trạch relates a person "
                            + "to a direction, so one cannot exist without the other.");
        }
    }

    /**
     * Whether the two year conventions produce the same life trigram.
     *
     * <p>Compares the <em>trigram</em>, not the year: the Kua formula can map
     * two different years onto the same number, and in that case the user has a
     * definite answer and telling them about a dispute would be noise.
     */
    public boolean boundaryConventionsAgree() {
        return trigram == trigramByTet;
    }

    /** Đông tứ or Tây tứ, meaningful only when the conventions agree. */
    public Optional<TrigramGroup> group() {
        return boundaryConventionsAgree() ? Optional.of(trigram.group()) : Optional.empty();
    }

    public Optional<BatTrachRelation> facingRelationIfPresent() {
        return Optional.ofNullable(facingRelation);
    }

    /** The four cát directions of {@code trigram}, in compass order. */
    public List<CompassDirection> auspiciousDirections() {
        return directions.entrySet().stream()
                .filter(e -> e.getValue().auspicious())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /** The four hung directions of {@code trigram}, in compass order. */
    public List<CompassDirection> inauspiciousDirections() {
        return directions.entrySet().stream()
                .filter(e -> !e.getValue().auspicious())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }
}
