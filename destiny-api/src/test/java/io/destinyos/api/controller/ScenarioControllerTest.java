package io.destinyos.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.RetentionDto;
import io.destinyos.api.dto.ScenarioContextDto;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.core.retention.RetentionClass;
import io.destinyos.i18n.VietnameseLabels;
import java.time.Instant;
import io.destinyos.api.service.ScenarioOrchestrationService;
import io.destinyos.scenario.ScenarioType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * HTTP-layer contract for {@code POST /api/v1/scenarios/{scenarioType}}.
 * {@link ScenarioOrchestrationService} is mocked — its own behaviour is
 * covered by {@code ScenarioOrchestrationServiceTest}; this test is only
 * about routing, status codes and the {@link ApiExceptionHandler} wiring
 * (CLAUDE.md section 3: controllers hold no domain calculation).
 */
@WebMvcTest(controllers = ScenarioController.class)
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScenarioOrchestrationService orchestration;

    @Test
    void runsAValidScenarioAndReturnsTheOrchestrationResult() throws Exception {
        var fixture = new ScenarioRunResponse("calc-1", "BUSINESS", ScenarioContextDto.EMPTY, List.of(), true,
                List.of(), List.of(), List.of(), List.of(), null, "deadbeef",
                ephemeralRetention());
        when(orchestration.run(eq(ScenarioType.BUSINESS), any(ScenarioRunRequest.class)))
                .thenReturn(fixture);

        mockMvc.perform(post("/api/v1/scenarios/business")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculationId").value("calc-1"))
                .andExpect(jsonPath("$.resultHash").value("deadbeef"));
    }

    @Test
    void anUnknownScenarioTypeIsAnHonestBadRequestNotAServerError() throws Exception {
        mockMvc.perform(post("/api/v1/scenarios/not_a_real_scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void aMissingRequestBodyIsTreatedAsNoEnginesRequested() throws Exception {
        var fixture = new ScenarioRunResponse("calc-2", "DAILY_ACTION", ScenarioContextDto.EMPTY, List.of(), true,
                List.of(), List.of("TAROT"), List.of(), List.of(), null, "cafebabe",
                ephemeralRetention());
        when(orchestration.run(eq(ScenarioType.DAILY_ACTION), any(ScenarioRunRequest.class)))
                .thenReturn(fixture);

        mockMvc.perform(post("/api/v1/scenarios/daily_action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculationId").value("calc-2"));
    }

    /**
     * A fixture retention block. Present rather than null because
     * {@code ScenarioRunResponse.retention} is documented as always present —
     * a test that passes null would let a regression that drops it slip past.
     */
    private static RetentionDto ephemeralRetention() {
        return new RetentionDto(
                LabeledValue.of(RetentionClass.EPHEMERAL,
                        VietnameseLabels.of(RetentionClass.EPHEMERAL)),
                Instant.parse("2026-09-21T00:00:00Z"),
                true);
    }
}
