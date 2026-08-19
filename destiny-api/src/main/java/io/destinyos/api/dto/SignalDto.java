package io.destinyos.api.dto;

import java.util.List;

/** Response form of {@link io.destinyos.core.signal.Signal}. */
public record SignalDto(
        String signalId,
        String engine,
        String school,
        LabeledValue dimension,
        String tag,
        LabeledValue polarity,
        LabeledValue strength,
        LabeledValue applicability,
        boolean critical,
        List<String> evidenceIds
) {
}
