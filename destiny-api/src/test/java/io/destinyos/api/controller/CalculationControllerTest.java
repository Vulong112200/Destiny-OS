package io.destinyos.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.destinyos.api.dto.ScenarioRunResponse;
import io.destinyos.api.service.CalculationQueryService;
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

    @Test
    void aKnownCalculationIdReturnsItsRecordedExplainabilityRecord() throws Exception {
        var fixture = new ScenarioRunResponse("calc-1", "BUSINESS", true,
                List.of(), List.of(), List.of(), List.of(), null, "deadbeef");
        when(query.find("calc-1")).thenReturn(Optional.of(fixture));

        mockMvc.perform(get("/api/v1/calculations/calc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultHash").value("deadbeef"));
    }

    @Test
    void anUnknownCalculationIdIs404NotAFabricatedResult() throws Exception {
        when(query.find("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/calculations/missing"))
                .andExpect(status().isNotFound());
    }
}
