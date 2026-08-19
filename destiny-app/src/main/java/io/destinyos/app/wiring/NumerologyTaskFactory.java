package io.destinyos.app.wiring;

import io.destinyos.api.dto.NumerologyRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.engines.numerology.NumerologyEngine;
import io.destinyos.engines.numerology.NumerologyInput;
import io.destinyos.execution.EngineTask;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** See {@link TarotTaskFactory} — the same split, for Numerology. */
@Component
public class NumerologyTaskFactory implements EngineTaskFactory {

    private final NumerologyEngine engine;

    public NumerologyTaskFactory(NumerologyEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        NumerologyRequest numerology = request.numerology();
        if (numerology == null || numerology.fullName() == null || numerology.birthDate() == null) {
            return Optional.empty();
        }

        var input = new NumerologyInput(numerology.fullName(), numerology.birthDate());
        return Optional.of(EngineTask.of(engine, input));
    }
}
