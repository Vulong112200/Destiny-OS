package io.destinyos.api.dto;

import java.util.List;

/**
 * Response form of {@link io.destinyos.fusion.Conflict}
 * (UI_UX_VIETNAMESE_SPEC: "Điểm khác biệt giữa các phương pháp"). Never
 * resolved — a {@code METHODOLOGY_CONFLICT} reaches the client exactly as
 * detected (Master Spec section 10 Rule F7).
 */
public record ConflictDto(
        LabeledValue type,
        LabeledValue dimension,
        List<String> involvedEngines,
        String description
) {
}
