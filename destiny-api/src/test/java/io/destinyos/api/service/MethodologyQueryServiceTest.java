package io.destinyos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.destinyos.api.dto.MethodologyDto;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.persistence.registry.MethodologyEntity;
import io.destinyos.persistence.registry.MethodologyRegistryService;
import io.destinyos.persistence.registry.MethodologyVersionEntity;
import java.util.List;
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
        when(registry.allVersions()).thenReturn(List.of(version));

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
        when(registry.allVersions()).thenReturn(List.of(version));

        MethodologyDto dto = service.listAll().get(0);

        assertThat(dto.calculable()).isTrue();
        assertThat(dto.school()).isEqualTo("Rider-Waite-Smith (RWS)");
        assertThat(dto.status().labelVi()).isEqualTo("Thiếu nội dung diễn giải");
    }

    @Test
    @DisplayName("listAll() reads all versions once, never one lookup per methodology")
    void listAllDoesNotQueryPerMethodology() {
        MethodologyEntity bazi = new MethodologyEntity("BAZI", "Bát Tự", "EASTERN_ASTROLOGY");
        MethodologyEntity tarot = new MethodologyEntity("TAROT_RWS", "Tarot", "DIVINATION");
        MethodologyVersionEntity baziV1 = new MethodologyVersionEntity(
                bazi, "1.0", MethodologyStatus.RESEARCH_REQUIRED, null, null, Set.of("R1"), null);
        MethodologyVersionEntity tarotV1 = new MethodologyVersionEntity(
                tarot, "1.0", MethodologyStatus.CONTENT_REQUIRED, "Rider-Waite-Smith (RWS)",
                "DESTINY_OS_MASTER_SPECIFICATION.md section 17", Set.of(), null);

        when(registry.allMethodologies()).thenReturn(List.of(bazi, tarot));
        when(registry.allVersions()).thenReturn(List.of(baziV1, tarotV1));

        assertThat(service.listAll()).hasSize(2);

        // The point of the test. Calling latestVersion(id) in the mapping loop
        // is what made GET /api/v1/methodologies take ~6.7s against a remote
        // database: one query per methodology, each in its own transaction,
        // plus one more per row for the eagerly fetched researchIds. Nothing
        // about the returned DTOs would have revealed that - only the call
        // count does, which is why this assertion exists at all.
        verify(registry, never()).latestVersion(org.mockito.ArgumentMatchers.anyString());
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
        when(registry.allVersions()).thenReturn(List.of());

        MethodologyDto dto = service.listAll().get(0);

        assertThat(dto.methodologyId()).isEqualTo("KY_MON");
        assertThat(dto.version()).isNull();
        assertThat(dto.status()).isNull();
        assertThat(dto.calculable()).isFalse();
    }
}
