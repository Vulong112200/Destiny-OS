package io.destinyos.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativeSource;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.NarrativeResponseDto;
import io.destinyos.api.service.NarrativeOrchestrationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * HTTP contract for the Narrative API group (Phase 12, ADR D8): 404 means
 * "no calculation"/"no narrative yet", never a 500 - a fallback narrative is
 * still a 200, the same honesty principle {@code MethodologyControllerTest}
 * already asserts for research-blocked methodologies (ADR D7).
 */
@WebMvcTest(controllers = NarrativeController.class)
class NarrativeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NarrativeOrchestrationService orchestration;

    @Test
    void generateReturnsTheNarrativeForAnExistingCalculation() throws Exception {
        var dto = new NarrativeResponseDto("calc-1",
                LabeledValue.of(NarrativeSource.AI_GENERATED, "Diễn giải bởi AI"),
                LabeledValue.of(FallbackReason.NONE, "Không áp dụng"),
                "Tóm tắt", List.of("tín hiệu 1"), List.of(), List.of(), List.of(),
                "openrouter", "model-x", null);
        when(orchestration.generate("calc-1")).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/api/v1/calculations/calc-1/narrative"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Tóm tắt"))
                .andExpect(jsonPath("$.source.labelVi").value("Diễn giải bởi AI"));
    }

    @Test
    void generateReturns404ForAnUnknownCalculation() throws Exception {
        when(orchestration.generate("no-such-id")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/calculations/no-such-id/narrative"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findReturnsThePersistedNarrative() throws Exception {
        var dto = new NarrativeResponseDto("calc-2",
                LabeledValue.of(NarrativeSource.FALLBACK, "Tóm tắt từ dữ liệu tính toán gốc"),
                LabeledValue.of(FallbackReason.AI_DISABLED, "Phần diễn giải AI đang tắt"),
                "Bao cao du phong", List.of(), List.of(), List.of(), List.of(),
                null, null, "2026-08-20T00:00:00Z");
        when(orchestration.find("calc-2")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/calculations/calc-2/narrative"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source.technical").value("FALLBACK"))
                .andExpect(jsonPath("$.providerName").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void findReturns404WhenNoNarrativeHasBeenGeneratedYet() throws Exception {
        when(orchestration.find("calc-3")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/calculations/calc-3/narrative"))
                .andExpect(status().isNotFound());
    }
}
