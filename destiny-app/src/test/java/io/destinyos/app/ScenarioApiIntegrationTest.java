package io.destinyos.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.api.dto.NumerologyRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.ScenarioRunResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * The one true end-to-end test: real HTTP, real {@code TarotEngine} and
 * {@code NumerologyEngine} (wired by {@code EngineWiringConfig}), real
 * {@code FusionEngine}, and a real database round-trip through
 * {@code CalculationRecorder} — everything {@code destiny-api}'s unit and
 * slice tests deliberately mock or stub out to stay within their own module
 * boundary.
 *
 * <p>Uses {@code ScenarioType.BUSINESS} rather than {@code DAILY_ACTION}:
 * per {@code ScenarioRegistry}, BUSINESS is the only defined-policy scenario
 * whose applicable-engines list includes both {@code TAROT} and
 * {@code NUMEROLOGY_PYTHAGOREAN} — the two engines this MVP actually has.
 *
 * <p>Both engines currently emit evidence but no signals (research items
 * R11 / R8's interpretive-content gap — the mechanical computation is done,
 * the meaning layer is not written yet). Asserting
 * {@code FusionOutcome.INSUFFICIENT_EVIDENCE} here is therefore the honest,
 * currently-correct expectation, not a placeholder — it will change the
 * moment either engine starts emitting real signals, and this test should
 * be updated at that point rather than loosened preemptively.
 */
@SpringBootTest(classes = DestinyOsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ScenarioApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("A BUSINESS scenario run with both engines persists and returns a full explainability record")
    void runsBusinessScenarioAndPersistsIt() {
        var request = new ScenarioRunRequest(
                new NumerologyRequest("Nguyễn Văn A", LocalDate.of(1990, 5, 15)),
                new io.destinyos.api.dto.TarotRequest("PAST_PRESENT_FUTURE", 42L, "Tôi có nên mở rộng kinh doanh không?"));

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.policyDefined()).isTrue();
        assertThat(body.calculationId()).isNotBlank();
        assertThat(body.resultHash()).isNotBlank();

        assertThat(body.engines()).hasSize(2);
        assertThat(body.engines()).allSatisfy(engine ->
                assertThat(engine.status().labelVi()).isEqualTo("Thành công"));

        // Both engines compute honestly but neither has interpretive content
        // yet (R8/R11) - evidence is real, signals are not fabricated to fill
        // the gap.
        assertThat(body.evidence()).isNotEmpty();
        assertThat(body.signals()).isEmpty();

        assertThat(body.fusion()).isNotNull();
        assertThat(body.fusion().overallOutcome().technical()).isEqualTo("INSUFFICIENT_EVIDENCE");
        assertThat(body.fusion().overallOutcome().labelVi()).isEqualTo("Chưa đủ dữ liệu");

        ResponseEntity<ScenarioRunResponse> replay = rest.getForEntity(
                "/api/v1/calculations/" + body.calculationId(), ScenarioRunResponse.class);

        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().resultHash()).isEqualTo(body.resultHash());
        assertThat(replay.getBody().scenarioId()).isEqualTo("BUSINESS");
    }

    @Test
    @DisplayName("A scenario with no defined applicability policy runs zero engines rather than guessing one")
    void undefinedPolicyScenarioRunsNothing() {
        var request = new ScenarioRunRequest(null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/career", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.policyDefined()).isFalse();
        assertThat(body.fusion()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/methodologies lists a research-blocked methodology honestly, not as a 404")
    void methodologyRegistryListsBlockedMethodologies() {
        ResponseEntity<io.destinyos.api.dto.MethodologyDto[]> response = rest.getForEntity(
                "/api/v1/methodologies", io.destinyos.api.dto.MethodologyDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).anySatisfy(m -> {
            assertThat(m.methodologyId()).isEqualTo("BAZI");
            assertThat(m.calculable()).isFalse();
            assertThat(m.status().labelVi()).isEqualTo("Cần xác minh thuật toán");
        });
    }

    @Test
    @DisplayName("An unknown calculation id is a 404, never a fabricated result")
    void unknownCalculationIdIs404() {
        ResponseEntity<ScenarioRunResponse> response = rest.getForEntity(
                "/api/v1/calculations/does-not-exist", ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
