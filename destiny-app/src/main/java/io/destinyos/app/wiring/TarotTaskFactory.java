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

        // request.effectiveQuestion(), not tarot.question(): the question now
        // belongs to the run, and a caller that sends it in the request context
        // must not end up with a TarotDrawInput that records a different
        // question (or none) from the one persisted and narrated. The two can
        // only disagree if this reads the raw field, so it does not.
        //
        // TarotEngine does not currently read TarotDrawInput.question() at all -
        // the draw is a function of the seed and the spread, and a question
        // cannot be allowed to change which cards come up (Rule A). It is
        // carried on the input as the record of what was asked, which is
        // precisely why it has to be the same question everything else recorded.
        var input = new TarotDrawInput(spread, request.effectiveQuestion(), tarot.seed(), null);
        return Optional.of(EngineTask.of(engine, input));
    }
}
