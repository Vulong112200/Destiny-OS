package io.destinyos.api.dto;

import java.time.Instant;

/**
 * How long this calculation will be kept (CLAUDE.md §7).
 *
 * <p>Sent with every result rather than only on request. A system that
 * silently deletes a user's reading after 30 days and never says so is
 * withholding something the user needs in order to act — the same reasoning
 * that makes {@code Uncertainty} travel all the way to the UI rather than
 * being resolved away internally.
 *
 * @param retentionClass why the result is kept, with its Vietnamese label
 * @param expiresAt      when automatic deletion becomes possible, or
 *                       {@code null} for never. Null is a positive statement
 *                       ("this is not scheduled for deletion"), not a missing
 *                       value
 * @param canBeSaved     whether {@code POST /api/v1/calculations/{id}/save}
 *                       would change anything — false once the result is
 *                       already kept indefinitely, so the UI can hide a button
 *                       that would do nothing
 */
public record RetentionDto(
        LabeledValue retentionClass,
        Instant expiresAt,
        boolean canBeSaved
) {
}
