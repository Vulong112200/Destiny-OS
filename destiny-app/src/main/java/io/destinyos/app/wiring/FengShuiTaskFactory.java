package io.destinyos.app.wiring;

import io.destinyos.api.dto.FengShuiRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.calendar.VietnameseRegion;
import io.destinyos.engines.fengshui.CompassDirection;
import io.destinyos.engines.fengshui.FengShuiKuaEngine;
import io.destinyos.engines.fengshui.FengShuiKuaInput;
import io.destinyos.core.context.Gender;
import io.destinyos.execution.EngineTask;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * See {@link TarotTaskFactory} — the same split, for Phong Thủy Bát Trạch.
 *
 * <p>Three translations happen here, and the interesting thing about them is how
 * differently they treat a missing value:
 *
 * <ul>
 *   <li><strong>Gender: no default, ever.</strong> An unparseable or absent
 *       gender produces {@code Optional.empty()} — the engine is simply not
 *       run — rather than a default. The male and female Kua formulas are
 *       different and not symmetric, so any default would silently give half of
 *       users another person's Kua number. That is the one input here where
 *       guessing produces a confident wrong answer rather than a degraded one.</li>
 *   <li><strong>Facing direction: absent is meaningful.</strong> Null means "I
 *       have no direction to assess", and the engine answers with the
 *       eight-direction profile and no signal. Nothing needs defaulting.</li>
 *   <li><strong>Region: absent means UNKNOWN.</strong> R14b makes that a real
 *       answer for 1955-1975 births, so falling back to it lets the engine
 *       decline honestly instead of having this layer pick a side.</li>
 * </ul>
 */
@Component
public class FengShuiTaskFactory implements EngineTaskFactory {

    /** See {@link BaziTaskFactory} for why the request zone is fixed here. */
    private static final ZoneId REQUEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Midday when no time is given. Unlike Bát Tự, Bát Trạch has no hour-branch
     * boundary to fall foul of — but the Kua year still turns in early February,
     * so the nominal time must not push the instant onto the wrong calendar date
     * after the timezone round-trip. Midday cannot.
     */
    private static final LocalTime NOMINAL_TIME_WHEN_UNKNOWN = LocalTime.NOON;

    private final FengShuiKuaEngine engine;

    public FengShuiTaskFactory(FengShuiKuaEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        FengShuiRequest fengShui = request.fengShui();
        if (fengShui == null || fengShui.birthDate() == null) {
            return Optional.empty();
        }

        Gender gender = parseGender(fengShui.gender());
        if (gender == null) {
            // Declining to run is the honest outcome. The alternative - running
            // with a defaulted gender - would return a Kua number that looks
            // exactly as authoritative as a correct one.
            return Optional.empty();
        }

        LocalDateTime local = LocalDateTime.of(fengShui.birthDate(),
                fengShui.birthTime() != null ? fengShui.birthTime() : NOMINAL_TIME_WHEN_UNKNOWN);

        var input = new FengShuiKuaInput(
                local.atZone(REQUEST_ZONE).toInstant(),
                gender,
                parseRegion(fengShui.region()),
                fengShui.longitude(),
                parseDirection(fengShui.facingDirection()));

        return Optional.of(EngineTask.of(engine, input));
    }

    private static Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
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
            return VietnameseRegion.UNKNOWN;
        }
    }

    private static CompassDirection parseDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }
        try {
            return CompassDirection.valueOf(direction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Same reasoning as a missing direction: the engine returns the
            // profile without judging a direction it does not recognise, rather
            // than judging a direction the caller did not mean.
            return null;
        }
    }
}
