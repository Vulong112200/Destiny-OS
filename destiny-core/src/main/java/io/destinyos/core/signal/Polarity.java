package io.destinyos.core.signal;

/**
 * Direction of a {@link Signal} within one dimension.
 *
 * <p>Note what is deliberately absent: {@code NOT_APPLICABLE}. Applicability is
 * a separate axis ({@link Applicability}) precisely so that a non-applicable
 * engine can never be mistaken for a neutral vote. Master Spec §7 and
 * FUSION_ENGINE_SPEC §4 both state this, and conflating the two would silently
 * corrupt every vote count (audit risk RK7).
 */
public enum Polarity {
    /** The traditional reading is favourable for this dimension. */
    SUPPORT,
    /** The traditional reading counsels care. Not the same as NEGATIVE. */
    CAUTION,
    /** The traditional reading is unfavourable. */
    NEGATIVE,
    /** The engine applied, and found nothing that leans either way. */
    NEUTRAL
}
