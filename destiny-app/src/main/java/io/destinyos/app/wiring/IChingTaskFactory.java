package io.destinyos.app.wiring;

import io.destinyos.api.dto.IChingRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.engines.iching.CastingMethod;
import io.destinyos.engines.iching.IChingCastInput;
import io.destinyos.engines.iching.IChingEngine;
import io.destinyos.execution.EngineTask;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * See {@link TarotTaskFactory} for the general split. Unlike every other
 * factory in this package, this one has no gender/region/longitude to
 * translate — a hexagram casting needs no birth data — but it does have a
 * real methodological choice to translate honestly: which of the four
 * casting methods to run, and what each one requires.
 */
@Component
public class IChingTaskFactory implements EngineTaskFactory {

    private final IChingEngine engine;

    public IChingTaskFactory(IChingEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<EngineTask<?, ?>> createTask(ScenarioRunRequest request) {
        IChingRequest iching = request.iching();
        if (iching == null || iching.method() == null || iching.method().isBlank()) {
            return Optional.empty();
        }

        CastingMethod method = parseMethod(iching.method());
        if (method == null) {
            // An unrecognised method name is not the same as "not supplied" -
            // declining to run is the honest outcome, the same reasoning
            // FengShuiTaskFactory applies to an unrecognised facing direction.
            return Optional.empty();
        }

        IChingCastInput input = switch (method) {
            case THREE_COINS -> iching.seed() != null
                    ? IChingCastInput.threeCoins(iching.seed())
                    : IChingCastInput.threeCoins();
            case YARROW -> iching.seed() != null
                    ? IChingCastInput.yarrow(iching.seed())
                    : IChingCastInput.yarrow();
            case MAI_HOA_NUMBER -> {
                if (iching.upperNumber() == null || iching.lowerNumber() == null) {
                    // Both numbers are required together for this method
                    // (IChingEngine does not accept a single number in this
                    // version - see its class Javadoc) - declining to run
                    // rather than guessing a missing number.
                    yield null;
                }
                yield IChingCastInput.fromNumbers(iching.upperNumber(), iching.lowerNumber());
            }
            case MAI_HOA_TIME -> IChingCastInput.now();
        };

        if (input == null) {
            return Optional.empty();
        }

        return Optional.of(EngineTask.of(engine, input));
    }

    private static CastingMethod parseMethod(String method) {
        try {
            return CastingMethod.valueOf(method.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
