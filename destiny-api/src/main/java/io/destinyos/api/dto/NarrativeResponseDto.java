package io.destinyos.api.dto;

import java.util.List;

/**
 * Response body for the Narrative API group (Phase 12, ADR D8):
 * {@code POST}/{@code GET /api/v1/calculations/{id}/narrative}.
 *
 * <p>{@code source}/{@code fallbackReason} are always present, even on a
 * successful AI-generated narrative ({@code fallbackReason} then labels
 * {@code NONE}) - UI_UX_VIETNAMESE_SPEC section 1 forbids a bare enum
 * anywhere, including a status the reader might reasonably want to inspect
 * ("was this really from AI, or the fallback?").
 *
 * @param providerName nullable - absent when {@code source} is the fallback
 * @param model        nullable - absent when {@code source} is the fallback
 */
public record NarrativeResponseDto(
        String calculationId,
        LabeledValue source,
        LabeledValue fallbackReason,
        String summary,
        List<String> keySignals,
        List<String> conflicts,
        List<String> cautions,
        List<String> reflectionQuestions,
        String providerName,
        String model,
        String generatedAt
) {
}
