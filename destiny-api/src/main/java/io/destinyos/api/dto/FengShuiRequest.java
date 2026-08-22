package io.destinyos.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input for the Phong Thủy Bát Trạch engine (Phase 10).
 *
 * <p>Present in a {@link ScenarioRunRequest} only when the caller wants Bát
 * Trạch to participate.
 *
 * @param birthDate       local calendar date of birth in Vietnam. Required
 * @param birthTime       local time, or {@code null}. Unlike Bát Tự, Bát Trạch
 *                        needs the hour only for a birth within minutes of the
 *                        Lập Xuân instant, so omitting it costs almost nothing —
 *                        but the engine still uses it when given rather than
 *                        assuming a time
 * @param gender          {@code MALE} or {@code FEMALE} (case-insensitive),
 *                        <strong>required</strong>: the male and female Kua
 *                        formulas differ and are not symmetric, so there is no
 *                        neutral default to fall back on. A missing value is an
 *                        honest 400, not a guess
 * @param region          {@code NORTH}, {@code SOUTH} or {@code UNKNOWN}
 * @param longitude       birth longitude in degrees east, or {@code null}
 * @param facingDirection the direction of the house, office or room being
 *                        assessed — {@code NORTH}, {@code NORTHEAST},
 *                        {@code EAST}, {@code SOUTHEAST}, {@code SOUTH},
 *                        {@code SOUTHWEST}, {@code WEST}, {@code NORTHWEST}
 *                        (case-insensitive) — or {@code null}. Without it the
 *                        engine returns the eight-direction profile and no
 *                        signal: Bát Trạch judges a person <em>against a
 *                        direction</em>, and there is nothing to judge yet
 */
public record FengShuiRequest(
        LocalDate birthDate,
        LocalTime birthTime,
        String gender,
        String region,
        Double longitude,
        String facingDirection
) {
}
