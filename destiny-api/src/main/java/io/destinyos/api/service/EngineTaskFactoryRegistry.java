package io.destinyos.api.service;

import java.util.Map;

/**
 * Keyed by engine id (not Spring bean name). Wrapped in its own type rather
 * than injecting a raw {@code Map<String, EngineTaskFactory>} bean, because
 * Spring's own "collect every bean of type X into a
 * {@code Map<String, X>} keyed by bean name" autowiring behaviour would
 * otherwise compete with an explicitly-defined map bean of the same
 * parameterized type — a subtle, version-dependent ambiguity not worth
 * risking when the keys need to be exact engine ids like {@code "TAROT"},
 * not bean names like {@code "tarotTaskFactory"}.
 */
public final class EngineTaskFactoryRegistry {

    private final Map<String, EngineTaskFactory> factories;

    public EngineTaskFactoryRegistry(Map<String, EngineTaskFactory> factories) {
        this.factories = Map.copyOf(factories);
    }

    public Map<String, EngineTaskFactory> all() {
        return factories;
    }
}
