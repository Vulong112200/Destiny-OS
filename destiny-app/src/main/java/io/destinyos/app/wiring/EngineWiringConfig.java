package io.destinyos.app.wiring;

import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.api.service.EngineTaskFactoryRegistry;
import io.destinyos.engines.numerology.NumerologyEngine;
import io.destinyos.engines.tarot.TarotEngine;
import io.destinyos.execution.EngineExecutor;
import io.destinyos.fusion.FusionEngine;
import io.destinyos.scenario.ScenarioEngine;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The one place in the whole application allowed to name every concrete
 * engine — this is what "assembly" means for {@code destiny-app}
 * (its own module Javadoc). Everything downstream (
 * {@code destiny-api}, {@code destiny-scenario}, {@code destiny-fusion})
 * only ever sees the SPI types this class hands out as beans.
 */
@Configuration
public class EngineWiringConfig {

    @Bean
    public TarotEngine tarotEngine() {
        return new TarotEngine();
    }

    @Bean
    public NumerologyEngine numerologyEngine() {
        return new NumerologyEngine();
    }

    @Bean
    public EngineExecutor engineExecutor() {
        return EngineExecutor.withDefaults();
    }

    @Bean
    public FusionEngine fusionEngine() {
        return new FusionEngine();
    }

    @Bean
    public ScenarioEngine scenarioEngine(EngineExecutor executor, FusionEngine fusion) {
        return new ScenarioEngine(executor, fusion);
    }

    /**
     * Keyed by engine id, not Spring bean name — the key an
     * {@code EngineTaskFactory} is registered under here is exactly the
     * string {@code ScenarioDefinition#applicableEngines()} uses to decide
     * whether a scenario wants this engine at all.
     */
    @Bean
    public EngineTaskFactoryRegistry engineTaskFactoryRegistry(TarotEngine tarotEngine,
                                                               NumerologyEngine numerologyEngine,
                                                               TarotTaskFactory tarotTaskFactory,
                                                               NumerologyTaskFactory numerologyTaskFactory) {
        Map<String, EngineTaskFactory> factories = new LinkedHashMap<>();
        factories.put(tarotEngine.engineId(), tarotTaskFactory);
        factories.put(numerologyEngine.engineId(), numerologyTaskFactory);
        return new EngineTaskFactoryRegistry(factories);
    }
}
