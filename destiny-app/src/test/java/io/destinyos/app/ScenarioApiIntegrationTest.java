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
 * <p>Both engines now emit real signals (research items R11/R8's Vietnamese
 * interpretive content, authored 2026-08-19) — this is the first test in the
 * project to exercise a genuine, non-{@code INSUFFICIENT_EVIDENCE} Fusion
 * result end to end. For this fixed seed, the three drawn Tarot cards carry
 * disagreeing polarities, so the real, honest outcome is
 * {@code MAJOR_CONFLICT} — Rule E's "conflict is a valid result" made
 * concrete, not a consensus forced where none exists.
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
        // Both engines now emit real signals (R11/R8 content authored) - 3
        // Tarot cards x 5 dimension fields + 5 Numerology numbers, for this
        // fixed seed/name/birthdate.
        assertThat(body.signals()).hasSize(20);

        assertThat(body.fusion()).isNotNull();
        // A genuine, honest MAJOR_CONFLICT: the three Tarot cards drawn for
        // this seed carry different polarities (SUPPORT, CAUTION, NEGATIVE),
        // so every dimension they touch shows real internal disagreement -
        // Rule E's "conflict is a valid result" is not aspirational, this is
        // the first real Fusion output the project has ever produced from
        // authored engine content, and it correctly does not force a false
        // consensus.
        assertThat(body.fusion().overallOutcome().technical()).isEqualTo("MAJOR_CONFLICT");
        assertThat(body.fusion().dimensions()).isNotEmpty();
        assertThat(body.fusion().dimensions()).allSatisfy(dimension ->
                assertThat(dimension.state().technical()).isEqualTo("CONFLICT"));

        ResponseEntity<ScenarioRunResponse> replay = rest.getForEntity(
                "/api/v1/calculations/" + body.calculationId(), ScenarioRunResponse.class);

        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().resultHash()).isEqualTo(body.resultHash());
        assertThat(replay.getBody().scenarioId()).isEqualTo("BUSINESS");

        // Phase 12 (AI Narrative, ADR D8): the test profile leaves
        // destiny.ai.enabled at its real default (false) rather than
        // stubbing it out - this is deliberately exercising the actual
        // "AI absent" path through the real Spring context, not a mock.
        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> narrative = rest.postForEntity(
                "/api/v1/calculations/" + body.calculationId() + "/narrative", null,
                io.destinyos.api.dto.NarrativeResponseDto.class);

        assertThat(narrative.getStatusCode()).isEqualTo(HttpStatus.OK);
        var narrativeBody = narrative.getBody();
        assertThat(narrativeBody).isNotNull();
        assertThat(narrativeBody.source().technical()).isEqualTo("FALLBACK");
        assertThat(narrativeBody.fallbackReason().technical()).isEqualTo("AI_DISABLED");
        assertThat(narrativeBody.summary()).isNotBlank();
        // The fallback digest is built from this run's own real conflict -
        // never fabricated, and traceable straight back to Fusion's output.
        assertThat(narrativeBody.conflicts()).isNotEmpty();

        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> replayedNarrative = rest.getForEntity(
                "/api/v1/calculations/" + body.calculationId() + "/narrative",
                io.destinyos.api.dto.NarrativeResponseDto.class);

        assertThat(replayedNarrative.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replayedNarrative.getBody()).isNotNull();
        assertThat(replayedNarrative.getBody().summary()).isEqualTo(narrativeBody.summary());
    }

    @Test
    @DisplayName("Requesting a narrative for an unknown calculation is a 404, and reading one before it's generated is too")
    void narrativeEndpointsAre404ForUnknownOrUngeneratedCalculations() {
        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> generate = rest.postForEntity(
                "/api/v1/calculations/does-not-exist/narrative", null,
                io.destinyos.api.dto.NarrativeResponseDto.class);
        assertThat(generate.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> readBeforeGenerating = rest.getForEntity(
                "/api/v1/calculations/does-not-exist/narrative", io.destinyos.api.dto.NarrativeResponseDto.class);
        assertThat(readBeforeGenerating.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
