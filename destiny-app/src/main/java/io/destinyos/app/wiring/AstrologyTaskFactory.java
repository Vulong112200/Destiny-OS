package io.destinyos.app.wiring;

import io.destinyos.api.dto.AstrologyRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.engines.astrology.WesternAstrologyEngine;
import io.destinyos.engines.astrology.WesternAstrologyInput;
import io.destinyos.execution.EngineTask;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * See {@link TarotTaskFactory} for the general split. Western astrology has no
 * "run with a degraded input" path: {@code birthDate}, {@code birthTime},
 * {@code latitudeDegrees} and {@code longitudeDegrees} are all required
 * (mirrors {@link WesternAstrologyEngine}'s {@code EngineCapability}, which
 * declares both {@code requiresBirthTime} and {@code requiresLocation} true).
 * Any one of them missing means declining to run rather than guessing — see
 * {@link AstrologyRequest}'s Javadoc for why guessing a time here is far worse
 * than guessing one for Bát Tự or Phong Thủy.
 */
@Component
public class AstrologyTaskFactory implements EngineTaskFactory {

    /**
     * See {@link BaziTaskFactory} for why the request zone is fixed here.
     * {@link AstrologyRequest}'s Javadoc records the resulting limitation:
     * this assumes the birth clock was Vietnam civil time even though the
     * birthplace coordinates could name anywhere on Earth.
     */
    private static final ZoneId REQUEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final WesternAstrologyEngine engine;

    public AstrologyTaskFactory(WesternAstrologyEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        AstrologyRequest astrology = request.astrology();
        if (astrology == null
                || astrology.birthDate() == null
                || astrology.birthTime() == null
                || astrology.latitudeDegrees() == null
                || astrology.longitudeDegrees() == null) {
            return Optional.empty();
        }

        LocalDateTime local = LocalDateTime.of(astrology.birthDate(), astrology.birthTime());

        var input = new WesternAstrologyInput(
                local.atZone(REQUEST_ZONE).toInstant(),
                astrology.latitudeDegrees(),
                astrology.longitudeDegrees());

        return Optional.of(EngineTask.of(engine, input));
    }
}
