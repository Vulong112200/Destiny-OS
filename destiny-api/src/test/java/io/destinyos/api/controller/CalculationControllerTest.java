package io.destinyos.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.RetentionDto;
import io.destinyos.api.dto.ScenarioContextDto;
import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.service.CalculationQueryService;
import io.destinyos.api.service.CalculationSaveService;
import io.destinyos.core.retention.RetentionClass;
import io.destinyos.i18n.VietnameseLabels;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP-layer contract for {@code GET /api/v1/calculations/{id}}: replaying a past run. */
@WebMvcTest(controllers = CalculationController.class)
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculationQueryService query;

    @MockBean
    private CalculationSaveService save;

    @Test
    void aKnownCalculationIdReturnsItsRecordedExplainabilityRecord() throws Exception {
        var fixture = new ScenarioRunResponse("calc-1", "BUSINESS", ScenarioContextDto.EMPTY, List.of(), true,
                List.of(), List.of(), List.of(), List.of(), null, "deadbeef",
                ephemeralRetention());
        when(query.find("calc-1")).thenReturn(Optional.of(fixture));

        mockMvc.perform(get("/api/v1/calculations/calc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultHash").value("deadbeef"))
                // The reader is told, in Vietnamese, that this result expires.
                .andExpect(jsonPath("$.retention.retentionClass.technical").value("EPHEMERAL"))
                .andExpect(jsonPath("$.retention.canBeSaved").value(true))
                .andExpect(jsonPath("$.retention.expiresAt").exists());
    }

    @Test
    void savingAKnownCalculationReturnsItsNewRetentionState() throws Exception {
        var saved = new RetentionDto(
                LabeledValue.of(RetentionClass.USER_SAVED,
                        VietnameseLabels.of(RetentionClass.USER_SAVED)),
                null,
                false);
        when(save.save("calc-1")).thenReturn(Optional.of(saved));

        mockMvc.perform(post("/api/v1/calculations/calc-1/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retentionClass.technical").value("USER_SAVED"))
                // No expiry at all, not a far-future one: the UI must be able to
                // say "will not be deleted" without interpreting a date.
                .andExpect(jsonPath("$.expiresAt").doesNotExist())
                .andExpect(jsonPath("$.canBeSaved").value(false));
    }

    @Test
    void savingAnUnknownCalculationIs404() throws Exception {
        when(save.save("missing")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/calculations/missing/save"))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnknownCalculationIdIs404NotAFabricatedResult() throws Exception {
        when(query.find("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/calculations/missing"))
                .andExpect(status().isNotFound());
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
