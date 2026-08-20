package io.destinyos.ai.openrouter;

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
 */
@ConfigurationProperties(prefix = "destiny.ai.openrouter")
public class OpenRouterProperties {

    private String apiKey = "";
    private String model = "";
    private String baseUrl = "https://openrouter.ai/api/v1";
    private int timeoutMs = 15_000;
    private int maxTokens = 800;
    private double temperature = 0.3;

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

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
