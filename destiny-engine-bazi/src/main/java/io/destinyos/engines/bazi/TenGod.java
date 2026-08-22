package io.destinyos.engines.bazi;

/**
 * Thập Thần — the ten relational roles a Heavenly Stem can take relative to
 * the Day Master (Nhật Chủ, the day pillar's stem).
 *
 * <p><strong>This enum is structure, not interpretation.</strong> Which Thập
 * Thần a stem is follows mechanically from two facts that no school disputes:
 * the Ngũ Hành relation between the two stems, and whether their Âm Dương
 * polarity matches. What each Thập Thần <em>means</em> for a person's career,
 * wealth or relationships is a different question entirely, and it is not
 * answered anywhere in this module — that reading depends on Day Master
 * strength (research item R3) and on the Dụng Thần school (R1), both open.
 *
 * <p>Sources for the derivation rule, cross-checked:
 * <ul>
 *   <li>Vietnamese: "Thập thần" (phongthuykhaitoan.com/thap-than/) —
 *       "cái ta sinh cùng âm dương là Thực Thần, khác âm dương là Thương
 *       Quan; cái sinh ta cùng âm dương là Thiên Ấn, khác âm dương là
 *       Chính Ấn".</li>
 *   <li>English: Oracleeast, "Ten Gods in BaZi: Correct Polarity Table"
 *       and bazi-web.com, "Direct Wealth (Zheng Cai) vs. Indirect Wealth
 *       (Pian Cai)" — same polarity yields the 偏 ("biased") member of each
 *       pair, opposite polarity the 正 ("proper") member.</li>
 * </ul>
 * Both retrieved 2026-08-22. They agree on all ten cases.
 */
public enum TenGod {
    /** Tỷ Kiên (比肩) — same element, same polarity. */
    TY_KIEN,
    /** Kiếp Tài (劫財) — same element, opposite polarity. */
    KIEP_TAI,
    /** Thực Thần (食神) — Day Master generates it, same polarity. */
    THUC_THAN,
    /** Thương Quan (傷官) — Day Master generates it, opposite polarity. */
    THUONG_QUAN,
    /** Thiên Tài (偏財) — Day Master controls it, same polarity. */
    THIEN_TAI,
    /** Chính Tài (正財) — Day Master controls it, opposite polarity. */
    CHINH_TAI,
    /** Thất Sát (七殺, also Thiên Quan) — controls the Day Master, same polarity. */
    THAT_SAT,
    /** Chính Quan (正官) — controls the Day Master, opposite polarity. */
    CHINH_QUAN,
    /** Thiên Ấn (偏印, also Kiêu Thần) — generates the Day Master, same polarity. */
    THIEN_AN,
    /** Chính Ấn (正印) — generates the Day Master, opposite polarity. */
    CHINH_AN
}
