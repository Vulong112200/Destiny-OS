package io.destinyos.engines.numerology;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.core.version.MethodologyVersions;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumerologyEngineTest {

    private final NumerologyEngine engine = new NumerologyEngine();

    private static CalculationContext context() {
        return new CalculationContext("calc-numerology-1", "NUMEROLOGY_PYTHAGOREAN",
                new MethodologyVersions("1.0", "1.0", "1.0", null),
                ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), null,
                Instant.parse("2026-08-18T00:00:00Z"), null, null,
                BirthTimePrecision.UNKNOWN, List.of());
    }

    @Test
    @DisplayName("A valid Vietnamese name and birth date succeed with a full profile")
    void validInputSucceeds() {
        var input = new NumerologyInput("Nguyễn Văn An", LocalDate.of(1990, 3, 15));
        var result = engine.calculate(input, context());

        assertThat(result.status()).isEqualTo(EngineStatus.SUCCESS);
        assertThat(result.data().lifePath().value()).isEqualTo(1);
        assertThat(result.data().normalizedName().displayForm()).isEqualTo("Nguyen Van An");
    }

    @Test
    @DisplayName("An unnormalizable name returns INVALID_INPUT, not an exception")
    void unnormalizableNameIsInvalidInput() {
        var input = new NumerologyInput("Nguyễn 123", LocalDate.of(1990, 3, 15));
        var result = engine.calculate(input, context());

        assertThat(result.status()).isEqualTo(EngineStatus.INVALID_INPUT);
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @DisplayName("The same input always produces the same result (deterministic, no seed needed)")
    void isDeterministic() {
        var input = new NumerologyInput("Trần Thị Bích", LocalDate.of(1985, 6, 29));

        var first = engine.calculate(input, context()).data();
        var second = engine.calculate(input, context()).data();

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("Evidence is recorded for all five numbers, and one signal per number is produced")
    void evidenceAndSignalsRecordedForEveryNumber() {
        var input = new NumerologyInput("Lê Văn Bình", LocalDate.of(2000, 1, 1));
        var result = engine.calculate(input, context());

        assertThat(result.evidence()).hasSize(5);
        // Every (type, value) pair the engine can produce is authored in
        // NumerologyNumberMeanings, so all 5 numbers yield a signal.
        assertThat(result.signals()).hasSize(5);
        assertThat(result.signals()).allSatisfy(signal -> {
            assertThat(signal.engine()).isEqualTo(NumerologyEngine.ENGINE_ID);
            assertThat(signal.dimension()).isEqualTo(io.destinyos.core.signal.Dimension.OTHER);
            assertThat(signal.critical()).isFalse();
            assertThat(signal.evidenceIds()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("validateInput rejects null, blank, and unnormalizable names before calculation runs")
    void validateInputCatchesBadInputEarly() {
        assertThat(engine.validateInput(null).valid()).isFalse();
        assertThat(engine.validateInput(new NumerologyInput("   ", LocalDate.now())).valid())
                .isFalse();
        assertThat(engine.validateInput(new NumerologyInput("abc123", LocalDate.now())).valid())
                .isFalse();
        assertThat(engine.validateInput(new NumerologyInput("An", LocalDate.now())).valid())
                .isTrue();
    }

    @Test
    @DisplayName("The engine does not require the Calendar, birth time or location")
    void hasNoCalendarDependency() {
        // Property that lets Phase 4 proceed independently of Calendar
        // research (R9, R10, R14a/b, R15-R17), same as Tarot (ADR D2).
        var capability = engine.capability();

        assertThat(capability.requiresCalendar()).isFalse();
        assertThat(capability.requiresBirthTime()).isFalse();
        assertThat(capability.requiresLocation()).isFalse();
        assertThat(capability.requiresName()).isTrue();
    }

    @Test
    @DisplayName("Every number's evidence carries the authored meaning content, not just its value")
    void evidenceIncludesAuthoredMeaning() {
        var input = new NumerologyInput("Lê Văn Bình", LocalDate.of(2000, 1, 1));
        var result = engine.calculate(input, context());

        // Every (type, value) pair the engine can produce is authored in
        // NumerologyNumberMeanings (see its own class javadoc), so all 5
        // pieces of evidence must carry a non-empty "meaning" fact - this is
        // the wiring NumerologyEngine.buildEvidence was previously missing.
        assertThat(result.evidence()).hasSize(5);
        assertThat(result.evidence()).allSatisfy(ev -> {
            assertThat(ev.fact()).containsKey("value");
            assertThat(ev.fact()).containsKey("isMasterNumber");
            assertThat(ev.fact()).containsKey("meaning");

            @SuppressWarnings("unchecked")
            var meaning = (java.util.Map<String, Object>) ev.fact().get("meaning");
            assertThat(meaning).containsKey("keywords");
            assertThat(meaning).containsKey("text");
            assertThat(meaning).containsKey("polarity");
            assertThat((List<?>) meaning.get("keywords")).isNotEmpty();
            assertThat((String) meaning.get("text")).isNotBlank();
            assertThat((String) meaning.get("polarity")).isNotBlank();
        });
    }

    @Test
    @DisplayName("Metadata names a real school and source, matching the registry seed")
    void metadataNamesSchoolAndSource() {
        var metadata = engine.metadata();

        assertThat(metadata.school()).isEqualTo("Pythagorean");
        assertThat(metadata.source()).isNotBlank();
        assertThat(metadata.methodologyId()).isEqualTo("NUMEROLOGY_PYTHAGOREAN");
    }
}
