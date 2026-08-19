package io.destinyos.api.testing;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.ValidationResult;
import java.util.List;

/**
 * A minimal test double for {@link MetaphysicalEngine}, local to
 * {@code destiny-api}'s test sources so unit tests here never need a
 * dependency on any concrete {@code destiny-engine-*} module (the same
 * boundary {@link io.destinyos.api.service.EngineTaskFactory} enforces in
 * production code). Mirrors {@code destiny-app}'s
 * {@code io.destinyos.testing.StubEngines.succeeding}.
 */
public final class StubEngine implements MetaphysicalEngine<String, String> {

    private final String engineId;

    public StubEngine(String engineId) {
        this.engineId = engineId;
    }

    @Override
    public EngineResult<String> calculate(String input, CalculationContext context) {
        Signal signal = new Signal(
                engineId + "-sig-1", engineId, "TEST_SCHOOL", Dimension.DECISION,
                "DECISION_SUPPORT", Polarity.SUPPORT, Strength.MEDIUM,
                Applicability.HIGH, false, List.of(), null);
        return EngineResult.success("ok:" + input, List.of(), List.of(signal));
    }

    @Override
    public ValidationResult validateInput(String input) {
        return ValidationResult.ok();
    }

    @Override
    public EngineCapability capability() {
        return EngineCapability.builder()
                .dimensions(Dimension.DECISION)
                .deterministic(true)
                .build();
    }

    @Override
    public EngineMetadata metadata() {
        return new EngineMetadata(engineId, "Động cơ thử nghiệm", "TEST_METHODOLOGY",
                "1.0", "1.0", "TEST_SCHOOL", "test-fixture", MethodologyStatus.PRODUCTION_READY);
    }
}
