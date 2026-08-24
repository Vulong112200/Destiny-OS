package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import java.util.List;
import java.util.Set;

import static io.destinyos.calendar.EarthlyBranch.*;

/**
 * The branch-pair and branch-triple relation tables Thiệu Vĩ Hoa's Day
 * Master strength method needs (tam hội, tam hợp, lục hợp, lục xung).
 *
 * <p><strong>Scope note.</strong> This is a narrower, R3-specific subset of
 * what research item R20 (Hợp/Xung/Hình/Hại/Phá) is separately trying to
 * specify for Bát Tự's interpretive layer generally — R20 remains open
 * (`docs/RESEARCH_BLOCKERS.md`) because it also needs Hình/Hại/Phá and a
 * precedence rule covering every simultaneous-relation case, neither of
 * which R3's method uses. Kept local to this engine rather than promoted to
 * a shared module, the same way {@code Gender} and {@code SolarYear} stayed
 * local until a second consumer actually needed them (CLAUDE.md's own
 * "extract when a second consumer arrives" precedent) — if R20's own
 * implementation later needs the same tables, that is the point to share
 * them, not now on speculation.
 *
 * <p>Sources: tam hội/tam hợp/lục hợp/lục xung group membership is the
 * standard table appearing identically across every Bát Tự source consulted
 * across this project's research (R20's own drafts, `docs/research_drafts/`);
 * the lục hợp transformation targets for Mão-Tuất (hóa Hỏa) and Dần-Hợi
 * (hóa Mộc — implied by Ví dụ 2/3's "phu tòng thê hóa mộc") are directly
 * confirmed against Thiệu Vĩ Hoa's own worked examples. The remaining three
 * lục hợp targets (Tý-Sửu, Thìn-Dậu, Tị-Thân, Ngọ-Mùi) are the same
 * near-universal convention every source agrees on but were not individually
 * re-confirmed against a Thiệu Vĩ Hoa worked example in this pass — flagged
 * here rather than presented as equally verified.
 */
final class BranchRelations {

    private BranchRelations() {
    }

    /** Tam Hội (three-branch seasonal assembly). Order within a group does not matter. */
    static final List<Set<EarthlyBranch>> TAM_HOI = List.of(
            Set.of(TIGER, RABBIT, DRAGON),   // Dan Mao Thin - hoa Moc
            Set.of(SNAKE, HORSE, GOAT),      // Ti Ngo Mui - hoa Hoa
            Set.of(MONKEY, ROOSTER, DOG),    // Than Dau Tuat - hoa Kim
            Set.of(PIG, RAT, OX)             // Hoi Ty Suu - hoa Thuy
    );

    static FiveElement tamHoiElement(Set<EarthlyBranch> group) {
        if (group.equals(TAM_HOI.get(0))) return FiveElement.WOOD;
        if (group.equals(TAM_HOI.get(1))) return FiveElement.FIRE;
        if (group.equals(TAM_HOI.get(2))) return FiveElement.METAL;
        if (group.equals(TAM_HOI.get(3))) return FiveElement.WATER;
        throw new IllegalArgumentException("Not a Tam Hoi group: " + group);
    }

    /** Tam Hợp (three-branch elemental trine). Order within a group does not matter. */
    static final List<Set<EarthlyBranch>> TAM_HOP = List.of(
            Set.of(MONKEY, RAT, DRAGON),     // Than Ty Thin - hoa Thuy
            Set.of(SNAKE, ROOSTER, OX),      // Ti Dau Suu - hoa Kim
            Set.of(TIGER, HORSE, DOG),       // Dan Ngo Tuat - hoa Hoa
            Set.of(PIG, RABBIT, GOAT)        // Hoi Mao Mui - hoa Moc
    );

    static FiveElement tamHopElement(Set<EarthlyBranch> group) {
        if (group.equals(TAM_HOP.get(0))) return FiveElement.WATER;
        if (group.equals(TAM_HOP.get(1))) return FiveElement.METAL;
        if (group.equals(TAM_HOP.get(2))) return FiveElement.FIRE;
        if (group.equals(TAM_HOP.get(3))) return FiveElement.WOOD;
        throw new IllegalArgumentException("Not a Tam Hop group: " + group);
    }

    /**
     * Lục Hợp (six combinations, adjacent pairs only). Mão-Tuất -> Hỏa
     * confirmed directly (Ví dụ 5); the other five pairings' targets are the
     * standard convention, see class Javadoc.
     */
    record LucHopPair(EarthlyBranch a, EarthlyBranch b, FiveElement element) {
        boolean matches(EarthlyBranch x, EarthlyBranch y) {
            return (a == x && b == y) || (a == y && b == x);
        }
    }

    static final List<LucHopPair> LUC_HOP = List.of(
            new LucHopPair(RAT, OX, FiveElement.EARTH),
            new LucHopPair(TIGER, PIG, FiveElement.WOOD),
            new LucHopPair(RABBIT, DOG, FiveElement.FIRE),
            new LucHopPair(DRAGON, ROOSTER, FiveElement.METAL),
            new LucHopPair(SNAKE, MONKEY, FiveElement.WATER),
            new LucHopPair(HORSE, GOAT, FiveElement.FIRE)
    );

    /** Lục Xung (six clashes, adjacent pairs only). Universally agreed, no school variance found. */
    static final List<Set<EarthlyBranch>> LUC_XUNG = List.of(
            Set.of(RAT, HORSE), Set.of(OX, GOAT), Set.of(TIGER, MONKEY),
            Set.of(RABBIT, ROOSTER), Set.of(DRAGON, DOG), Set.of(SNAKE, PIG)
    );

    static boolean isLucXung(EarthlyBranch a, EarthlyBranch b) {
        // Set.of(a, b) throws on a duplicate element - two adjacent pillars
        // can share the same branch (e.g. two Ngo), and a branch never
        // clashes with itself, so that case is simply not a Luc Xung.
        if (a == b) {
            return false;
        }
        return LUC_XUNG.contains(Set.of(a, b));
    }
}
