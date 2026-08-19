package io.destinyos.api.dto;

import java.util.List;

/**
 * Response form of a registered methodology (ADR D7). A methodology blocked
 * on research is a row here with an honest status and, where present, the
 * research items it is waiting on — never a silent absence.
 */
public record MethodologyDto(
        String methodologyId,
        String displayNameVi,
        String domain,
        String version,
        LabeledValue status,
        boolean calculable,
        String school,
        String source,
        List<String> researchIds,
        String notes
) {
}
