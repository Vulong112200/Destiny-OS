package io.destinyos.scenario;

/**
 * The ten MVP scenario types (Master Spec section 11).
 *
 * <p>Every value here is registered (D7-style honesty), but only
 * {@link ScenarioRegistry#BUSINESS_EXPANSION} and
 * {@link ScenarioRegistry#DAILY_ACTION} currently have a real applicability
 * policy — those are the only two scenarios Master Spec section 7 gives a
 * concrete worked example for. The other eight scenario types exist as
 * named use cases without a specified applicability policy; inventing one
 * for them would be exactly the kind of unsourced decision this project
 * avoids elsewhere, just applied to product design rather than metaphysics.
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
