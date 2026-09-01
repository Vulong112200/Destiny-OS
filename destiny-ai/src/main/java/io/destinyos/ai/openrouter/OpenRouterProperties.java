package io.destinyos.ai.openrouter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code destiny.ai.openrouter.*} (AI_NARRATIVE_SPEC.md section 7: model,
 * timeout, max tokens, temperature are all configuration, never hardcoded).
 *
 * <p>{@code model} deliberately has NO default value. OpenRouter's free-tier
 * model catalogue changes over time; hardcoding a specific model id here
 * would be a claim about current availability this project cannot verify
 * stays true, and D8 already requires the system to run correctly with no
 * provider configured at all - leaving this blank is exactly that state,
 * not a bug. An operator who wants OpenRouter must set
 * {@code DESTINY_AI_OPENROUTER_MODEL} to a model they have confirmed is
 * currently available on their account.
 *
 * <p>{@code fallbackModels} exists because a single free model is unreliable
 * by nature, not by misconfiguration. Free models are rate-limited
 * <em>upstream at the inference provider</em>, shared across every OpenRouter
 * user - measured live, three free models returned
 * {@code 429 "... is temporarily rate-limited upstream"} while two others
 * returned {@code 200} at the same instant. A model id can also simply be
 * retired. Neither is something an operator can fix by choosing a better
 * single value.
 */
@ConfigurationProperties(prefix = "destiny.ai.openrouter")
public class OpenRouterProperties {

    private String apiKey = "";
    private String model = "";
    private String baseUrl = "https://openrouter.ai/api/v1";
    private int timeoutMs = 15_000;

    /**
     * Ceiling on how long ONE {@code call(...)} may spend walking the whole
     * model chain, in milliseconds. 45000.
     *
     * <p>{@link #timeoutMs} bounds a single HTTP attempt and nothing more, so
     * before this property existed the real worst case was the product of
     * three independent numbers nobody was looking at together:
     * {@code MAX_ATTEMPTS (2) x chain length x timeoutMs}. With four models at
     * a 25s timeout - a configuration this project shipped in its own
     * {@code .env} - that is {@code 2 x 4 x 25000 = 200 seconds} for one
     * request. The javadoc on {@code OpenRouterNarrativeProvider.call} used to
     * call that latency "something the operator set rather than something this
     * class decides", which was true only in the sense that the operator set
     * three factors and was handed their product.
     *
     * <p>200 seconds is not a slow narrative, it is a broken page. Every
     * caller downstream has a shorter patience than that: a browser fetch, a
     * Next.js server component's platform execution limit (10-15s by default),
     * a reverse proxy's idle timeout. Whichever of them gives up first turns a
     * would-be fallback into nothing at all - and ADR D8's guarantee that the
     * system stays usable is precisely what makes the difference invisible.
     * The deterministic fallback is only a guarantee if somebody is still
     * listening when it arrives.
     *
     * <p>45 seconds is chosen so the whole chain still fits under the web
     * layer's own patience with room to spare: {@code destiny-web} allows a
     * narrative call 75s and its result route 90s, and the value here has to
     * leave the shorter of those room to be the side that gives up last.
     * Within 45s the default 15s timeout still buys three attempts, which is
     * enough for "my pinned model is rate-limited, try the free meta-model".
     * Widening the chain no longer widens the worst case, which is the point -
     * chain length becomes a question of "how many models get a chance"
     * rather than "how long a user waits".
     *
     * <p>The deadline is checked <em>between</em> attempts, never mid-flight,
     * because an in-flight HTTP call already has {@link #timeoutMs} bounding
     * it and cancelling it early would only discard an answer that may be
     * about to arrive. The honest consequence is that the true worst case is
     * {@code totalDeadlineMs + timeoutMs} - an attempt starting one
     * millisecond before the deadline still runs its full timeout - so pick
     * both numbers together, not either alone.
     *
     * <p>A deadline of {@code 0} is meaningful rather than broken: it degrades
     * to "try the primary model once, never walk the chain", because the check
     * only ever happens after an attempt has already been made. No request is
     * refused for a failure that never actually happened.
     */
    private int totalDeadlineMs = 45_000;

