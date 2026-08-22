package io.destinyos.api.service;

import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.RetentionDto;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.calculation.CalculationEntity;

/**
 * Maps a persisted calculation's retention state to its DTO.
 *
 * <p>Shared by the write path ({@link ScenarioOrchestrationService}) and the
 * read path ({@link CalculationQueryService}) on purpose: if each mapped it
 * separately, a result would be free to report one expiry when it was created
 * and a different one when it was read back, which is precisely the kind of
 * quiet inconsistency the {@code resultHash} discipline exists to rule out.
 */
final class RetentionDtoMapper {

    private RetentionDtoMapper() {
    }

    static RetentionDto toDto(CalculationEntity calculation) {
        var retentionClass = calculation.retentionClass();
        return new RetentionDto(
                LabeledValue.of(retentionClass, VietnameseLabels.of(retentionClass)),
                calculation.expiresAt(),
                // Saving is only meaningful while the row is still on a deletion
                // path. Reporting true for an already-saved result would offer
                // the user a button that changes nothing.
                retentionClass.isAutoDeletable());
    }
}
