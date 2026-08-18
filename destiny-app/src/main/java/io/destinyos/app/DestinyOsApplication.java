package io.destinyos.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot assembly.
 *
 * <p>Explicit base packages rather than relying on the main class's own
 * package: {@code io.destinyos.app} would otherwise be the only package
 * Spring Boot auto-scans, which would silently hide every bean, entity and
 * repository in {@code destiny-persistence} (package {@code
 * io.destinyos.persistence}). Scanning {@code io.destinyos} covers every
 * module without needing to update this class each time a new one is added.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.destinyos")
@EntityScan(basePackages = "io.destinyos.persistence")
@EnableJpaRepositories(basePackages = "io.destinyos.persistence")
public class DestinyOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DestinyOsApplication.class, args);
    }
}
