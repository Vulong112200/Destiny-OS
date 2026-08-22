package io.destinyos.app.wiring;

import io.destinyos.api.service.EngineTaskFactory;
import io.destinyos.api.service.EngineTaskFactoryRegistry;
import io.destinyos.engines.bazi.BaziEngine;
import io.destinyos.engines.fengshui.FengShuiKuaEngine;
import io.destinyos.engines.numerology.NumerologyEngine;
import io.destinyos.engines.tarot.TarotEngine;
import io.destinyos.execution.EngineExecutor;
import io.destinyos.execution.EngineMetrics;
import io.destinyos.execution.ExecutionPolicy;
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

    /**
     * Bát Tự, Phase 8a. Registered under the engine id {@code BAZI}, which is
     * the id Master Spec section 7's two worked scenario examples already name -
     * so wiring it here is what finally makes BUSINESS and DAILY_ACTION stop
     * reporting BAZI as an unavailable engine. It contributes no signal yet
     * (R1/R2/R3), only chart evidence.
     */
    @Bean
    public BaziEngine baziEngine() {
        return new BaziEngine();
    }

    /**
     * Phong Thủy Bát Trạch, Phase 10. Registered under the engine id
     * {@code FENGSHUI_KUA}, which both of Master Spec section 7's worked
     * scenarios already name - so wiring it here is what stops BUSINESS and
     * DAILY_ACTION reporting it as unavailable. Unlike Bát Tự it does emit
     * signals, but only when the caller supplies a facing direction to judge.
     */
    @Bean
    public FengShuiKuaEngine fengShuiKuaEngine() {
        return new FengShuiKuaEngine();
    }

    /**
     * The harness, wired to real metrics (CLAUDE.md section 5, Phase 14).
     *
     * <p>{@code EngineExecutor.withDefaults()} still exists and still records
     * nothing - that is the right default for a unit test. Production is where
     * a metrics backend belongs, and this is the module that assembles
     * production.
     */
    @Bean
    public EngineExecutor engineExecutor(EngineMetrics engineMetrics) {
        return new EngineExecutor(ExecutionPolicy.defaults(), engineMetrics);
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
                                                               BaziEngine baziEngine,
                                                               FengShuiKuaEngine fengShuiKuaEngine,
                                                               TarotTaskFactory tarotTaskFactory,
                                                               NumerologyTaskFactory numerologyTaskFactory,
                                                               BaziTaskFactory baziTaskFactory,
                                                               FengShuiTaskFactory fengShuiTaskFactory) {
        Map<String, EngineTaskFactory> factories = new LinkedHashMap<>();
        factories.put(tarotEngine.engineId(), tarotTaskFactory);
        factories.put(numerologyEngine.engineId(), numerologyTaskFactory);
        factories.put(baziEngine.engineId(), baziTaskFactory);
        factories.put(fengShuiKuaEngine.engineId(), fengShuiTaskFactory);
        return new EngineTaskFactoryRegistry(factories);
    }
}
