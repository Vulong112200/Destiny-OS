package io.destinyos.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * ADR D8 end to end: whatever goes wrong with the provider, a renderable
 * {@link NarrativeResult} always comes back, never an exception.
 */
class NarrativeServiceTest {

    private static final NarrativeInput INPUT = new NarrativeInput("Kich ban thu", null, null, Set.of(), Map.of(), java.util.List.of(),
            java.util.List.of(), java.util.List.of(), java.util.List.of(), Map.of());

    private static AiProperties enabled() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        return properties;
    }

    private static AiProperties disabled() {
        return new AiProperties();
    }

    /** A minimal, in-memory stand-in for a real provider - AI_NARRATIVE_SPEC.md section 7's model independence. */
    private static final class FakeProvider implements AiNarrativeProvider {
        private final ProviderCallResult result;

        FakeProvider(ProviderCallResult result) {
            this.result = result;
        }

        @Override
        public String name() {
            return "fake";
        }

        @Override
        public ProviderCallResult call(NarrativePrompt prompt, java.util.function.Predicate<String> usableContent) {
            // Deliberately ignores usableContent: this fake has no chain to
            // advance, and AiNarrativeProvider allows that precisely so
            // NarrativeService is forced to keep its own gate. If the service
            // ever started trusting the predicate instead of re-parsing, these
            // tests would be the ones to catch it.
            return result;
        }
    }

    @Test
    void disabledAlwaysFallsBackWithoutCallingAnyProvider() {
        NarrativeService service = new NarrativeService(disabled(), Optional.of(new FakeProvider(
                ProviderCallResult.ok("{\"summary\":\"should never be read\"}", "some-model"))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.AI_DISABLED);
    }

    @Test
    void enabledWithNoProviderConfiguredFallsBack() {
        NarrativeService service = new NarrativeService(enabled(), Optional.empty());

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.NO_API_KEY);
    }

    @Test
    void successfulCallWithValidJsonReturnsAiGenerated() {
        String json = "{\"summary\": \"Tom tat that\", \"keySignals\": [], \"conflicts\": [], "
                + "\"cautions\": [], \"reflectionQuestions\": []}";
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.ok(json, "test-model"))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.AI_GENERATED);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.NONE);
        assertThat(result.response().summary()).isEqualTo("Tom tat that");
        assertThat(result.providerName()).isEqualTo("fake");
        assertThat(result.model()).isEqualTo("test-model");
    }

    @Test
    void malformedJsonFallsBackRatherThanPropagatingAnError() {
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.ok("not json", "test-model"))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.MALFORMED_JSON);
        assertThat(result.response().isWellFormed()).isTrue();
    }

    @Test
    void providerTimeoutFallsBack() {
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.failure(FallbackReason.TIMEOUT))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.TIMEOUT);
    }

    @Test
    void providerRateLimitedFallsBack() {
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.failure(FallbackReason.RATE_LIMITED))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.RATE_LIMITED);
    }

    @Test
    void emptyResponseFallsBack() {
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.ok("", "test-model"))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source()).isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.MALFORMED_JSON);
    }

    @Test
    void aSchemaTemplateEchoIsNotLabelledAsAnAiReading() {
        // The production defect. A free model returned this project's own
        // response schema back verbatim; every gate passed, and the user was
        // shown four bullets reading "..." headed "Diễn giải bởi AI".
        //
        // Two things had to be true and only one was: the content is useless
        // AND it was labelled as a genuine AI reading. The label is the part
        // that matters most - presenting empty output as a result is what the
        // honesty rules forbid - so this asserts the source, not just the text.
        String templateEcho = "{\"summary\": \"...\", \"keySignals\": [\"...\"], \"conflicts\": [\"...\"], "
                + "\"cautions\": [\"...\"], \"reflectionQuestions\": [\"...\"]}";
        NarrativeService service = new NarrativeService(enabled(),
                Optional.of(new FakeProvider(ProviderCallResult.ok(templateEcho, "echoing-model"))));

        NarrativeResult result = service.generate(INPUT);

        assertThat(result.source())
                .as("a template echo must never be presented as an AI-generated reading")
                .isEqualTo(NarrativeSource.FALLBACK);
        assertThat(result.fallbackReason()).isEqualTo(FallbackReason.MALFORMED_JSON);
        // And what the user gets instead is the deterministic report, which is
        // genuinely useful - the thing the echo had displaced.
        assertThat(result.response().isWellFormed()).isTrue();
        // containsIgnoringCase: the fallback opens the phrase at a sentence
        // start ("Dữ liệu tính toán gốc vẫn được giữ nguyên..."), and pinning
        // the capitalisation here would make this test fail for a reason that
        // has nothing to do with what it is checking.
        assertThat(result.response().summary()).containsIgnoringCase("dữ liệu tính toán gốc");
    }
}
