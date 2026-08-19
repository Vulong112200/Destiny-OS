package io.destinyos.app.wiring;

import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.TarotRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.engines.tarot.TarotDrawInput;
import io.destinyos.engines.tarot.TarotEngine;
import io.destinyos.engines.tarot.TarotSpread;
import io.destinyos.execution.EngineTask;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The one place allowed to know both {@code destiny-api}'s request shape
 * and {@code TarotEngine}'s concrete input type (see
 * {@link EngineTaskFactory}'s Javadoc for why this split exists).
 */
@Component
public class TarotTaskFactory implements EngineTaskFactory {

    private final TarotEngine engine;

    public TarotTaskFactory(TarotEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        TarotRequest tarot = request.tarot();
        if (tarot == null || tarot.spread() == null) {
            return Optional.empty();
        }

        TarotSpread spread;
        try {
            spread = TarotSpread.valueOf(tarot.spread().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Tarot spread '" + tarot.spread()
                    + "'. Valid values: " + java.util.Arrays.toString(TarotSpread.values()));
        }

        var input = new TarotDrawInput(spread, tarot.question(), tarot.seed(), null);
        return Optional.of(EngineTask.of(engine, input));
    }
}
