package io.destinyos.core.context;

/**
 * Why a calculation carries unresolved uncertainty (ADR D3).
 *
 * <p>These are not errors. They record that the system knows the limits of what
 * it knows — the condition CLAUDE.md Rule C exists to protect, expressed as
 * data so it can travel to the user instead of being resolved away.
 */
public enum UncertaintyKind {
    /**
     * No sourced historical timezone rule covers this (date, region).
     * MUST NOT fall through to a default offset (ADR D3, research item R14).
     */
    HISTORICAL_TIMEZONE_RULE_UNKNOWN,
    /** Birth region unknown, and the applicable rule depends on region (R17). */
    BIRTH_REGION_UNKNOWN,
    /** Birth time is APPROXIMATE or UNKNOWN and the result is sensitive to it. */
    BIRTH_TIME_IMPRECISE,
    /** The instant sits close enough to a solar term boundary to be sensitive (R9). */
    SOLAR_TERM_BOUNDARY,
    /** The instant sits close to a day-rollover boundary, e.g. giờ Tý (R10). */
    DAY_BOUNDARY,
    /** Requested date falls outside the sourced dataset's validity range. */
    OUTSIDE_DATASET_RANGE,
    /** The methodology itself is unresolved for this case (CLAUDE.md Rule C). */
    METHODOLOGY_UNRESOLVED,
    /**
     * Birth longitude was not supplied, so true/mean solar time correction
     * (R10) was skipped in favor of civil clock time. Only affects results
     * sensitive to which side of an hour-branch boundary the birth time
     * falls on.
     */
    LONGITUDE_UNKNOWN,
    /**
     * An input a specific section of the result depends on was not supplied,
     * so that section is absent while the rest stands.
     *
     * <p>Distinct from {@link #METHODOLOGY_UNRESOLVED}: there the method is in
     * doubt, here the method is settled and the caller simply did not provide
     * what it consumes. Distinct from an {@code INVALID_INPUT} result too,
     * which rejects the whole calculation — this one keeps everything that did
     * not need the missing field. Bát Tự's Đại Vận without a gender is the
     * first case: the direction rule is closed, but running it on a guessed
     * gender would produce a full sequence that is wrong from its first period
     * and indistinguishable from a correct one.
     */
    REQUIRED_INPUT_MISSING
}
