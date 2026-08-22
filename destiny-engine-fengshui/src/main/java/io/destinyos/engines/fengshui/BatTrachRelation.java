package io.destinyos.engines.fengshui;

/**
 * The eight Bát Biến Du Niên relations between a person's life trigram and a
 * direction.
 *
 * <p>The four/four split into cát (auspicious) and hung (inauspicious), and the
 * ranking within each, are stated by the sources cited on
 * {@link BatTrachTable} — <em>"4 cung cát: Sinh khí, Diên Niên (thượng cát),
 * Thiên Y (trung cát), Phục vị (tiểu cát); 4 cung xấu: Họa hại, Lục sát và
 * Ngũ Quỷ, Tuyệt Mệnh (đại hung)"</em>. They are recorded here rather than
 * invented, which is what lets this engine emit a signal polarity at all: the
 * tradition supplies the direction of each relation, so no polarity has to be
 * assigned by this project.
 */
public enum BatTrachRelation {

    /** Sinh Khí (生氣) — thượng cát. */
    SINH_KHI(true, Rank.MAJOR),
    /** Diên Niên (延年) — thượng cát. */
    DIEN_NIEN(true, Rank.MAJOR),
    /** Thiên Y (天醫) — trung cát. */
    THIEN_Y(true, Rank.MEDIUM),
    /** Phục Vị (伏位) — tiểu cát; also the relation of a trigram to itself. */
    PHUC_VI(true, Rank.MINOR),

    /** Hoạ Hại (禍害) — tiểu hung. */
    HOA_HAI(false, Rank.MINOR),
    /** Lục Sát (六煞) — thứ hung. */
    LUC_SAT(false, Rank.MEDIUM),
    /** Ngũ Quỷ (五鬼) — đại hung. */
    NGU_QUY(false, Rank.MAJOR),
    /** Tuyệt Mệnh (絕命) — đại hung. */
    TUYET_MENH(false, Rank.MAJOR);

    /**
     * How emphatic the tradition is about a relation. Deliberately three named
     * steps rather than a number: a numeric weight here would be the fabricated
     * score ADR D6 forbids, and the sources themselves speak in these terms
     * (thượng/trung/tiểu cát, đại/thứ/tiểu hung), not in figures.
     */
    public enum Rank {
        MINOR,
        MEDIUM,
        MAJOR
    }

    private final boolean auspicious;
    private final Rank rank;

    BatTrachRelation(boolean auspicious, Rank rank) {
        this.auspicious = auspicious;
        this.rank = rank;
    }

    /** Cát (true) or hung (false). */
    public boolean auspicious() {
        return auspicious;
    }

    public Rank rank() {
        return rank;
    }
}
