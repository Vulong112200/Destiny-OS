package io.destinyos.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Architecture constraints, enforced mechanically (PHASE_1_PLAN section 7,
 * IMPLEMENTATION_AUDIT section 10.2).
 *
 * <p>Every rule here is also stated in prose somewhere in the specification.
 * That is exactly why they are tested: a documented convention is not a
 * constraint. Under delivery pressure, prose loses and the build does not.
 *
 * <p>These rules live in destiny-app because it is the only module with every
 * other module on its classpath.
 */
class ArchitectureRulesTest {

    private static final String ROOT = "io.destinyos";

    /**
     * Minimum class count we expect to import. Guards against the failure mode
     * that bit us once already: if the importer silently sees nothing, every
     * rule below "passes" while checking absolutely nothing.
     */
    private static final int MIN_EXPECTED_CLASSES = 20;

    /**
     * Production classes across every module.
     *
     * <p>Note the absence of {@code DoNotIncludeJars}. Sibling modules arrive on
     * this module's classpath as JARs, so excluding JARs excludes the very code
     * these rules exist to check. Scoping the import to {@link #ROOT} already
     * keeps third-party classes out.
     */
    private static JavaClasses production() {
        return new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
    }

    @Test
    @DisplayName("The importer actually sees the codebase (guards against vacuous passes)")
    void importerSeesRealClasses() {
        JavaClasses classes = production();

        var packages = new java.util.HashSet<String>();
        int count = 0;
        for (com.tngtech.archunit.core.domain.JavaClass javaClass : classes) {
            packages.add(javaClass.getPackageName());
            count++;
        }

        // Without this test, a misconfigured importer turns every rule below
        // into a no-op that reports success. An architecture suite that checks
        // nothing is worse than none at all: it manufactures false confidence.
        // This exact failure occurred once, hidden by stale target/ output until
        // a clean build exposed it.
        org.assertj.core.api.Assertions.assertThat(count)
                .as("ArchUnit imported too few classes - rules would pass vacuously")
                .isGreaterThan(MIN_EXPECTED_CLASSES);

        org.assertj.core.api.Assertions.assertThat(packages)
                .as("Core, engine SPI and execution packages must all be visible")
                .anyMatch(p -> p.startsWith("io.destinyos.core"))
                .anyMatch(p -> p.startsWith("io.destinyos.engine"))
                .anyMatch(p -> p.startsWith("io.destinyos.execution"));

        // This exact gap bit us once already: destiny-app never depended on
        // destiny-fusion or the concrete engine modules, so
        // fusionDependsOnlyOnTheSignalContract and enginesStayIndependent
        // below had been passing vacuously - checking zero real classes -
        // since Phase 5. Asserting these packages are actually on the
        // classpath is what would have caught it immediately.
        org.assertj.core.api.Assertions.assertThat(packages)
                .as("Fusion and concrete engine packages must be visible, or the rules "
                        + "that check them pass vacuously")
                .anyMatch(p -> p.startsWith("io.destinyos.fusion"))
                .anyMatch(p -> p.startsWith("io.destinyos.engines.tarot"))
                .anyMatch(p -> p.startsWith("io.destinyos.engines.numerology"));
    }

    @Test
    @DisplayName("destiny-core must not depend on Spring or JPA (ADR D1)")
    void coreStaysFrameworkFree() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("io.destinyos.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence..")
                .because("ADR D1 keeps the domain testable without a container. "
                        + "A framework dependency here makes every domain test "
                        + "pay for a Spring context.");

