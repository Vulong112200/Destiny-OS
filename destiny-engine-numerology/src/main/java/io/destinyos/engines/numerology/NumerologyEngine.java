package io.destinyos.engines.numerology;

import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineError;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.signal.Dimension;
import io.destinyos.engine.EngineCapability;
import io.destinyos.engine.EngineMetadata;
import io.destinyos.engine.MetaphysicalEngine;
import io.destinyos.engine.MethodologyStatus;
import io.destinyos.engine.SupportedDateRange;
import io.destinyos.engine.ValidationResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Pythagorean numerology only (research item R8). Chaldean is not
 * implemented: no source establishes a Vietnamese-orthography letter mapping
 * for it, and constructing one would be inventing a methodology
 * (CLAUDE.md Rule C).
 *
 * <p><strong>What this engine does not do:</strong> assign meaning to a
 * number. "Life Path 8" has no interpretive text attached — that Vietnamese
 * content does not exist yet, the same situation {@code TarotEngine} is in
 * with card meanings (research item R11). This engine reports the computed
 * numbers as evidence and produces no signals, rather than inventing what a
 * number implies for FINANCE or CAREER.
 */
public final class NumerologyEngine implements MetaphysicalEngine<NumerologyInput, NumerologyProfile> {

    public static final String ENGINE_ID = "NUMEROLOGY_PYTHAGOREAN";

    private static final EngineMetadata METADATA = new EngineMetadata(
            ENGINE_ID,
            "Thần số học (Pythagoras)",
            "NUMEROLOGY_PYTHAGOREAN",
            "1.0",
            "1.0",
            "Pythagorean",
            "Standard A-Z letter table (converging across all sources checked); "
                    + "Vietnamese normalization policy and Life Path reduction order "
                    + "recorded in docs/RESEARCH_BLOCKERS.md R8, 2026-08-18",
            MethodologyStatus.CONTENT_REQUIRED
    );

    private static final EngineCapability CAPABILITY = EngineCapability.builder()
            .dimensions(Dimension.values())
            .requiresBirthTime(false)
            .requiresLocation(false)
            .requiresName(true)
            .requiresCalendar(false)
            .deterministic(true)
            .requiresSeed(false)
            .supportedDateRange(SupportedDateRange.unbounded())
            .build();

    @Override
    public EngineResult<NumerologyProfile> calculate(NumerologyInput input, CalculationContext context) {
        Objects.requireNonNull(input, "input");

        NumerologyProfile profile;
        try {
            profile = PythagoreanCalculator.compute(input.fullName(), input.birthDate());
        } catch (IllegalArgumentException e) {
            return EngineResult.invalidInput(List.of(
                    EngineError.of("NUMEROLOGY_INVALID_NAME", e.getMessage(), ENGINE_ID)));
        }

        List<Evidence> evidence = List.of(
                buildEvidence("NUMEROLOGY_LIFE_PATH", profile.lifePath(), profile),
                buildEvidence("NUMEROLOGY_EXPRESSION", profile.expression(), profile),
                buildEvidence("NUMEROLOGY_SOUL_URGE", profile.soulUrge(), profile),
                buildEvidence("NUMEROLOGY_PERSONALITY", profile.personality(), profile),
                buildEvidence("NUMEROLOGY_BIRTHDAY", profile.birthday(), profile)
        );

        // No signals: see class Javadoc. Numbers are computed and honest;
        // what a given number implies for any Dimension is content that
        // does not exist yet.
        return EngineResult.success(profile, evidence, List.of());
    }

    private static Evidence buildEvidence(String ruleId, NumerologyResult result,
                                          NumerologyProfile profile) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("value", result.value());
        fact.put("isMasterNumber", result.isMasterNumber());
        if (result.type() != NumerologyNumberType.LIFE_PATH
                && result.type() != NumerologyNumberType.BIRTHDAY) {
            fact.put("normalizedName", profile.normalizedName().displayForm());
        }

        return new Evidence(
                UUID.randomUUID().toString(),
                ENGINE_ID,
                METADATA.school(),
                ruleId,
                "1.0",
                Dimension.OTHER,
                fact,
                "pythagorean-calculation",
                null,
                null
        );
    }

    @Override
    public ValidationResult validateInput(NumerologyInput input) {
        if (input == null) {
            return ValidationResult.failed("NULL_INPUT", "Numerology input is required.", ENGINE_ID);
        }
        if (input.fullName().isBlank()) {
            return ValidationResult.failed("BLANK_NAME", "Full name must not be blank.", ENGINE_ID);
        }
        if (VietnameseNameNormalizer.tryNormalize(input.fullName()).isEmpty()) {
            return ValidationResult.failed("UNNORMALIZABLE_NAME",
                    "Name contains a character not recognised after normalization "
                            + "(research item R8): '" + input.fullName() + "'.", ENGINE_ID);
        }
        return ValidationResult.ok();
    }

    @Override
    public EngineCapability capability() {
        return CAPABILITY;
    }

    @Override
    public EngineMetadata metadata() {
        return METADATA;
    }
}
