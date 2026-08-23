package io.destinyos.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input for the Western astrology engine (Phase 11).
 *
 * <p>Present in a {@link ScenarioRunRequest} only when the caller wants this
 * engine to participate. Its absence is not an error.
 *
 * <p><strong>A real scope limitation, stated rather than hidden.</strong>
 * Unlike Bát Tự and Phong Thủy — Vietnam-specific methodologies where
 * assuming the birth happened under Vietnam civil time is simply correct —
 * Western astrology charts are drawn for birthplaces anywhere in the world.
 * This request still assumes {@code birthDate}/{@code birthTime} are
 * Vietnam civil time (converted the same way {@code BaziTaskFactory} does),
 * even though {@code latitude}/{@code longitude} can name any place on
 * Earth. That is correct for a Vietnam-based user asking about their own
 * birth, and wrong for someone entering a foreign birthplace's local time
 * directly — there is no timezone selector yet. Recorded here rather than
 * worked around, since a wrong Ascendant computed confidently is exactly
 * what CLAUDE.md Rule C exists to prevent.
 *
 * <p><strong>Why {@code birthTime} is not nullable here, unlike Bát Tự's.</strong>
 * {@code BaziRequest.birthTime} may be {@code null} because a Tứ Trụ chart
 * degrades gracefully to two pillars without an exact hour. The Ascendant
 * moves roughly 1° every 4 minutes, so an astrology chart without a precise
 * time has no meaningful angles or houses at all — {@link EngineCapability}'s
 * {@code requiresBirthTime() == true} reflects that, and the task factory
 * declines to create a task rather than guess a time.
 *
 * @param birthDate       local calendar date of birth, Vietnam civil time
 * @param birthTime       local time of birth, Vietnam civil time — required
 * @param latitudeDegrees birth latitude, positive north, in [-90, 90]
 * @param longitudeDegrees birth longitude, positive east, in [-180, 180]
 */
public record AstrologyRequest(
        LocalDate birthDate,
        LocalTime birthTime,
        Double latitudeDegrees,
        Double longitudeDegrees
) {
}
