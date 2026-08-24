package io.destinyos.app.wiring;

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

    public WebCorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
