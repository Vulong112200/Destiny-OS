package io.destinyos.app.wiring;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows destiny-web (ADR D4) to call this API from a different origin.
 * Scoped to {@code /api/**} only — never opened up for the whole
 * application. Origins come from {@code app.cors.allowed-origins}
 * (env var {@code APP_CORS_ALLOWED_ORIGINS}, comma-separated), defaulting to
 * just the local Next.js dev server, so a deployed frontend's real origin
 * can be added without a code change.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    /**
     * Splits the comma-separated list, then <strong>trims every entry and
     * drops the empty ones</strong>.
     *
     * <p>Without the trim, {@code "https://a.com, https://b.com"} - written
     * the way anyone writes a list - produced the second origin as
     * {@code " https://b.com"} with a leading space. Spring compares
     * {@code Origin} headers by exact string equality, so that entry matches
     * nothing: the first origin works, the second is rejected with
     * {@code 403 Invalid CORS request}, and the operator is left staring at a
     * configuration value that visibly contains the origin being refused.
     * Dropping empty entries covers the same class of typo - a trailing comma,
     * or a value that is nothing but whitespace.
     */
    public WebCorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
