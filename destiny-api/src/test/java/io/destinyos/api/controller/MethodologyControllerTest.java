package io.destinyos.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.api.service.MethodologyQueryService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * HTTP-layer contract for {@code GET /api/v1/methodologies}: a research-blocked
 * methodology is a normal 200 row (ADR D7), never a 404.
 */
@WebMvcTest(controllers = MethodologyController.class)
class MethodologyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MethodologyQueryService query;

    @Test
    void listsEveryRegisteredMethodologyIncludingBlockedOnes() throws Exception {
        var bazi = new MethodologyDto("BAZI", "Bát Tự", "EASTERN_ASTROLOGY", "1.0",
                LabeledValue.of(io.destinyos.engine.MethodologyStatus.RESEARCH_REQUIRED, "Cần xác minh thuật toán"),
                false, null, null, List.of("R1", "R2", "R3"), null);
        when(query.listAll()).thenReturn(List.of(bazi));

        mockMvc.perform(get("/api/v1/methodologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].methodologyId").value("BAZI"))
                .andExpect(jsonPath("$[0].calculable").value(false))
                .andExpect(jsonPath("$[0].status.labelVi").value("Cần xác minh thuật toán"));
    }

    @Test
    void aKnownMethodologyIdReturnsIt() throws Exception {
        var tarot = new MethodologyDto("TAROT_RWS", "Tarot", "DIVINATION", "1.0",
                LabeledValue.of(io.destinyos.engine.MethodologyStatus.CONTENT_REQUIRED, "Thiếu nội dung diễn giải"),
                true, "Rider-Waite-Smith (RWS)", "Master Spec section 17", List.of(), null);
        when(query.find("TAROT_RWS")).thenReturn(Optional.of(tarot));

        mockMvc.perform(get("/api/v1/methodologies/TAROT_RWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculable").value(true));
    }

    @Test
    void anUnregisteredMethodologyIdIs404() throws Exception {
        when(query.find("NOT_REGISTERED")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/methodologies/NOT_REGISTERED"))
                .andExpect(status().isNotFound());
    }
}
