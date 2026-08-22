package io.destinyos.persistence.retention;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds {@link RetentionProperties} and exposes the classifier as a bean.
 *
 * <p>{@link RetentionClassifier} is constructed here rather than annotated
 * {@code @Component} so it stays a plain object: the decision it makes is
 * consequential enough that its tests should be able to call it directly with
 * a hand-built policy, no container involved.
 */
@Configuration
@EnableConfigurationProperties(RetentionProperties.class)
public class RetentionConfiguration {

    @Bean
    public RetentionClassifier retentionClassifier(RetentionProperties properties) {
        return new RetentionClassifier(properties);
    }
}
