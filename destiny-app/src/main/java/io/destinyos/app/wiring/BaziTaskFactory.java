package io.destinyos.app.wiring;

import io.destinyos.api.dto.BaziRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.Gender;
import io.destinyos.engines.bazi.BaziEngine;
import io.destinyos.engines.bazi.BaziInput;
import io.destinyos.execution.EngineTask;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * See {@link TarotTaskFactory} — the same split, for Bát Tự.
 *
 * <p>Two translations happen here and nowhere else, both of them decisions the
 * API layer must not make on its own:
 *
 * <ul>
 *   <li><strong>Missing time means UNKNOWN precision, not midnight.</strong>
 *       A null {@code birthTime} maps to {@link BirthTimePrecision#UNKNOWN}
 *       with a nominal midday instant used only to fix the calendar date.
 *       Midday, not midnight: the instant still has to land on the right
 *       calendar date after the timezone round-trip, and midnight sits inside
 *       the Giờ Tý window whose 23:00 boundary rolls the day pillar to the next
 *       date. The engine discards the time-of-day entirely at UNKNOWN
 *       precision, so this value never reaches a pillar — but it would have
 *       reached the date, which is why it is midday.</li>
 *   <li><strong>An unrecognised region is UNKNOWN, not a guess.</strong>
 *       R14b means an unknown region is a real answer for 1955-1975 births, so
 *       falling back to UNKNOWN lets the engine decline honestly instead of
 *       having this layer pick a side the research cannot support.</li>
 * </ul>
 */
@Component
public class BaziTaskFactory implements EngineTaskFactory {

    /**
     * Vietnam's civil timezone today. The engine re-derives the historical
     * offset for the birth date itself from {@code HistoricalTimezoneRuleTable}
     * (R14a) — this zone is used only to turn the caller's local date and time
     * into an instant, which is the same round-trip {@code CalendarEngine}
     * expects of its own callers.
     */
    private static final ZoneId REQUEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /** See this class's Javadoc: midday, deliberately, not midnight. */
    private static final LocalTime NOMINAL_TIME_WHEN_UNKNOWN = LocalTime.NOON;

    private final BaziEngine engine;

    public BaziTaskFactory(BaziEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        BaziRequest bazi = request.bazi();
        if (bazi == null || bazi.birthDate() == null) {
            return Optional.empty();
        }

        boolean timeKnown = bazi.birthTime() != null;
        LocalDateTime local = LocalDateTime.of(bazi.birthDate(),
                timeKnown ? bazi.birthTime() : NOMINAL_TIME_WHEN_UNKNOWN);

        var input = new BaziInput(
                local.atZone(REQUEST_ZONE).toInstant(),
                parseRegion(bazi.region()),
                bazi.longitude(),
                timeKnown ? BirthTimePrecision.EXACT : BirthTimePrecision.UNKNOWN,
                parseGender(bazi.gender()));

        return Optional.of(EngineTask.of(engine, input));
    }

    /**
     * Null for absent or unrecognised, and the task is still created.
     *
     * <p>Deliberately not the same call as {@link FengShuiTaskFactory}'s: there
     * a missing gender means no task at all, because the Kua number <em>is</em>
     * the result. Here it costs only the Đại Vận section, and suppressing the
     * whole Tứ Trụ over it would withhold a chart the engine can build
     * perfectly well.
     */
    private static Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Same reasoning as parseRegion: "unrecognised" and "not supplied"
            // lead to the same honest place, and the engine states that place.
            return null;
        }
    }

    private static VietnameseRegion parseRegion(String region) {
        if (region == null || region.isBlank()) {
            return VietnameseRegion.UNKNOWN;
        }
        try {
            return VietnameseRegion.valueOf(region.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Deliberately not a 400. "I don't recognise that region" and
            // "the user doesn't know their region" lead to the same honest
            // place, and R14b makes that place a valid result.
            return VietnameseRegion.UNKNOWN;
        }
    }
}
