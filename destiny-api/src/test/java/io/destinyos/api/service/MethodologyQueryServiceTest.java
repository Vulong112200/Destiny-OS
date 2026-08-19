package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.persistence.registry.MethodologyEntity;
import io.destinyos.persistence.registry.MethodologyRegistryService;
import io.destinyos.persistence.registry.MethodologyVersionEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the read side of ADR D7: a research-blocked methodology is a row
 * with an honest status label, never an omission or a 404.
 */
class MethodologyQueryServiceTest {

    private final MethodologyRegistryService registry = mock(MethodologyRegistryService.class);
    private final MethodologyQueryService service = new MethodologyQueryService(registry);

    @Test
    @DisplayName("A research-blocked methodology is listed with its honest status and research refs")
    void listsBlockedMethodologyHonestly() {
        MethodologyEntity bazi = new MethodologyEntity("BAZI", "Bát Tự", "EASTERN_ASTROLOGY");
        MethodologyVersionEntity version = new MethodologyVersionEntity(
                bazi, "1.0", MethodologyStatus.RESEARCH_REQUIRED, null, null,
                Set.of("R1", "R2", "R3"), "Dụng Thần / Hỷ Thần chưa xác minh trường phái.");

        when(registry.allMethodologies()).thenReturn(List.of(bazi));
        when(registry.latestVersion("BAZI")).thenReturn(Optional.of(version));

        List<MethodologyDto> result = service.listAll();

        assertThat(result).hasSize(1);
        MethodologyDto dto = result.get(0);
        assertThat(dto.methodologyId()).isEqualTo("BAZI");
        assertThat(dto.calculable()).isFalse();
        assertThat(dto.status().technical()).isEqualTo("RESEARCH_REQUIRED");
        assertThat(dto.status().labelVi()).isEqualTo("Cần xác minh thuật toán");
        assertThat(dto.researchIds()).containsExactlyInAnyOrder("R1", "R2", "R3");
    }

    @Test
    @DisplayName("A calculable methodology reports its school, source and calculable=true")
    void listsCalculableMethodology() {
        MethodologyEntity tarot = new MethodologyEntity("TAROT_RWS", "Tarot", "DIVINATION");
        MethodologyVersionEntity version = new MethodologyVersionEntity(
                tarot, "1.0", MethodologyStatus.CONTENT_REQUIRED, "Rider-Waite-Smith (RWS)",
                "DESTINY_OS_MASTER_SPECIFICATION.md section 17", Set.of(), null);

        when(registry.allMethodologies()).thenReturn(List.of(tarot));
        when(registry.latestVersion("TAROT_RWS")).thenReturn(Optional.of(version));

        MethodologyDto dto = service.listAll().get(0);

        assertThat(dto.calculable()).isTrue();
        assertThat(dto.school()).isEqualTo("Rider-Waite-Smith (RWS)");
        assertThat(dto.status().labelVi()).isEqualTo("Thiếu nội dung diễn giải");
    }

    @Test
    @DisplayName("find() returns empty rather than throwing for an unknown methodology id")
    void findReturnsEmptyForUnknownId() {
        when(registry.allMethodologies()).thenReturn(List.of());

        assertThat(service.find("NOT_REGISTERED")).isEmpty();
    }

    @Test
    @DisplayName("A methodology with no version row yet still appears, with null version fields")
    void methodologyWithoutVersionStillAppears() {
        MethodologyEntity notYetVersioned = new MethodologyEntity("KY_MON", "Kỳ Môn", "DIVINATION");
        when(registry.allMethodologies()).thenReturn(List.of(notYetVersioned));
        when(registry.latestVersion("KY_MON")).thenReturn(Optional.empty());

        MethodologyDto dto = service.listAll().get(0);

        assertThat(dto.methodologyId()).isEqualTo("KY_MON");
        assertThat(dto.version()).isNull();
        assertThat(dto.status()).isNull();
        assertThat(dto.calculable()).isFalse();
    }
}
