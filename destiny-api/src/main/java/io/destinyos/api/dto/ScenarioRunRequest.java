package io.destinyos.api.dto;

/**
 * Request body for {@code POST /api/v1/scenarios/{scenarioType}}.
 *
 * <p>Every field is optional — the caller supplies whichever engines they
 * actually want to participate. An engine not supplied here is never invoked,
 * the same way {@code ScenarioEngine#run} never invokes an engine the caller's
 * {@code availableTasks} map does not contain, and the response's
 * {@code unavailableEngines} says which ones the scenario wanted but did not
 * get.
 *
 * <p>{@code context} is the one field that is not an engine's input: it is
 * what the <em>user</em> asked (see {@link ScenarioContextRequest}), which
 * belongs to the run as a whole rather than to whichever engine happens to be
 * listed first. It is deliberately not another per-engine field — persistence,
 * the response, the AI narrative and the deterministic fallback all need it,
 * and none of them should have to guess which engine's request to read it out
 * of.
 *
 * @param context   what the user asked and which UI intent they picked;
 *                  presentation and narrative framing only, never an engine
 *                  input (see {@link ScenarioContextRequest})
 * @param numerology Pythagorean numerology input, or {@code null}
 * @param tarot      Tarot input, or {@code null}
 * @param bazi       Bát Tự input, or {@code null}
 * @param fengShui   Bát Trạch input, or {@code null}
 * @param astrology  Western astrology input, or {@code null}
 * @param iching     Kinh Dịch input, or {@code null}
 */
public record ScenarioRunRequest(
        ScenarioContextRequest context,
        NumerologyRequest numerology,
        TarotRequest tarot,
        BaziRequest bazi,
        FengShuiRequest fengShui,
        AstrologyRequest astrology,
        IChingRequest iching
) {

    /**
     * The engine-only form, for callers that supply no request context.
     *
     * <p>Exists so that adding {@link #context} stayed a strictly additive
     * change: a client (or a test) that only ever sent engine inputs is
     * describing exactly the same run it always did, and should not have to
     * write {@code null} for a concept it does not use. JSON callers are
     * unaffected either way — Jackson binds records by the canonical
     * constructor and by property name, so a body without {@code "context"}
     * simply leaves it null.
     */
    public ScenarioRunRequest(NumerologyRequest numerology, TarotRequest tarot, BaziRequest bazi,
                              FengShuiRequest fengShui, AstrologyRequest astrology, IChingRequest iching) {
        this(null, numerology, tarot, bazi, fengShui, astrology, iching);
    }

    /**
     * The question this run is actually about, or {@code null} if the caller
     * asked nothing in particular.
     *
     * <p>{@link ScenarioContextRequest#question()} is authoritative. The
     * fallback to {@link TarotRequest#question()} exists purely for backward
     * compatibility: that field was the only place a question could be sent
     * before this type had a {@code context}, and clients already in the field
     * still send it there. Normalizing the fallback through
     * {@link ScenarioContextRequest#normalizeQuestion} rather than using
     * {@code tarot.question()} raw matters — otherwise a legacy caller's
     * {@code "   "} would be persisted and prompted as a blank "question"
     * while a new caller's identical input would correctly be absent.
     *
     * <p>Resolved here, once, rather than at each of the three call sites that
     * need it (persistence, the echoed response, and the Tarot engine input),
     * so those three can never disagree about what the user asked.
     */
    public String effectiveQuestion() {
        if (context != null && context.question() != null) {
            return context.question();
        }
        return tarot == null ? null : ScenarioContextRequest.normalizeQuestion(tarot.question());
    }

    /** The UI intent id, or {@code null} — never a calculation input. */
    public String focusId() {
        return context == null ? null : context.focusId();
    }

    /** The Vietnamese UI intent label, or {@code null} — never a calculation input. */
    public String focusLabel() {
        return context == null ? null : context.focusLabel();
    }
}
