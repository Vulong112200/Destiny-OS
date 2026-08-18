package io.destinyos.engines.numerology;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @param fullName  the name exactly as the person entered it — normalized
 *                  internally, never silently changed without also
 *                  reporting the normalized form used
 * @param birthDate needed for Life Path and Birthday
 */
public record NumerologyInput(String fullName, LocalDate birthDate) {
    public NumerologyInput {
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(birthDate, "birthDate");
    }
}
