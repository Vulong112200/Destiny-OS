package io.destinyos.scenario;

/**
 * The ten MVP scenario types (Master Spec section 11).
 *
 * <p>Every value here is registered (D7-style honesty). {@code BUSINESS} and
 * {@code DAILY_ACTION} carry the two applicability policies Master Spec
 * section 7 gives a concrete worked example for. The project owner extended
 * seven more (2026-08-23) after reviewing traditional-practice evidence
 * gathered for each — see {@code docs/research_drafts/scenario_scope_reference.md}
 * and {@code docs/DECISION_LOG.md}. {@code COMPATIBILITY} remains
 * {@link ScenarioDefinition#undefinedPolicy}: its strongest evidence (Bát Tự
 * hợp hôn, Tử Vi xem tuổi, Chiêm tinh synastry) is all dual-chart, which this
 * system's single-chart architecture cannot represent yet, so declaring a
 * policy for it would imply an input shape that does not exist.
 */
public enum ScenarioType {
    CAREER,
    FINANCE,
    BUSINESS,
    RELATIONSHIP,
    COMPATIBILITY,
    PURCHASE,
    TRAVEL,
    PROJECT,
    DAILY_ACTION,
    GENERAL_DECISION
}
