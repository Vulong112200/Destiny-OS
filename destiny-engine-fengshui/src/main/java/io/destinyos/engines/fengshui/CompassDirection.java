package io.destinyos.engines.fengshui;

/**
 * The eight compass directions Bát Trạch works in.
 *
 * <p>Eight, not sixteen or twenty-four: Bát Trạch divides a site into eight
 * 45° sectors, and the finer 24-mountain subdivision belongs to other schools
 * (Phi Tinh, Huyền Không) that Master Spec §20 forbids blending in.
 */
public enum CompassDirection {
    /** Bắc. */
    NORTH,
    /** Đông Bắc. */
    NORTHEAST,
    /** Đông. */
    EAST,
    /** Đông Nam. */
    SOUTHEAST,
    /** Nam. */
    SOUTH,
    /** Tây Nam. */
    SOUTHWEST,
    /** Tây. */
    WEST,
    /** Tây Bắc. */
    NORTHWEST
}
