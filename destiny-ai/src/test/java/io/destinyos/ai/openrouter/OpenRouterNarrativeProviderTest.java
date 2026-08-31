package io.destinyos.ai.openrouter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

import io.destinyos.ai.ChatMessage;
import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativePrompt;
import io.destinyos.ai.ProviderCallResult;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

/**
 * AI_NARRATIVE_SPEC.md section 6's failure list, mapped one by one against a
 * faked OpenRouter HTTP endpoint - no real network call in this test suite,
 * matching how the rest of the reactor avoids depending on live external
 * services in its test runs.
 *
 * <p>The chain tests at the bottom cover model fallback. They exist because a
 * single free model is unreliable by nature rather than by misconfiguration:
 * free models are rate-limited upstream at the inference provider and shared
 * across every OpenRouter user, so "my model 429s while another free model
 * answers" is normal, not an outage.
 */
class OpenRouterNarrativeProviderTest {

    private static final String URL = "https://fake-openrouter.test/api/v1/chat/completions";
    private static final String FALLBACK = "openrouter/free";

    /**
     * The acceptance predicate for tests that are not about acceptance.
     *
     * <p>Every test here exercises transport behaviour - status mapping,
     * retry, chain advance - so each one passes a predicate that accepts any
     * content, keeping the content-quality gate out of the way. The gate
     * itself is tested where it lives, in {@code NarrativeResponseParserTest}
     * and {@code NarrativeServiceTest}; the one provider-level test that cares
     * whether an unusable 200 advances the chain supplies its own predicate
     * rather than this one.
     */
    private static final Predicate<String> ACCEPT_ALL = content -> true;

    private static NarrativePrompt prompt() {
        return new NarrativePrompt(List.of(ChatMessage.system("sys"), ChatMessage.user("usr")));
    }

    private static OpenRouterProperties properties(String... fallbackModels) {
        OpenRouterProperties properties = new OpenRouterProperties();
        properties.setApiKey("test-key");
        properties.setModel("test-model");
        properties.setBaseUrl("https://fake-openrouter.test/api/v1");
        properties.setFallbackModels(List.of(fallbackModels));
        return properties;
    }

    private record Fixture(OpenRouterNarrativeProvider provider, MockRestServiceServer server) {

        /** Primes one expected call naming {@code model} in its body. */
        Fixture expecting(String model, ResponseCreator responder) {
            server.expect(requestTo(URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(jsonPath("$.model").value(model))
                    .andRespond(responder);
            return this;
        }
    }

    /**
     * A provider whose chain is {@code test-model} plus the given fallbacks.
     *
     * <p>Defaults to <em>no</em> fallbacks so the single-model tests below keep
     * asserting single-model behaviour - the production default of
     * {@code openrouter/free} would otherwise quietly turn every one of them
     * into a two-model test and stop them checking what they were written for.
     */
    private static Fixture fixture(String... fallbackModels) {
        OpenRouterProperties properties = properties(fallbackModels);
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        // Deliberately not calling .requestFactory(...) here (that's what
        // OpenRouterNarrativeProvider.buildRestClient does for production
        // wiring) - bindTo() above already installed the mock's request
        // factory on this builder, and setting another would replace it.
        RestClient restClient = builder.defaultHeader("Authorization", "Bearer test-key").build();
        return new Fixture(new OpenRouterNarrativeProvider(restClient, properties), server);
    }

    /** A 200 whose body names {@code model} and carries one usable choice. */
    private static ResponseCreator answeredBy(String model) {
        return withSuccess("""
                {"model": "%s", "choices": [{"message": {"role": "assistant", "content": "{\\"summary\\":\\"ok\\"}"}}]}
                """.formatted(model), MediaType.APPLICATION_JSON);
    }

    @Test
    void successfulCallExtractsMessageContentAndModel() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andRespond(answeredBy("test-model"));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isTrue();
        assertThat(result.rawContent()).contains("\"summary\":\"ok\"");
        assertThat(result.modelUsed()).isEqualTo("test-model");
        fixture.server().verify();
    }

