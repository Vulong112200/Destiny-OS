package io.destinyos.engines.numerology;

import java.util.Objects;

/**
 * One computed number.
 *
 * @param type          which number this is
 * @param value         1-9, or 11/22/33 for a master number
 * @param isMasterNumber convenience flag; always equals {@link NumerologyReduction#isMasterNumber}
 */
public record NumerologyResult(NumerologyNumberType type, int value, boolean isMasterNumber) {
    public NumerologyResult {
        Objects.requireNonNull(type, "type");
    }

    public static NumerologyResult of(NumerologyNumberType type, int reducedValue) {
        return new NumerologyResult(type, reducedValue, NumerologyReduction.isMasterNumber(reducedValue));
    }
}
