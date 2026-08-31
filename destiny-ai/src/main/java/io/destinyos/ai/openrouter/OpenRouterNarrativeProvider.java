package io.destinyos.ai.openrouter;

import com.fasterxml.jackson.databind.JsonNode;
import io.destinyos.ai.AiNarrativeProvider;
import io.destinyos.ai.ChatMessage;
import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativePrompt;
import io.destinyos.ai.ProviderCallResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * OpenRouter (an OpenAI-compatible {@code /chat/completions} API) as one
 * {@link AiNarrativeProvider} implementation - AI_NARRATIVE_SPEC.md section 7
 * names it as "a provider option", never the only one. The API key is read
 * server-side only from {@link OpenRouterProperties} and never appears in
 * any response this class returns (Master Spec section 28, "API keys never
 * exposed"); only the model's rendered text ever reaches
 * {@link ProviderCallResult}.
 *
 * <p>Walks a chain of models rather than calling one: the configured
 * {@link OpenRouterProperties#getModel() primary}, then each
 * {@link OpenRouterProperties#getFallbackModels() fallback} in order. Free
 * models are rate-limited <em>upstream at the inference provider</em> and
 * shared across every OpenRouter user, so "my model returns 429 right now
 * while another free model returns 200" is the normal weather here, not an
 * outage. See {@code OpenRouterProperties#fallbackModels} for why the chain is
 * walked client-side instead of using OpenRouter's own
 * {@code "models": [...]} request array.
 */
public class OpenRouterNarrativeProvider implements AiNarrativeProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterNarrativeProvider.class);

    /** One retry on a transient failure, never more - CLAUDE.md section 5 forbids infinite retry. */
    private static final int MAX_ATTEMPTS = 2;

    private final RestClient restClient;
    private final OpenRouterProperties properties;

    /**
     * Takes an already-built {@link RestClient} rather than a
     * {@code RestClient.Builder} that this constructor configures itself.
     * That split matters for testability: {@code MockRestServiceServer}
     * intercepts by installing its own request factory onto a builder, and
     * whichever call to {@code requestFactory(...)} happens LAST on that
     * builder wins. If this constructor called {@code .requestFactory(...)}
     * itself, it would silently discard a test's mock and every test would
     * hit the real network instead. {@link #buildRestClient} does that
     * configuration once, for production wiring only (see
     * {@code OpenRouterAutoConfiguration}); tests build their own
     * mock-bound {@code RestClient} and pass it here directly.
     */
    public OpenRouterNarrativeProvider(RestClient restClient, OpenRouterProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    static RestClient buildRestClient(RestClient.Builder builder, OpenRouterProperties properties) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(timeoutBoundedFactory(properties.getTimeoutMs()))
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .build();
    }

    private static ClientHttpRequestFactory timeoutBoundedFactory(int timeoutMs) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        return ClientHttpRequestFactories.get(settings);
    }

    @Override
    public String name() {
        return "openrouter";
    }

    /**
     * Tries each model in {@link #modelChain()} in order and returns the first
     * success.
     *
     * <p>Retry semantics are unchanged and stay <em>inside</em> one model:
     * {@link #MAX_ATTEMPTS} attempts, retried only on TIMEOUT or SERVER_ERROR.
     * A model exhausted for any reason - retried out, rate-limited, invalid
     * id, empty response - hands over to the next. Total attempts are
     * therefore bounded by {@code MAX_ATTEMPTS x chain size}: still finite, as
     * CLAUDE.md section 5 requires. Chain length is the operator's own
     * configuration and each link carries its own timeout, so a request's
     * worst-case latency is something they set rather than something this
     * class decides for them.
     *
     * <p>When everything fails, the <em>last</em> failure reason is returned.
     * That is the reason belonging to the last model actually tried, which is
     * what a reader of {@code NarrativeResponseDto.fallbackReason} needs in
     * order to understand why the chain ran out - not the first model's
     * reason, which the chain existed precisely to route around.
     *
     * <p>A model whose content the caller rejects is treated as a failed model
     * and the chain moves on, exactly as it does for a 429. This matters more
     * than it looks: measured in production, a free model returned a clean 200
     * whose body was this project's own response schema echoed back verbatim
     * ({@code {"summary": "...", "keySignals": ["..."]}}). Transport-level
     * checks cannot see that, so without the caller's acceptance test the chain
     * would stop at the first model fluent enough to answer with nothing.
     */
    @Override
    public ProviderCallResult call(NarrativePrompt prompt, Predicate<String> usableContent) {
        List<String> chain = modelChain();
        ProviderCallResult result = ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);

        for (int i = 0; i < chain.size(); i++) {
            String model = chain.get(i);
            result = accepted(model, callWithRetries(prompt, model), usableContent);
            if (result.success()) {
                if (i > 0) {
                    // Worth an INFO: "the narrative came from a model I never
                    // configured" is surprising the first time it shows up in
                    // a response, and this is the line that explains it.
                    log.info("OpenRouter model {} answered after {} earlier model(s) in the chain failed.",
                            result.modelUsed(), i);
                }
                return result;
            }
            if (i < chain.size() - 1) {
                log.warn("OpenRouter model {} failed ({}); trying next model in the chain.",
                        model, result.failureReason());
            }
        }

        log.warn("Every OpenRouter model in the chain failed ({} tried); last reason was {}. "
                + "Falling back to the deterministic hard-data report.", chain.size(), result.failureReason());
        return result;
    }

    /**
     * The primary model followed by the configured fallbacks, with blanks and
     * duplicates removed.
     *
     * <p>The primary is deduplicated against the fallback list so an operator
     * who names the same model in both places does not pay for two identical
     * rounds of attempts against a model that has already failed.
     *
     * <p>A blank primary would still yield a usable chain from the fallbacks
     * alone - but it cannot occur, because
     * {@code OpenRouterConfiguredCondition} refuses to create this bean at all
     * without one. That check is deliberately not relaxed: an empty primary
     * quietly meaning "just use the fallback" would hide a misconfiguration
     * the operator should be told about.
     */
    private List<String> modelChain() {
        List<String> chain = new ArrayList<>();
        String primary = properties.getModel();
        if (primary != null && !primary.isBlank()) {
            chain.add(primary.trim());
        }
        for (String fallback : properties.getFallbackModels()) {
            if (!chain.contains(fallback)) {
                chain.add(fallback);
            }
        }
        return chain;
    }

    /**
     * Downgrades a transport-level success whose content the caller will not
     * use into a per-model failure, so the chain keeps going.
     *
     * <p>Reported as {@link FallbackReason#MALFORMED_JSON} because that is
     * precisely what happened: the HTTP call worked and the payload failed the
     * caller's schema gate. It is also what {@link io.destinyos.ai.NarrativeService}
     * would have reported had the content reached it, so the reason a user
     * finally sees does not depend on which layer noticed.
     *
     * <p>Logged at WARN, naming the model. A model that habitually echoes the
     * template is a configuration problem an operator can act on - drop it from
     * the chain - but only if it is visible. Silently burning an attempt on it
     * every single request, while the chain quietly covers for it, is exactly
     * the kind of invisible degradation ADR D8 makes comfortable to ignore.
     * The content itself is never logged: it is model output, not a diagnostic,
     * and the same no-bodies rule as the catch blocks applies.
     */
    private ProviderCallResult accepted(String model, ProviderCallResult result,
                                        Predicate<String> usableContent) {
        if (!result.success() || usableContent.test(result.rawContent())) {
            return result;
        }
        log.warn("OpenRouter model {} returned content the narrative layer rejected (it did not parse as a "
                + "well-formed narrative, e.g. the response schema echoed back verbatim). Mapped to {}; "
                + "trying the rest of the chain.", model, FallbackReason.MALFORMED_JSON);
        return ProviderCallResult.failure(FallbackReason.MALFORMED_JSON);
    }

    private ProviderCallResult callWithRetries(NarrativePrompt prompt, String model) {
        ProviderCallResult result = ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            result = attemptCall(prompt, model);
            if (result.success() || !isRetryable(result.failureReason())) {
                return result;
            }
        }
        return result;
    }

    private boolean isRetryable(FallbackReason reason) {
        return reason == FallbackReason.TIMEOUT || reason == FallbackReason.SERVER_ERROR;
    }

    /**
     * One HTTP attempt against one model.
     *
     * <p>Every branch below logs. Before they did, this method mapped every
     * failure to a {@link FallbackReason} and said nothing at all, which made
     * a misconfiguration indistinguishable from an absent provider: an API key
     * accidentally left quoted in {@code .env} produced a 401, which became
     * {@code PROVIDER_UNAVAILABLE}, which rendered the same deterministic
     * fallback as "no provider configured" - with no log line anywhere to tell
     * the two apart. ADR D8's guarantee that the system stays usable is
     * exactly what makes a silent misconfiguration easy to live with by
     * accident, so this logging is what keeps "degraded" from looking like
     * "fine".
     *
     * <p>What is never logged: the API key, the request body, and the response
     * body (Master Spec section 28). {@code HttpClientErrorException.getMessage()}
     * embeds the response body, so these lines carry the status code and the
     * exception type rather than the exception's message. The model id is the
     * one identifying detail worth having, and it is public configuration.
     */
    private ProviderCallResult attemptCall(NarrativePrompt prompt, String model) {
        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody(prompt, model))
                    .retrieve()
                    .body(JsonNode.class);
            return logged(model, extractContent(response, model));
        } catch (HttpClientErrorException.TooManyRequests e) {
            // Usually not this account's quota: free models are rate-limited
            // upstream at the inference provider and shared by every
            // OpenRouter user, which is the whole reason a chain exists.
            log.warn("OpenRouter rate-limited model {} (HTTP 429). Mapped to {}.",
                    model, FallbackReason.RATE_LIMITED);
            return ProviderCallResult.failure(FallbackReason.RATE_LIMITED);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            // The failure that cost a full debugging cycle to find. Called out
            // separately from other 4xx because the fix is entirely different:
            // nothing about the model or the chain will help.
            log.warn("OpenRouter rejected authentication for model {} (HTTP {}): the API key was not accepted. "
                    + "Check destiny.ai.openrouter.api-key / OPENROUTER_API_KEY - a value accidentally wrapped "
                    + "in quotes in .env is sent literally and fails this way. Mapped to {}.",
                    model, e.getStatusCode().value(), FallbackReason.PROVIDER_UNAVAILABLE);
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        } catch (HttpClientErrorException e) {
            // Includes the retired or misspelled model id: OpenRouter
            // validates the id upfront and answers 400 "... is not a valid
            // model ID". Nothing is retried and the chain moves on - which is
            // exactly the case OpenRouter's own "models" array does NOT cover,
            // since it rejects the whole request even with a valid fallback
            // listed alongside.
            log.warn("OpenRouter rejected the request for model {} (HTTP {}). If this is 400, the model id may "
                    + "no longer exist. Mapped to {}.",
                    model, e.getStatusCode().value(), FallbackReason.PROVIDER_UNAVAILABLE);
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        } catch (HttpServerErrorException e) {
            log.warn("OpenRouter returned a server error for model {} (HTTP {}). Mapped to {}.",
                    model, e.getStatusCode().value(), FallbackReason.SERVER_ERROR);
            return ProviderCallResult.failure(FallbackReason.SERVER_ERROR);
        } catch (ResourceAccessException e) {
            log.warn("OpenRouter call for model {} did not complete within the configured timeout ({} ms). "
                    + "Mapped to {}.", model, properties.getTimeoutMs(), FallbackReason.TIMEOUT);
            return ProviderCallResult.failure(FallbackReason.TIMEOUT);
        } catch (RestClientException e) {
            log.warn("OpenRouter call for model {} failed with {}. Mapped to {}.",
                    model, e.getClass().getSimpleName(), FallbackReason.PROVIDER_UNAVAILABLE);
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        } catch (RuntimeException e) {
            // AiNarrativeProvider's contract is "never throws" - a response
            // body Jackson cannot parse (HttpMessageNotReadableException) is
            // a sibling of RestClientException, not a subtype of it, so it
            // would otherwise escape the catches above and violate that
            // contract. Anything unexpected here is exactly as unusable to
            // NarrativeService as a network failure, so it maps the same way.
            log.warn("OpenRouter call for model {} failed unexpectedly with {}. Mapped to {}.",
                    model, e.getClass().getSimpleName(), FallbackReason.PROVIDER_UNAVAILABLE);
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        }
    }

    /**
     * Logs a non-exceptional failure (a 200 whose body carried no usable
     * content) on its way past. An empty response leaves no stack trace and no
     * HTTP status behind, so without this it is the one failure category that
     * would stay invisible even after the catches above started logging.
     */
    private ProviderCallResult logged(String model, ProviderCallResult result) {
        if (!result.success()) {
            log.warn("OpenRouter model {} returned a response with no usable content. Mapped to {}.",
                    model, result.failureReason());
        }
        return result;
    }

    private Map<String, Object> requestBody(NarrativePrompt prompt, String model) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", prompt.messages().stream().map(this::toMessageMap).toList());
        body.put("max_tokens", properties.getMaxTokens());
        body.put("temperature", properties.getTemperature());
        return body;
    }

    private Map<String, String> toMessageMap(ChatMessage message) {
        return Map.of("role", message.role(), "content", message.content());
    }

    private ProviderCallResult extractContent(JsonNode response, String requestedModel) {
        if (response == null) {
            return ProviderCallResult.failure(FallbackReason.EMPTY_RESPONSE);
        }
        JsonNode choices = response.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return ProviderCallResult.failure(FallbackReason.EMPTY_RESPONSE);
        }
        String content = choices.get(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            return ProviderCallResult.failure(FallbackReason.EMPTY_RESPONSE);
        }
        // Defaults to the model actually requested, not the configured
        // primary: with a chain those differ exactly when it matters most, and
        // reporting the primary would make NarrativeResponseDto.model claim an
        // answer came from a model that never produced it. When the requested
        // model is a meta-model such as openrouter/free, OpenRouter reports
        // the concrete model it routed to, which is better still.
        String modelUsed = response.path("model").asText(requestedModel);
        return ProviderCallResult.ok(content, modelUsed);
    }
}