    @Test
    void tooManyRequestsMapsToRateLimited() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(URL)).andRespond(withTooManyRequests());

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.RATE_LIMITED);
    }

    @Test
    void serverErrorRetriesOnceThenMapsToServerError() {
        Fixture fixture = fixture();
        // Retried once (bounded, CLAUDE.md section 5) - both attempts must be primed.
        fixture.server().expect(requestTo(URL)).andRespond(withServerError());
        fixture.server().expect(requestTo(URL)).andRespond(withServerError());

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.SERVER_ERROR);
        fixture.server().verify();
    }

    @Test
    void emptyChoicesMapsToEmptyResponse() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(URL))
                .andRespond(withSuccess("{\"choices\": []}", MediaType.APPLICATION_JSON));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.EMPTY_RESPONSE);
    }

    @Test
    void malformedResponseBodyMapsToProviderUnavailableRatherThanThrowing() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.OK)
                        .body("this is not json")
                        .contentType(MediaType.APPLICATION_JSON));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.PROVIDER_UNAVAILABLE);
    }

    @Test
    @DisplayName("Authentication failure is still PROVIDER_UNAVAILABLE, and does not retry")
    void unauthorizedMapsToProviderUnavailableWithoutRetrying() {
        // A quoted API key in .env produces exactly this. The mapping is
        // unchanged; what changed is that it now logs a distinct
        // "authentication rejected" line instead of nothing at all, which is
        // what made it indistinguishable from "no provider configured".
        Fixture fixture = fixture();
        fixture.expecting("test-model", withStatus(HttpStatus.UNAUTHORIZED));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.failureReason()).isEqualTo(FallbackReason.PROVIDER_UNAVAILABLE);
        // Only one expectation primed: a 401 is not retryable and must not be
        // retried, since nothing about a second identical call would differ.
        fixture.server().verify();
    }

    @Test
    @DisplayName("A rate-limited model hands over to the next model in the chain")
    void chainAdvancesPastARateLimitedModel() {
        // The measured production case: three free models returned 429
        // "temporarily rate-limited upstream" while two others returned 200 at
        // the same instant. 429 is not retryable, so the primary is tried
        // exactly once before the chain moves on.
        Fixture fixture = fixture(FALLBACK);
        fixture.expecting("test-model", withTooManyRequests())
                .expecting(FALLBACK, answeredBy("nvidia/nemotron-3-nano:free"));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isTrue();
        assertThat(result.rawContent()).contains("\"summary\":\"ok\"");
        fixture.server().verify();
    }

    @Test
    @DisplayName("An invalid model id hands over to the next model in the chain")
    void chainAdvancesPastAnInvalidModelId() {
        // The case OpenRouter's own "models": [...] request array does NOT
        // cover: a removed or misspelled id is validated upfront and the whole
        // request is rejected with 400 even when a valid fallback is listed
        // beside it. Recovering from it has to happen client-side, here.
        Fixture fixture = fixture(FALLBACK);
        fixture.expecting("test-model", withStatus(HttpStatus.BAD_REQUEST))
                .expecting(FALLBACK, answeredBy(FALLBACK));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isTrue();
        fixture.server().verify();
    }

    @Test
    @DisplayName("The model that answered is reported, not the configured primary")
    void reportsTheModelThatActuallyAnswered() {
        // NarrativeResponseDto.model must name the model that produced the
        // text. Reporting the configured primary would be a false claim about
        // provenance - and with openrouter/free the useful answer is the
        // concrete model OpenRouter routed to, which its response body names.
        Fixture fixture = fixture(FALLBACK);
        fixture.expecting("test-model", withServerError())
                .expecting("test-model", withServerError())
                .expecting(FALLBACK, answeredBy("nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free"));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.modelUsed()).isEqualTo("nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free");
        // SERVER_ERROR is retryable, so the primary used its full MAX_ATTEMPTS
        // before the chain advanced: retry semantics stay inside one model,
        // and priming exactly two primary calls is what asserts that.
        fixture.server().verify();
    }

    @Test
    @DisplayName("The first model to succeed wins, and no later model is called")
    void firstSuccessStopsTheChain() {
        Fixture fixture = fixture(FALLBACK);
        fixture.expecting("test-model", answeredBy("test-model"));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isTrue();
        assertThat(result.modelUsed()).isEqualTo("test-model");
        // verify() fails on any unconsumed expectation and only one was
        // primed, so this asserts the fallback was never contacted.
        fixture.server().verify();
    }

    @Test
    @DisplayName("When every model fails, the LAST model's reason is what surfaces")
    void allModelsFailingReturnsTheLastReason() {
        // Not the first model's reason: the chain exists precisely to route
        // around that one, so reporting it would explain the failure the
        // system recovered from rather than the one it did not.
        Fixture fixture = fixture(FALLBACK);
        fixture.expecting("test-model", withTooManyRequests())
                .expecting(FALLBACK, withStatus(HttpStatus.UNAUTHORIZED));

        ProviderCallResult result = fixture.provider().call(prompt(), ACCEPT_ALL);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason())
                .as("401 on the last model maps to PROVIDER_UNAVAILABLE, not the primary's RATE_LIMITED")
                .isEqualTo(FallbackReason.PROVIDER_UNAVAILABLE);
        fixture.server().verify();
    }

    @Test
    @DisplayName("A fallback naming the primary again is not tried twice")
    void chainDeduplicatesThePrimaryAgainstTheFallbackList() {
        // An operator listing the same model in both places should not pay for
        // two identical rounds against a model that has already failed.
        Fixture fixture = fixture("test-model", FALLBACK);
        fixture.expecting("test-model", withTooManyRequests())
                .expecting(FALLBACK, answeredBy(FALLBACK));

        assertThat(fixture.provider().call(prompt(), ACCEPT_ALL).success()).isTrue();
        fixture.server().verify();
    }

    @Test
    @DisplayName("Blank and duplicate fallback entries are dropped on binding")
    void fallbackModelListIsNormalized() {
        // What Spring's binder produces from a comma-separated env var with a
        // stray trailing comma or spaces. An empty model id is not a fallback:
        // it would waste a link of the chain on a request that fails for a
        // reason unrelated to availability, and muddy the logs while doing it.
        OpenRouterProperties properties = new OpenRouterProperties();
        properties.setFallbackModels(Arrays.asList(" a ", "", "  ", "a", null, "b"));

        assertThat(properties.getFallbackModels()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("The default chain adds openrouter/free, never openrouter/auto")
    void defaultFallbackIsTheFreeMetaModel() {
        // openrouter/auto is a paid router - measured, it answers 402
        // Insufficient credits on a free-tier account, which would turn
        // "AI enabled" into a silent permanent fallback on exactly the kind of
        // account this project targets.
        assertThat(new OpenRouterProperties().getFallbackModels()).containsExactly("openrouter/free");
    }
}
