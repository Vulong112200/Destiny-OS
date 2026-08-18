package io.destinyos.engines.tarot;

/**
 * Whether a drawn card is upright or reversed.
 *
 * <p>Orientation is determined by {@link TarotOrientationPolicy}, which is a
 * recorded engineering decision (DECISION_LOG C9) rather than a metaphysical
 * dispute between schools: unlike Bát Tự's Dụng Thần, no tradition claims
 * "one true way" to decide reversal, so this does not require the Rule D
 * multi-school treatment.
 */
public enum TarotOrientation {
    UPRIGHT,
    REVERSED
}