    /**
     * Output token ceiling. 2000, raised from 800 after measurement — do not
     * lower it back without repeating the measurement.
     *
     * <p>800 was silently breaking the AI path in production. Vietnamese costs
     * far more tokens per character than English on these tokenizers, and this
     * schema asks for a summary plus four arrays of full sentences. Running the
     * project's own system prompt against free OpenRouter models, 2 of 3 runs
     * came back invalid: the reply stopped mid-string (one measured at 973
     * characters, not ending in {@code }}), {@link NarrativeResponseParser}
     * correctly rejected it, and the request degraded to
     * {@link io.destinyos.ai.FallbackReason#MALFORMED_JSON}.
     *
     * <p>That failure is nearly invisible from the outside: ADR D8 guarantees a
     * renderable result either way, so a truncation budget does not produce an
     * error, it produces a system that looks like it has AI enabled and never
     * uses it. Cutting this number looks free and is not.
     */
    private int maxTokens = 2_000;

    private double temperature = 0.3;

    /**
     * Models to try, in order, when {@link #model} cannot answer.
     *
     * <p>Defaults to the single meta-model {@code openrouter/free}, which
     * OpenRouter routes to whichever free model is currently serving
     * (verified: it answered by routing to
     * {@code nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free}). That makes
     * the default chain "my chosen model, else whatever free model is up",
     * which is the behaviour an operator almost always wants and would
     * otherwise have to discover.
     *
     * <p>Deliberately NOT {@code openrouter/auto}: measured on a free-tier
     * account, {@code auto} returns {@code 402 Insufficient credits}. It is a
     * paid router, and defaulting to it would turn "AI enabled" into a silent
     * permanent fallback on exactly the accounts this project targets.
     *
     * <p>Also deliberately client-side rather than OpenRouter's own
     * {@code "models": [...]} request array. That array works for runtime
     * failures, but a <em>removed or misspelled</em> model id is validated
     * upfront and rejected with
     * {@code 400 "... is not a valid model ID"} for the whole request, even
     * when a valid fallback is listed alongside it. The case an operator most
     * needs covering - "the model I pinned no longer exists" - is precisely
     * the case that array does not cover, so the chain is walked here.
     *
     * <p>Never null; blank and duplicate entries are dropped on binding so a
     * comma-separated environment variable with a stray trailing comma or
     * space does not become an empty model id in a request body.
     */
    private List<String> fallbackModels = List.of("openrouter/free");

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getTotalDeadlineMs() {
        return totalDeadlineMs;
    }

    public void setTotalDeadlineMs(int totalDeadlineMs) {
        this.totalDeadlineMs = totalDeadlineMs;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<String> getFallbackModels() {
        return fallbackModels;
    }

    /**
     * Accepts what Spring's binder produces from
     * {@code DESTINY_AI_OPENROUTER_FALLBACK_MODELS=a,b,c} and cleans it up:
     * each entry trimmed, blanks removed, duplicates removed (first wins),
     * null treated as empty.
     *
     * <p>Normalizing here rather than at the call site means the chain-walking
     * code never has to defend against {@code ""} - an empty model id is not a
     * fallback, it is a request that will be rejected for a reason that has
     * nothing to do with availability, wasting one link of the chain and
     * confusing the logs.
     */
    public void setFallbackModels(List<String> fallbackModels) {
        List<String> cleaned = new ArrayList<>();
        if (fallbackModels != null) {
            for (String candidate : fallbackModels) {
                if (candidate == null) {
                    continue;
                }
                String trimmed = candidate.trim();
                if (!trimmed.isEmpty() && !cleaned.contains(trimmed)) {
                    cleaned.add(trimmed);
                }
            }
        }
        this.fallbackModels = List.copyOf(cleaned);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