        rule.check(production());
    }

    @Test
    @DisplayName("The engine SPI must not depend on Spring")
    void engineApiStaysFrameworkFree() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("io.destinyos.engine..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("Engines are plain objects. Binding the SPI to a "
                        + "framework would force every future engine to adopt it.");

        rule.check(production());
    }

    @Test
    @DisplayName("No engine may depend on another engine (Master Spec section 0)")
    void enginesStayIndependent() {
        // Cross-engine calls would make source diversity meaningless: two
        // supposedly independent sources would share a derivation, and Fusion
        // would count one finding twice (FUSION_ENGINE_SPEC section 5).
        //
        // MUST use the slices() API, not a plain noClasses()/should() pair
        // with the same wildcard pattern on both sides: resideInAPackage
        // matches the literal pattern independently on each side and does
        // not correlate which concrete value "(*)" captured, so
        // "io.destinyos.engines.(*).." depending on
        // "io.destinyos.engines.(*).." is satisfied trivially by any class
        // inside ONE engine depending on another class in that SAME engine
        // (e.g. TarotEngine using TarotCard) - a false positive that fired
        // 182 times the moment a second real engine package existed to
        // compare against. slices() correctly treats each first-level
        // sub-package as its own slice and only flags a dependency that
        // crosses BETWEEN slices.
        com.tngtech.archunit.lang.ArchRule rule =
                com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                        .matching("io.destinyos.engines.(*)..")
                        .should().notDependOnEachOther()
                        .because("Master Spec section 0 and command section 3 require "
                                + "engines to stay independent, or source diversity "
                                + "counts one finding as several.");

        rule.check(production());
    }

    @Test
    @DisplayName("Fusion must not depend on any engine (ADR D5)")
    void fusionDependsOnlyOnTheSignalContract() {
        // This is the rule the whole roadmap rests on. ADR D2 orders Fusion
        // (Phase 6) before Bat Tu (Phase 8), which is only sound while Fusion
        // consumes Signal rather than any concrete engine.
        ArchRule rule = noClasses()
                .that().resideInAPackage("io.destinyos.fusion..")
                .should().dependOnClassesThat()
                .resideInAPackage("io.destinyos.engines..")
                .because("ADR D5: Fusion consumes the Signal contract only. "
                        + "Violating this invalidates the phase ordering in ADR D2.");

        rule.allowEmptyShould(true).check(production());
    }

    @Test
    @DisplayName("Controllers must not contain domain calculation (CLAUDE.md section 3)")
    void controllersStayThin() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("io.destinyos.api..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("io.destinyos.engines..")
                .because("CLAUDE.md section 3: controllers hold no domain "
                        + "calculation. They call the orchestrator, not engines.");

        rule.allowEmptyShould(true).check(production());
    }

    @Test
    @DisplayName("No domain type may expose a numeric score, weight or confidence (ADR D6)")
    void noProbabilityInTheDomain() {
        // The single most important rule in this file. CLAUDE.md Rule B and
        // FUSION_ENGINE_SPEC section 11 forbid presenting metaphysical signals
        // as probability. Once a double lives in the domain, averaging it
        // becomes the path of least resistance and evidence-based fusion
        // quietly degrades into the weighted average the spec rejects.
        ArchRule rule = fields()
                .that().areDeclaredInClassesThat()
                .resideInAnyPackage("io.destinyos.core..", "io.destinyos.fusion..")
                .and().areNotStatic()
                .should().notHaveRawType(double.class)
                .andShould().notHaveRawType(float.class)
                .andShould().notHaveRawType(Double.class)
                .andShould().notHaveRawType(Float.class)
                .andShould().notHaveRawType(java.math.BigDecimal.class)
                .because("ADR D6: no probability, score or weight in the domain. "
                        + "Make weighted averaging unrepresentable rather than "
                        + "merely discouraged.");

        rule.check(production());
    }

    @Test
    @DisplayName("The execution harness must not depend on a concrete engine")
    void harnessStaysGeneric() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("io.destinyos.execution..")
                .should().dependOnClassesThat()
                .resideInAPackage("io.destinyos.engines..")
                .because("The harness runs engines through the SPI. Knowing a "
                        + "concrete engine would let it special-case one.");

        rule.allowEmptyShould(true).check(production());
    }
}
