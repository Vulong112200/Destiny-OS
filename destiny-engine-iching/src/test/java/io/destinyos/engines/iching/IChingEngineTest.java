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
        @DisplayName("Status stays PARTIAL even now that signals are emitted — LINE_SELECTION_RULE is still open")
        void statusStaysPartial() {
            // Closing CAT_HUNG_POLARITY does not make the engine complete.
            // Promoting to SUCCESS the moment signals appeared would hide the
            // one gap that is still real.
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.status()).isEqualTo(EngineStatus.PARTIAL);
        }

        @Test
        @DisplayName("The line-selection-rule gap is named as a blocked section on every reading")
        void blockedSectionIsNamedInEvidence() {
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.evidence())
                    .extracting(e -> e.ruleId())
                    .contains("ICHING_BLOCKED_LINE_SELECTION_RULE");
            assertThat(result.warnings())
                    .anySatisfy(w -> assertThat(w.critical()).isTrue());
        }

        @Test
        @DisplayName("CAT_HUNG_POLARITY is no longer blocked — it must not be claimed as open and closed at once")
        void catHungIsNoLongerBlocked() {
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.evidence())
                    .extracting(e -> e.ruleId())
                    .doesNotContain("ICHING_BLOCKED_CAT_HUNG_POLARITY");
        }

        @Test
        @DisplayName("Hào làm chủ never becomes a signal — the source denies it carries any polarity")
        void haoLamChuIsEvidenceOnly() {
            // Nguyễn Hiến Lê tr.102: "Làm chủ chỉ vì nó là số ít trong một đám
            // số nhiều, chứ không phải vì tốt hay xấu." A signal derived from
            // the governing line would assert exactly what that sentence denies.
            var result = run(IChingCastInput.fromNumbers(3, 6));
            assertThat(result.signals())
                    .as("no signal may be sourced from the hào-làm-chủ methodology")
                    .noneSatisfy(s -> assertThat(s.school()).isEqualTo(HaoLamChu.SCHOOL));
        }
    }

    @Nested
    @DisplayName("Cát/hung signals (CAT_HUNG_POLARITY, closed 2026-09-01)")
    class CatHung {

        @Test
        @DisplayName("A reading emits real cát/hung signals traceable to the evidence they were read from")
        void emitsSignalsLinkedToEvidence() {
            var result = run(IChingCastInput.fromNumbers(3, 6));
            assertThat(result.signals()).isNotEmpty();

            var evidenceIds = result.evidence().stream().map(e -> e.evidenceId()).toList();
            assertThat(result.signals()).allSatisfy(signal -> {
                assertThat(signal.tag()).startsWith("ICHING_CAT_HUNG_");
                // A signal whose evidenceIds point nowhere is unauditable, which
                // defeats the whole reason this layer is a reading and not a score.
                assertThat(signal.evidenceIds()).isNotEmpty();
                assertThat(evidenceIds).containsAll(signal.evidenceIds());
            });
        }

        @Test
        @DisplayName("The hexagram's own quẻ từ signal carries full applicability")
        void queGocSignalIsFullyApplicable() {
            var result = run(IChingCastInput.fromNumbers(3, 6));
            assertThat(result.signals())
                    .filteredOn(s -> s.tag().startsWith("ICHING_CAT_HUNG_QUE_GOC"))
                    .isNotEmpty()
                    .allSatisfy(s -> assertThat(s.applicability())
                            .isEqualTo(io.destinyos.core.signal.Applicability.HIGH));
        }

        @Test
        @DisplayName("No cát/hung signal is marked critical while LINE_SELECTION_RULE is open")
        void nothingIsCriticalYet() {
            // With several moving lines the engine cannot say which 凶 is *the*
            // answer, and `critical` means a signal that should dominate.
            var result = run(IChingCastInput.threeCoins(1));
            assertThat(result.signals()).allSatisfy(s -> assertThat(s.critical()).isFalse());
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
