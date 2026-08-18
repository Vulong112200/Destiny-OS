package io.destinyos.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot assembly.
 *
 * <p>Deliberately thin in Phase 1. No engine, no controller, no persistence -
 * those arrive in their own phases. This module exists now so the ArchUnit
 * suite has somewhere to live with every other module on its classpath.
 */
@SpringBootApplication
public class DestinyOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DestinyOsApplication.class, args);
    }
}
