package io.destinyos.testing;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.ResearchReference;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Stub engines for exercising the execution harness.
 *
 * <p>These are test doubles, not metaphysics. They contain no calculation of
 * any kind, which is what lets the entire concurrency and isolation contract be
 * verified in Phase 1 - before any real engine exists, and therefore before
 * there is any temptation to test the harness against an unverified algorithm.
 */
public final class StubEngines {

    private StubEngines() {
    }

    /** Completes immediately with one usable signal. */
    public static MetaphysicalEngine<String, String> succeeding(String engineId) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                Signal signal = new Signal(
                        engineId + "-sig-1", engineId, "TEST_SCHOOL", Dimension.DECISION,
                        "DECISION_SUPPORT", Polarity.SUPPORT, Strength.MEDIUM,
                        Applicability.HIGH, false, List.of(), null);
                return EngineResult.success("ok:" + input, List.of(), List.of(signal));
            }
        };
    }

    /** Blocks past any sane timeout, to exercise cancellation. */
    public static MetaphysicalEngine<String, String> hanging(String engineId, CountDownLatch started) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                started.countDown();
                try {
                    // Long enough that the harness timeout always wins.
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return EngineResult.success("never-returned", List.of(), List.of());
            }
        };
    }

    /**
     * Succeeds, but only after {@code duration} - so a test can distinguish an
     * engine that is genuinely over budget from one that merely waited its
     * turn for a concurrency permit.
     */
    public static MetaphysicalEngine<String, String> slowSucceeding(String engineId,
                                                                    java.time.Duration duration) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                try {
                    TimeUnit.NANOSECONDS.sleep(duration.toNanos());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return EngineResult.success("ok:" + input, List.of(), List.of());
            }
        };
    }

    /** Throws, to exercise Rule F isolation. */
    public static MetaphysicalEngine<String, String> throwing(String engineId) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                throw new IllegalStateException("deliberate stub failure");
            }
        };
    }

    /** Returns the honest non-answer an unresolved methodology should give. */
    public static MetaphysicalEngine<String, String> researchBlocked(String engineId,
                                                                     String researchId) {
        return new Stub(engineId, MethodologyStatus.RESEARCH_REQUIRED) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                return EngineResult.researchRequired(ResearchReference.of(
                        researchId, "TEST", "Methodology not verified for this stub."));
            }
        };
    }

    /** Declines because it does not apply here. Must never count as neutral. */
    public static MetaphysicalEngine<String, String> notApplicable(String engineId) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                return EngineResult.notApplicable("Stub does not apply to this scenario.");
            }
        };
    }

    /** Returns null, to prove the harness tolerates a defective engine. */
    public static MetaphysicalEngine<String, String> returningNull(String engineId) {
        return new Stub(engineId) {
            @Override
            public EngineResult<String> calculate(String input, CalculationContext context) {
                return null;
            }
        };
    }

    private abstract static class Stub implements MetaphysicalEngine<String, String> {

        private final String engineId;
        private final MethodologyStatus status;

        Stub(String engineId) {
            this(engineId, MethodologyStatus.PRODUCTION_READY);
        }

        Stub(String engineId, MethodologyStatus status) {
            this.engineId = engineId;
            this.status = status;
        }

        @Override
        public ValidationResult validateInput(String input) {
            return input == null
                    ? ValidationResult.failed("NULL_INPUT", "Input is required.", engineId)
                    : ValidationResult.ok();
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
                    "1.0", "1.0", "TEST_SCHOOL", "test-fixture", status);
        }
    }
}
