package io.destinyos.persistence.narrative;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativeResponse;
import io.destinyos.ai.NarrativeResult;
import io.destinyos.ai.NarrativeSource;
import io.destinyos.core.result.EngineStatus;
import io.destinyos.persistence.TestApplication;
import io.destinyos.persistence.calculation.CalculationEntity;
import io.destinyos.persistence.calculation.CalculationRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Round-trip tests for the V7 schema (Phase 12, ADR D8): a generated or
 * fallback narrative must be recoverable exactly as produced, and
 * regenerating one must overwrite rather than accumulate rows - see the V7
 * migration's own comment for why this table is an upsert, unlike
 * {@code fusion_results}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestApplication.class, NarrativeRecorder.class})
class NarrativeRecorderTest {

    @Autowired
    private NarrativeRecorder recorder;

    @Autowired
    private CalculationRepository calculations;

    @Autowired
    private EntityManager entityManager;

    private String newCalculation() {
        String id = "calc-" + System.nanoTime();
        var calculation = new CalculationEntity(id, "hash", "1.0", "1.0", "1.0", "Asia/Ho_Chi_Minh", Instant.now());
        calculation.markCompleted(EngineStatus.SUCCESS, "result-hash", Instant.now());
        calculations.save(calculation);
        return id;
    }

    @Test
    @DisplayName("An AI-generated narrative round-trips every field, including its lists")
    void roundTripsAiGeneratedNarrative() {
        String calculationId = newCalculation();
        var response = new NarrativeResponse("Tom tat", List.of("tin hieu 1", "tin hieu 2"),
                List.of("xung dot 1"), List.of("can trong 1"), List.of("cau hoi 1"));
        var result = NarrativeResult.aiGenerated(response, "openrouter", "some-model");

        recorder.record(calculationId, result);
        entityManager.flush();
        entityManager.clear();

        Optional<NarrativeEntity> found = recorder.find(calculationId);

        assertThat(found).isPresent();
        NarrativeEntity entity = found.get();
        assertThat(entity.source()).isEqualTo(NarrativeSource.AI_GENERATED);
        assertThat(entity.fallbackReason()).isEqualTo(FallbackReason.NONE);
        assertThat(entity.summary()).isEqualTo("Tom tat");
        assertThat(entity.keySignals()).containsExactly("tin hieu 1", "tin hieu 2");
        assertThat(entity.conflicts()).containsExactly("xung dot 1");
        assertThat(entity.cautions()).containsExactly("can trong 1");
        assertThat(entity.reflectionQuestions()).containsExactly("cau hoi 1");
        assertThat(entity.providerName()).isEqualTo("openrouter");
        assertThat(entity.model()).isEqualTo("some-model");
        assertThat(entity.generatedAt()).isNotNull();
    }

    @Test
    @DisplayName("A fallback narrative round-trips with a null provider/model")
    void roundTripsFallbackNarrative() {
        String calculationId = newCalculation();
        var response = new NarrativeResponse("Bao cao du phong", List.of(), List.of(), List.of(), List.of());
        var result = NarrativeResult.fallback(response, FallbackReason.TIMEOUT);

        recorder.record(calculationId, result);
        entityManager.flush();
        entityManager.clear();

        NarrativeEntity entity = recorder.find(calculationId).orElseThrow();

        assertThat(entity.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(entity.fallbackReason()).isEqualTo(FallbackReason.TIMEOUT);
        assertThat(entity.providerName()).isNull();
        assertThat(entity.model()).isNull();
    }

    @Test
    @DisplayName("Regenerating a narrative for the same calculation upserts, not duplicates")
    void regeneratingUpsertsRatherThanDuplicating() {
        String calculationId = newCalculation();
        var first = NarrativeResult.fallback(
                new NarrativeResponse("Ban dau", List.of(), List.of(), List.of(), List.of()),
                FallbackReason.AI_DISABLED);
        var second = NarrativeResult.aiGenerated(
                new NarrativeResponse("Da tao lai", List.of(), List.of(), List.of(), List.of()),
                "openrouter", "model-2");

        recorder.record(calculationId, first);
        recorder.record(calculationId, second);
        entityManager.flush();
        entityManager.clear();

        NarrativeEntity entity = recorder.find(calculationId).orElseThrow();
        assertThat(entity.summary()).isEqualTo("Da tao lai");
        assertThat(entity.source()).isEqualTo(NarrativeSource.AI_GENERATED);
    }

    @Test
    @DisplayName("A calculation with no narrative yet reports empty, not an error")
    void missingNarrativeIsEmptyNotAnException() {
        assertThat(recorder.find("no-such-calculation")).isEmpty();
    }
}
