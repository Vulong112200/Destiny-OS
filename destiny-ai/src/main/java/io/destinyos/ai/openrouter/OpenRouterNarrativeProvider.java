package io.destinyos.ai.openrouter;

import com.fasterxml.jackson.databind.JsonNode;
import io.destinyos.ai.AiNarrativeProvider;
import io.destinyos.ai.ChatMessage;
import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativePrompt;
import io.destinyos.ai.ProviderCallResult;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
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
 */
public class OpenRouterNarrativeProvider implements AiNarrativeProvider {

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

    @Override
    public ProviderCallResult call(NarrativePrompt prompt) {
        ProviderCallResult result = ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            result = attemptCall(prompt);
            if (result.success() || !isRetryable(result.failureReason())) {
                return result;
            }
        }
        return result;
    }

    private boolean isRetryable(FallbackReason reason) {
        return reason == FallbackReason.TIMEOUT || reason == FallbackReason.SERVER_ERROR;
    }

    private ProviderCallResult attemptCall(NarrativePrompt prompt) {
        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody(prompt))
                    .retrieve()
                    .body(JsonNode.class);
            return extractContent(response);
        } catch (HttpClientErrorException.TooManyRequests e) {
            return ProviderCallResult.failure(FallbackReason.RATE_LIMITED);
        } catch (HttpServerErrorException e) {
            return ProviderCallResult.failure(FallbackReason.SERVER_ERROR);
        } catch (ResourceAccessException e) {
            return ProviderCallResult.failure(FallbackReason.TIMEOUT);
        } catch (RestClientException e) {
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        } catch (RuntimeException e) {
            // AiNarrativeProvider's contract is "never throws" - a response
            // body Jackson cannot parse (HttpMessageNotReadableException) is
            // a sibling of RestClientException, not a subtype of it, so it
            // would otherwise escape the catches above and violate that
            // contract. Anything unexpected here is exactly as unusable to
            // NarrativeService as a network failure, so it maps the same way.
            return ProviderCallResult.failure(FallbackReason.PROVIDER_UNAVAILABLE);
        }
    }

    private Map<String, Object> requestBody(NarrativePrompt prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("messages", prompt.messages().stream().map(this::toMessageMap).toList());
        body.put("max_tokens", properties.getMaxTokens());
        body.put("temperature", properties.getTemperature());
        return body;
    }

    private Map<String, String> toMessageMap(ChatMessage message) {
        return Map.of("role", message.role(), "content", message.content());
    }

    private ProviderCallResult extractContent(JsonNode response) {
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
        String modelUsed = response.path("model").asText(properties.getModel());
        return ProviderCallResult.ok(content, modelUsed);
    }
}
