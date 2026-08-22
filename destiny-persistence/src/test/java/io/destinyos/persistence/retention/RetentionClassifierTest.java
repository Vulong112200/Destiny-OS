package io.destinyos.persistence.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.destinyos.core.retention.RetentionClass;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The retention decision, tested without a container.
 *
 * <p>This class decides when a user's data becomes deletable, so its tests are
 * deliberately the cheapest and most direct in the module: a wrong answer here
 * destroys data, and nothing about verifying it should depend on a Spring
 * context booting correctly.
 */
class RetentionClassifierTest {

    private static final Instant COMPLETED = Instant.parse("2026-08-22T10:00:00Z");

    private static RetentionClassifier classifier() {
        return new RetentionClassifier(RetentionProperties.defaults());
    }

    @Test
    @DisplayName("A daily reading expires after the daily duration")
    void dailyScenarioUsesTheDailyDuration() {
        var decision = classifier().classify("DAILY_ACTION", COMPLETED);

        assertThat(decision.retentionClass()).isEqualTo(RetentionClass.EPHEMERAL);
        assertThat(decision.expiresAt()).isEqualTo(COMPLETED.plus(Duration.ofDays(30)));
    }

    @Test
    @DisplayName("Any other scenario expires after the longer transient duration")
    void otherScenariosUseTheTransientDuration() {
        var decision = classifier().classify("BUSINESS", COMPLETED);

        assertThat(decision.expiresAt()).isEqualTo(COMPLETED.plus(Duration.ofDays(90)));
    }

    @Test
    @DisplayName("Scenario matching is case-insensitive")
    void scenarioMatchingIsCaseInsensitive() {
        // The API accepts /api/v1/scenarios/daily_action in lowercase, and a
        // classifier that only matched the uppercase enum name would silently
        // give daily readings the 90-day lifetime instead of 30.
        assertThat(classifier().classify("daily_action", COMPLETED).expiresAt())
                .isEqualTo(COMPLETED.plus(Duration.ofDays(30)));
    }

    @Test
    @DisplayName("A run with no scenario is transient, not exempt")
    void nullScenarioIsStillEphemeral() {
        var decision = classifier().classify(null, COMPLETED);

        assertThat(decision.retentionClass()).isEqualTo(RetentionClass.EPHEMERAL);
        assertThat(decision.expiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Expiry is measured from the run's completion, never from wall-clock now")
    void expiryIsReproducible() {
        // Master Spec §25: recording the same run twice must give the same
        // answer. An expiry computed from Instant.now() would drift between
        // calls and make the stored row non-reproducible.
        var first = classifier().classify("BUSINESS", COMPLETED);
        var second = classifier().classify("BUSINESS", COMPLETED);

        assertThat(second.expiresAt()).isEqualTo(first.expiresAt());
    }

    @Test
    @DisplayName("The expiry is truncated to milliseconds so it round-trips through the database")
    void expiryRoundTripsThroughTheDatabase() {
        // PostgreSQL and H2 store timestamps at microsecond precision, so an
        // Instant carrying nanoseconds is not the value that comes back out.
        // A real end-to-end test caught the API reporting two different expiry
        // instants for the same row - once at run time, once on read-back.
        var withNanos = Instant.parse("2026-08-22T10:00:00Z").plusNanos(123_456_789);

        var expiry = classifier().classify("BUSINESS", withNanos).expiresAt();

        assertThat(expiry).isEqualTo(expiry.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        assertThat(expiry.getNano() % 1_000_000).isZero();
    }

    @Test
    @DisplayName("A configured policy overrides the defaults")
    void policyIsConfigurable() {
        // DATA_MODEL_AND_RETENTION.md §8: "Policy phải configurable". The
        // spec's lower bound is 7 days for daily readings, so an operator must
        // be able to actually choose it.
        var strict = new RetentionProperties(true, Duration.ofDays(7), Duration.ofDays(30),
                100, Set.of("DAILY_ACTION"), null);

        assertThat(new RetentionClassifier(strict).classify("DAILY_ACTION", COMPLETED).expiresAt())
                .isEqualTo(COMPLETED.plus(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("A non-positive duration is rejected at startup, not at deletion time")
    void nonPositiveDurationIsRejected() {
        // A zero lifetime would make every reading deletable the instant it was
        // written. Failing at bind time turns that from a silent data-loss bug
        // into a startup error someone reads.
        assertThatThrownBy(() -> new RetentionProperties(true, Duration.ZERO, null, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");

        assertThatThrownBy(() -> new RetentionProperties(true, Duration.ofDays(-1), null, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("An EPHEMERAL decision without an expiry is unrepresentable")
    void ephemeralMustCarryAnExpiry() {
        // The dangerous half of the invariant: a row labelled temporary with no
        // expiry is kept forever while claiming otherwise - the unbounded growth
        // CLAUDE.md §7 forbids, wearing a label that says it isn't happening.
        assertThatThrownBy(() -> new RetentionClassifier.RetentionDecision(
                RetentionClass.EPHEMERAL, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kept forever");
    }

    @Test
    @DisplayName("A never-deleted class carrying an expiry is unrepresentable")
    void nonDeletableClassesRejectAnExpiry() {
        for (RetentionClass retentionClass : RetentionClass.values()) {
            if (retentionClass.isAutoDeletable()) {
                continue;
            }
            assertThatThrownBy(() -> new RetentionClassifier.RetentionDecision(
                    retentionClass, COMPLETED))
                    .as("%s must not accept an expiry", retentionClass)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("Only EPHEMERAL is auto-deletable")
    void onlyEphemeralIsAutoDeletable() {
        // Stated as a test because it is the single fact the whole cleanup job
        // rests on: adding a future class must force a decision here rather
        // than defaulting to deletable.
        assertThat(RetentionClass.EPHEMERAL.isAutoDeletable()).isTrue();
        assertThat(RetentionClass.USER_SAVED.isAutoDeletable()).isFalse();
        assertThat(RetentionClass.PERSISTENT.isAutoDeletable()).isFalse();
        assertThat(RetentionClass.AUDIT.isAutoDeletable()).isFalse();
    }
}
