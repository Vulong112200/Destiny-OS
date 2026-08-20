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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * AI_NARRATIVE_SPEC.md section 6's failure list, mapped one by one against a
 * faked OpenRouter HTTP endpoint - no real network call in this test suite,
 * matching how the rest of the reactor avoids depending on live external
 * services in its test runs.
 */
class OpenRouterNarrativeProviderTest {

    private static NarrativePrompt prompt() {
        return new NarrativePrompt(List.of(ChatMessage.system("sys"), ChatMessage.user("usr")));
    }

    private static OpenRouterProperties properties() {
        OpenRouterProperties properties = new OpenRouterProperties();
        properties.setApiKey("test-key");
        properties.setModel("test-model");
        properties.setBaseUrl("https://fake-openrouter.test/api/v1");
        return properties;
    }

    private record Fixture(OpenRouterNarrativeProvider provider, MockRestServiceServer server) {
    }

    private static Fixture fixture() {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties().getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        // Deliberately not calling .requestFactory(...) here (that's what
        // OpenRouterNarrativeProvider.buildRestClient does for production
        // wiring) - bindTo() above already installed the mock's request
        // factory on this builder, and setting another would replace it.
        RestClient restClient = builder.defaultHeader("Authorization", "Bearer test-key").build();
        return new Fixture(new OpenRouterNarrativeProvider(restClient, properties()), server);
    }

    @Test
    void successfulCallExtractsMessageContentAndModel() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andRespond(withSuccess("""
                        {"model": "test-model", "choices": [{"message": {"role": "assistant", "content": "{\\"summary\\":\\"ok\\"}"}}]}
                        """, MediaType.APPLICATION_JSON));

        ProviderCallResult result = fixture.provider().call(prompt());

        assertThat(result.success()).isTrue();
        assertThat(result.rawContent()).contains("\"summary\":\"ok\"");
        assertThat(result.modelUsed()).isEqualTo("test-model");
        fixture.server().verify();
    }

    @Test
    void tooManyRequestsMapsToRateLimited() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andRespond(withTooManyRequests());

        ProviderCallResult result = fixture.provider().call(prompt());

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.RATE_LIMITED);
    }

    @Test
    void serverErrorRetriesOnceThenMapsToServerError() {
        Fixture fixture = fixture();
        // Retried once (bounded, CLAUDE.md section 5) - both attempts must be primed.
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andRespond(withServerError());
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andRespond(withServerError());

        ProviderCallResult result = fixture.provider().call(prompt());

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.SERVER_ERROR);
        fixture.server().verify();
    }

    @Test
    void emptyChoicesMapsToEmptyResponse() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andRespond(withSuccess("{\"choices\": []}", MediaType.APPLICATION_JSON));

        ProviderCallResult result = fixture.provider().call(prompt());

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.EMPTY_RESPONSE);
    }

    @Test
    void malformedResponseBodyMapsToProviderUnavailableRatherThanThrowing() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo("https://fake-openrouter.test/api/v1/chat/completions"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.OK)
                        .body("this is not json")
                        .contentType(MediaType.APPLICATION_JSON));

        ProviderCallResult result = fixture.provider().call(prompt());

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(FallbackReason.PROVIDER_UNAVAILABLE);
    }
}
