package io.destinyos.calendar;

/**
 * The Five Elements (Ngũ Hành) and the two relation cycles every Eastern
 * methodology in this project shares.
 *
 * <p>Lives in {@code destiny-calendar} rather than in an engine on purpose.
 * The element of a Heavenly Stem or Earthly Branch is part of the Can Chi
 * system itself, not an interpretation layered on top of it — Giáp
 * <em>is</em> Yang Wood in every source consulted, with no school variation
 * of any kind. Putting it here also keeps Bát Tự, Tử Vi and Phong Thủy from
 * ever needing to depend on one another for it, which the ArchUnit rule
 * {@code enginesStayIndependent} forbids outright.
 *
 * <p>Source for both cycles: the standard Ngũ Hành Tương Sinh / Tương Khắc
 * ordering, universally stated and not disputed by any school —
 * Mộc → Hỏa → Thổ → Kim → Thủy → Mộc for generation (tương sinh), and
 * Mộc → Thổ → Thủy → Hỏa → Kim → Mộc for control (tương khắc).
 *
 * <p><strong>No numeric weight appears anywhere in this enum.</strong> Every
 * published hidden-stem or element-strength table attaches percentages
 * (60/30/10 and similar); ADR D6 forbids importing them, and
 * {@code ArchitectureRulesTest.noProbabilityInTheDomain} would reject them
 * even if someone tried. Element <em>counts</em> are integers and are honest;
 * element <em>strength</em> is research item R3 and is not computed at all.
 */
public enum FiveElement {
    /** Mộc. */
    WOOD,
    /** Hỏa. */
    FIRE,
    /** Thổ. */
    EARTH,
    /** Kim. */
    METAL,
    /** Thủy. */
    WATER;

    /** Tương sinh: the element this one produces. */
    public FiveElement generates() {
        return switch (this) {
            case WOOD -> FIRE;
            case FIRE -> EARTH;
            case EARTH -> METAL;
            case METAL -> WATER;
            case WATER -> WOOD;
        };
    }

    /** Tương khắc: the element this one overcomes. */
    public FiveElement controls() {
        return switch (this) {
            case WOOD -> EARTH;
            case EARTH -> WATER;
            case WATER -> FIRE;
            case FIRE -> METAL;
            case METAL -> WOOD;
        };
    }

    /** Inverse of {@link #generates()}: the element that produces this one. */
    public FiveElement generatedBy() {
        for (FiveElement candidate : values()) {
            if (candidate.generates() == this) {
                return candidate;
            }
        }
        throw new IllegalStateException("Generation cycle is not a permutation: " + this);
    }

    /** Inverse of {@link #controls()}: the element that overcomes this one. */
    public FiveElement controlledBy() {
        for (FiveElement candidate : values()) {
            if (candidate.controls() == this) {
                return candidate;
            }
        }
        throw new IllegalStateException("Control cycle is not a permutation: " + this);
    }

    /**
     * How {@code other} stands relative to this element. The five cases below
     * are exhaustive and mutually exclusive, which is what makes the Ten Gods
     * derivation in {@code destiny-engine-bazi} a total function rather than a
     * lookup table with holes in it.
     */
    public ElementRelation relationTo(FiveElement other) {
        if (other == this) {
            return ElementRelation.SAME;
        }
        if (generates() == other) {
            return ElementRelation.I_GENERATE;
        }
        if (controls() == other) {
            return ElementRelation.I_CONTROL;
        }
        if (generatedBy() == other) {
            return ElementRelation.GENERATES_ME;
        }
        return ElementRelation.CONTROLS_ME;
    }

    /** The five possible standings of one element relative to another. */
    public enum ElementRelation {
        /** Same element (đồng hành). */
        SAME,
        /** This element produces the other (ta sinh). */
        I_GENERATE,
        /** This element overcomes the other (ta khắc). */
        I_CONTROL,
        /** The other produces this element (sinh ta). */
        GENERATES_ME,
        /** The other overcomes this element (khắc ta). */
        CONTROLS_ME
    }
}
