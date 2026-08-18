package io.destinyos.core.result;

import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.signal.Signal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * What every engine returns (Master Spec §4). Immutable.
 *
 * <p>The static factories below exist to make the honest outcomes as easy to
 * produce as the successful one. If {@link #researchRequired} were awkward to
 * call while {@link #success} were convenient, the convenient one would win
 * under deadline pressure — which is exactly the failure CLAUDE.md Rule C is
 * written to prevent.
 *
 * @param <T> engine-specific payload
 */
public record EngineResult<T>(
        EngineStatus status,
        T data,
        List<Evidence> evidence,
        List<Signal> signals,
        List<EngineWarning> warnings,
        List<EngineError> errors,
        ResearchReference researchReference,
        Map<String, String> metadata
) {
    public EngineResult {
        Objects.requireNonNull(status, "status");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        signals  = signals  == null ? List.of() : List.copyOf(signals);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        errors   = errors   == null ? List.of() : List.copyOf(errors);
        metadata = metadata == null ? Map.of()  : Map.copyOf(metadata);

        // An engine that declines to answer must say why. ADR D7 requires the
        // non-answer to be informative, and this makes that structural rather
        // than a convention someone will forget.
        if ((status == EngineStatus.RESEARCH_REQUIRED || status == EngineStatus.NOT_IMPLEMENTED)
                && researchReference == null) {
            throw new IllegalArgumentException(
                    status + " requires a ResearchReference explaining what is missing "
                            + "(ADR D7, CLAUDE_CODE_WORKFLOW §8).");
        }
    }

    public static <T> EngineResult<T> success(T data, List<Evidence> evidence, List<Signal> signals) {
        return new EngineResult<>(EngineStatus.SUCCESS, data, evidence, signals,
                List.of(), List.of(), null, Map.of());
    }

    public static <T> EngineResult<T> partial(T data, List<Evidence> evidence, List<Signal> signals,
                                              List<EngineWarning> warnings) {
        return new EngineResult<>(EngineStatus.PARTIAL, data, evidence, signals,
                warnings, List.of(), null, Map.of());
    }

    /** The engine does not apply here. Fusion MUST NOT read this as neutral. */
    public static <T> EngineResult<T> notApplicable(String reason) {
        return new EngineResult<>(EngineStatus.NOT_APPLICABLE, null, List.of(), List.of(),
                List.of(EngineWarning.of("NOT_APPLICABLE", reason)), List.of(), null, Map.of());
    }

    /** The algorithm is not verified. CLAUDE.md Rule C: produce nothing rather than guess. */
    public static <T> EngineResult<T> researchRequired(ResearchReference reference) {
        return new EngineResult<>(EngineStatus.RESEARCH_REQUIRED, null, List.of(), List.of(),
                List.of(), List.of(), reference, Map.of());
    }

    public static <T> EngineResult<T> notImplemented(ResearchReference reference) {
        return new EngineResult<>(EngineStatus.NOT_IMPLEMENTED, null, List.of(), List.of(),
                List.of(), List.of(), reference, Map.of());
    }

    public static <T> EngineResult<T> invalidInput(List<EngineError> errors) {
        return new EngineResult<>(EngineStatus.INVALID_INPUT, null, List.of(), List.of(),
                List.of(), errors, null, Map.of());
    }

    /** Failed, but the wider request continues — the Rule F isolation case. */
    public static <T> EngineResult<T> failedRecoverable(EngineError error) {
        return new EngineResult<>(EngineStatus.FAILED_RECOVERABLE, null, List.of(), List.of(),
                List.of(), List.of(error), null, Map.of());
    }

    public static <T> EngineResult<T> failedFatal(EngineError error) {
        return new EngineResult<>(EngineStatus.FAILED_FATAL, null, List.of(), List.of(),
                List.of(), List.of(error), null, Map.of());
    }

    public Optional<T> dataIfPresent() {
        return Optional.ofNullable(data);
    }

    public Optional<ResearchReference> researchReferenceIfPresent() {
        return Optional.ofNullable(researchReference);
    }

    /**
     * Signals that actually take part in fusion. Non-applicable signals are
     * dropped here rather than downstream, so no caller can accidentally count
     * them (FUSION_ENGINE_SPEC §4).
     */
    public List<Signal> participatingSignals() {
        return signals.stream().filter(Signal::participates).toList();
    }

    public EngineResult<T> withMetadata(Map<String, String> extra) {
        var merged = new java.util.LinkedHashMap<>(metadata);
        merged.putAll(extra);
        return new EngineResult<>(status, data, evidence, signals, warnings, errors,
                researchReference, merged);
    }
}
