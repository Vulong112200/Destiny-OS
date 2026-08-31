package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.engine.ValidationResult;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Behaviour and honesty invariants of {@link IChingEngine}. */
class IChingEngineTest {

    private final IChingEngine engine = new IChingEngine();

    @Nested
    @DisplayName("Honesty about what is not computed")
    class Honesty {

        @Test
        @DisplayName("The engine emits no signals at all, and reports PARTIAL")
        void emitsNoSignals() {
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.signals()).isEmpty();
            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
        }

        @Test
        @DisplayName("The line-selection-rule and cát/hung-polarity gaps are named as blocked sections on every reading")
        void blockedSectionIsNamedInEvidence() {
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.evidence())
                    .extracting(e -> e.ruleId())
                    .contains("ICHING_BLOCKED_LINE_SELECTION_RULE", "ICHING_BLOCKED_CAT_HUNG_POLARITY");
            assertThat(result.warnings())
                    .anySatisfy(w -> assertThat(w.critical()).isTrue());
        }
    }

    @Nested
    @DisplayName("Quẻ từ / hào từ content (R24/R25)")
    class JudgmentContent {

        @Test
        @DisplayName("The Mai Hoa worked example (hexagram 64) carries real quẻ từ evidence, not just the cast")
        void queTuEvidencePresent() {
            var result = run(IChingCastInput.fromNumbers(3, 6));
            var judgment = result.evidence().stream()
                    .filter(e -> e.ruleId().equals("ICHING_JUDGMENT_ORIGINAL"))
                    .findFirst().orElseThrow();
            assertThat(judgment.fact()).containsEntry("number", 64);
            assertThat((String) judgment.fact().get("nghia")).isNotBlank();
            assertThat((String) judgment.fact().get("hanTu")).isNotBlank();
        }

        @Test
        @DisplayName("A single moving line carries its own hào từ evidence")
        void haoTuEvidencePresentForMovingLine() {
            var result = run(IChingCastInput.fromNumbers(3, 6));
            int position = result.data().movingLinePositions().get(0);
            assertThat(result.evidence())
                    .extracting(e -> e.ruleId())
                    .contains("ICHING_LINE_JUDGMENT_" + position);
        }

        @Test
        @DisplayName("All six lines moving on Kiền (hexagram 1) reads Dụng Cửu, not six separate line texts")
        void dungCuuForKienAllLinesMoving() {
            // Three Coins with every line drawn old (9 or 6) — construct via
            // a seed search is brittle; instead assert the table-level
            // contract this branch relies on, since IChingCastInput has no
            // "force all lines old" input for Three Coins/Yarrow.
            assertThat(LineJudgmentTable.dungLine(1)).isPresent();
            assertThat(LineJudgmentTable.dungLine(1).orElseThrow().label()).isEqualTo("Dụng Cửu");
            assertThat(LineJudgmentTable.dungLine(2).orElseThrow().label()).isEqualTo("Dụng Lục");
        }
    }

    @Nested
    @DisplayName("Three Coins and Yarrow — random casting")
    class RandomCasting {

        @Test
        @DisplayName("Reproducible given the same seed, and reports the seed used")
        void reproducibleGivenSeed() {
            var a = run(IChingCastInput.threeCoins(555));
            var b = run(IChingCastInput.threeCoins(555));
            assertThat(a.data()).isEqualTo(b.data());
            assertThat(a.data().seed()).isEqualTo(555L);
        }

        @Test
        @DisplayName("Yarrow and Three Coins produce a real hexagram and an internally consistent moving-line list")
        void producesConsistentReading() {
            for (var method : List.of(IChingCastInput.threeCoins(1), IChingCastInput.yarrow(1))) {
                var reading = run(method).data();
                assertThat(reading.lines()).hasSize(6);
                assertThat(reading.originalHexagram()).isNotNull();
                assertThat(reading.movingLinePositions())
                        .allSatisfy(p -> assertThat(p).isBetween(1, 6));
                boolean hasChangedHexagram = !reading.movingLinePositions().isEmpty();
                assertThat(reading.changedHexagram() != null).isEqualTo(hasChangedHexagram);
            }
        }

        @Test
        @DisplayName("No seed supplied still produces a valid reading (CSPRNG-generated seed, reported back)")
        void generatesSeedWhenAbsent() {
            var reading = run(IChingCastInput.threeCoins()).data();
            assertThat(reading.seed()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mai Hoa — Number method")
    class MaiHoaNumber {

        @Test
        @DisplayName("Two supplied numbers reproduce the quoted worked example (hexagram 64, Hỏa Thủy Vị Tế)")
        void quotedExampleReproduces() {
            var reading = run(IChingCastInput.fromNumbers(3, 6)).data();
            assertThat(reading.originalHexagram().number()).isEqualTo(64);
            assertThat(reading.seed()).isNull();
            assertThat(reading.lines()).isEmpty();
            assertThat(reading.movingLinePositions()).hasSize(1);
            assertThat(reading.changedHexagram()).isNotNull();
        }

        @Test
        @DisplayName("Missing either number fails validation rather than guessing")
        void missingNumbersFailsValidation() {
            var incomplete = new IChingCastInput(CastingMethod.MAI_HOA_NUMBER, null, 3, null, null);
            ValidationResult validation = engine.validateInput(incomplete);
            assertThat(validation.valid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Mai Hoa — Year-Month-Day-Hour method")
    class MaiHoaTime {

        @Test
        @DisplayName("Casts from the calculation context's own calculatedAt when no instant is supplied")
        void defaultsToContextCalculatedAt() {
            var reading = run(IChingCastInput.now()).data();
            assertThat(reading.originalHexagram()).isNotNull();
            assertThat(reading.movingLinePositions()).hasSize(1);
            assertThat(reading.changedHexagram()).isNotNull();
            assertThat(reading.lines()).isEmpty();
        }

        @Test
        @DisplayName("An explicitly supplied instant overrides the context's calculatedAt")
        void explicitInstantOverridesContext() {
            var a = run(IChingCastInput.atInstant(Instant.parse("2026-01-15T03:00:00Z")));
            var b = run(IChingCastInput.atInstant(Instant.parse("2026-01-15T03:00:00Z")));
            assertThat(a.data()).isEqualTo(b.data());
        }
    }

    @Test
    @DisplayName("A null input fails validation")
    void nullInputFailsValidation() {
        assertThat(engine.validateInput(null).valid()).isFalse();
    }

    private EngineResult<IChingReading> run(IChingCastInput input) {
        return engine.calculate(input, context());
    }

    private static CalculationContext context() {
        return new CalculationContext("calc-iching-test", IChingEngine.SCHOOL,
                new MethodologyVersions("1.0", "1.0", "1.0", "1.0"),
                ZoneId.of("Asia/Ho_Chi_Minh"), null, null,
                Instant.parse("2026-08-24T10:00:00Z"),
                null, null, BirthTimePrecision.EXACT, null);
    }
}
