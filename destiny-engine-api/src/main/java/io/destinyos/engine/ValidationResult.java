package io.destinyos.engine;

import io.destinyos.core.result.EngineError;
import java.util.List;

/**
 * Outcome of {@link MetaphysicalEngine#validateInput}.
 *
 * <p>Separating validation from calculation lets the orchestrator reject bad
 * input before spending a virtual thread on it, and lets the UI tell a user
 * what is missing before they wait for a result.
 */
public record ValidationResult(boolean valid, List<EngineError> errors) {

    public ValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failed(List<EngineError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("An invalid result must explain why.");
        }
        return new ValidationResult(false, errors);
    }

    public static ValidationResult failed(String code, String message, String engine) {
        return failed(List.of(EngineError.of(code, message, engine)));
    }
}
