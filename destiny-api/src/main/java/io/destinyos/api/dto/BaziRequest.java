package io.destinyos.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input for the Bát Tự engine (Phase 8a — chart construction).
 *
 * <p>Present in a {@link ScenarioRunRequest} only when the caller wants Bát Tự
 * to participate. Its absence is not an error.
 *
 * <p><strong>Why the time is separate from the date, and nullable.</strong>
 * Master Spec §2 forbids treating an unknown birth time as an exact one, and
 * this is where that rule has to start being honoured — a single
 * {@code LocalDateTime} field would force the caller to invent a time (almost
 * always midnight) just to fill the shape, and midnight is the single worst
 * value to invent: it sits inside the Giờ Tý window whose 23:00 boundary rolls
 * the day pillar over. Leaving {@code birthTime} null therefore means "not
 * known", and the engine returns year and month pillars only, saying so.
 *
 * @param birthDate  local calendar date of birth in Vietnam
 * @param birthTime  local time of birth, or {@code null} if not known — never
 *                   a placeholder
 * @param region     {@code NORTH}, {@code SOUTH} or {@code UNKNOWN}
 *                   (case-insensitive); omit or send {@code UNKNOWN} rather
 *                   than guessing. For 1955-1975 births an unknown region
 *                   makes the chart unresolvable (R14b) — which is the honest
 *                   outcome, not a bug
 * @param longitude  birth longitude in degrees east, or {@code null}. Supplying
 *                   it enables the R10 mean-solar-time correction; omitting it
 *                   uses civil clock time and records that it did
 * @param gender     {@code MALE} or {@code FEMALE} (case-insensitive), or
 *                   {@code null}. Used only for the Đại Vận direction (R2):
 *                   omit it and the Tứ Trụ still comes back in full, without
 *                   the luck cycles and saying why. Unlike Phong Thủy, where
 *                   gender gates the whole result, here it gates one section
 */
public record BaziRequest(
        LocalDate birthDate,
        LocalTime birthTime,
        String region,
        Double longitude,
        String gender
) {
}
