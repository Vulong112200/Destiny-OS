package io.destinyos.persistence.retention;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.CalculationContext;
import io.destinyos.core.evidence.Evidence;
import io.destinyos.core.result.EngineResult;
import io.destinyos.core.retention.RetentionClass;
import io.destinyos.core.signal.Applicability;
import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Signal;
import io.destinyos.core.signal.Strength;
import io.destinyos.core.version.MethodologyVersions;
import io.destinyos.execution.EngineExecution;
import io.destinyos.execution.ExecutionOutcome;
import io.destinyos.fusion.Conflict;
import io.destinyos.fusion.ConflictType;
import io.destinyos.fusion.DimensionAnalysis;
import io.destinyos.fusion.DimensionState;
import io.destinyos.fusion.FusionOutcome;
import io.destinyos.fusion.FusionResult;
import io.destinyos.persistence.TestApplication;
import io.destinyos.persistence.calculation.CalculationEngineResultRepository;
import io.destinyos.persistence.calculation.CalculationRecorder;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.EvidenceRepository;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The cleanup job against a real schema (CLAUDE.md §7,
 * DATA_MODEL_AND_RETENTION.md §11).
 *
 * <p>These tests exercise a <em>destructive</em> operation, so they are written
 * from the direction of what must never happen: a saved result must survive
 * even when expired, a dry run must delete nothing, and a delete must not trip
 * over its own foreign keys. Every fixture here is a full calculation —
 * evidence, signals that cite that evidence, a fusion result with conflicts —
 * because a purge test built on a bare {@code calculations} row would pass
 * while the real delete order was wrong.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestApplication.class, CalculationRecorder.class, RetentionConfiguration.class,
        CalculationPurger.class, CalculationRetentionService.class})
// NOT_SUPPORTED disables the rollback-per-test transaction @DataJpaTest normally
// wraps around each method, and that is essential rather than incidental here:
// CalculationPurger deletes in a REQUIRES_NEW transaction, which by definition
// cannot see rows the test transaction has written but not committed. Under the
// default wrapping transaction every purge silently found nothing and reported
// success - the test passed while deleting nothing, which is the worst possible
// outcome for a test guarding a destructive operation. Running uncommitted-free
// means fixtures really are committed, so the purge crosses the same transaction
// boundary it will cross in production. The cost is manual cleanup in
// @BeforeEach, paid below.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CalculationRetentionServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-22T10:00:00Z");

    @Autowired
    private CalculationRecorder recorder;
    @Autowired
    private CalculationRetentionService retention;
    @Autowired
    private CalculationRepository calculations;
    @Autowired
    private EvidenceRepository evidenceRepo;
    @Autowired
    private SignalRepository signalRepo;
    @Autowired
    private FusionResultRepository fusionResultRepo;
    @Autowired
    private ConflictRepository conflictRepo;
    @Autowired
    private CalculationEngineResultRepository engineResults;
    @Autowired
    private RetentionRunRepository runs;
    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Nothing rolls back here, so each test starts by clearing what the last one
     * committed. Order follows the foreign keys in V4-V7, deepest first - the
     * same order {@link CalculationPurger} uses, for the same reason.
     */
    @BeforeEach
    void clearCommittedFixtures() {
        jdbc.execute("DELETE FROM signal_evidence_refs");
        jdbc.execute("DELETE FROM signals");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM fusion_result_rules");
        jdbc.execute("DELETE FROM fusion_result_supporting_sources");
        jdbc.execute("DELETE FROM fusion_result_caution_sources");
        jdbc.execute("DELETE FROM fusion_results");
        jdbc.execute("DELETE FROM conflicts");
        jdbc.execute("DELETE FROM ai_narratives");
        jdbc.execute("DELETE FROM calculation_engine_results");
        jdbc.execute("DELETE FROM calculations");
        jdbc.execute("DELETE FROM retention_runs");
    }

    @Nested
    @DisplayName("Classification at write time")
    class Classification {

        @Test
        @DisplayName("A recorded run is EPHEMERAL with a real expiry")
        void recordedRunsAreClassified() {
            record("calc-ret-1", "BUSINESS");

            var saved = calculations.findById("calc-ret-1").orElseThrow();
            assertThat(saved.retentionClass()).isEqualTo(RetentionClass.EPHEMERAL);
            assertThat(saved.expiresAt()).isNotNull().isAfter(saved.completedAt());
        }

        @Test
        @DisplayName("A daily reading gets a shorter life than a business scenario")
        void dailyReadingsExpireSooner() {
            record("calc-ret-daily", "DAILY_ACTION");
            record("calc-ret-business", "BUSINESS");

            var daily = calculations.findById("calc-ret-daily").orElseThrow();
            var business = calculations.findById("calc-ret-business").orElseThrow();

            assertThat(daily.expiresAt()).isBefore(business.expiresAt());
        }
    }

    @Nested
    @DisplayName("Dry run")
    class DryRun {

        @Test
        @DisplayName("Finds candidates and deletes nothing")
        void dryRunDeletesNothing() {
            record("calc-dry-1", "DAILY_ACTION");
            expire("calc-dry-1");

            var summary = retention.dryRun(NOW);

            assertThat(summary.dryRun()).isTrue();
            assertThat(summary.candidatesFound()).isEqualTo(1);
            assertThat(summary.deleted()).isZero();
            assertThat(calculations.findById("calc-dry-1")).isPresent();
        }

        @Test
        @DisplayName("Still records an audit row — a rehearsal that happened is not the same as none")
        void dryRunIsAudited() {
            record("calc-dry-2", "DAILY_ACTION");
            expire("calc-dry-2");

            var summary = retention.dryRun(NOW);

            var run = runs.findById(summary.runId()).orElseThrow();
            assertThat(run.dryRun()).isTrue();
            assertThat(run.candidatesFound()).isEqualTo(1);
            assertThat(run.calculationsDeleted()).isZero();
            assertThat(run.cutoff()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("Purge")
    class Purge {

        @Test
        @DisplayName("An expired calculation and every child row are removed together")
        void purgeRemovesTheWholeGraph() {
            record("calc-purge-1", "DAILY_ACTION");
            expire("calc-purge-1");

            // Guard: the fixture really does have the children whose foreign
            // keys make delete order matter. Without this, a green purge test
            // could mean "nothing to delete".
            assertThat(evidenceRepo.findByCalculationId("calc-purge-1")).isNotEmpty();
            assertThat(signalRepo.findByCalculationId("calc-purge-1")).isNotEmpty();
            assertThat(conflictRepo.findByCalculationId("calc-purge-1")).isNotEmpty();
            assertThat(fusionResultRepo.findByCalculationId("calc-purge-1")).isPresent();

            var summary = retention.purgeExpired(NOW);

            assertThat(summary.deleted()).isEqualTo(1);
            assertThat(summary.failures()).isZero();
            assertThat(calculations.findById("calc-purge-1")).isEmpty();
            assertThat(evidenceRepo.findByCalculationId("calc-purge-1")).isEmpty();
            assertThat(signalRepo.findByCalculationId("calc-purge-1")).isEmpty();
            assertThat(conflictRepo.findByCalculationId("calc-purge-1")).isEmpty();
            assertThat(fusionResultRepo.findByCalculationId("calc-purge-1")).isEmpty();
            assertThat(engineResults.findByCalculationId("calc-purge-1")).isEmpty();
        }

        @Test
        @DisplayName("A calculation that has not expired yet is left alone")
        void unexpiredCalculationsSurvive() {
            record("calc-purge-fresh", "BUSINESS");

            var summary = retention.purgeExpired(NOW);

            assertThat(summary.candidatesFound()).isZero();
            assertThat(calculations.findById("calc-purge-fresh")).isPresent();
        }

        @Test
        @DisplayName("A saved result survives even after its old expiry date has passed")
        void userSavedResultsAreNeverDeleted() {
            // DATA_MODEL_AND_RETENTION.md §11: "không xóa USER_SAVED". This is
            // the single most important assertion in the file - the case where
            // the job would destroy something a user explicitly asked to keep.
            record("calc-saved", "DAILY_ACTION");
            expire("calc-saved");
            retention.markUserSaved("calc-saved");

            var summary = retention.purgeExpired(NOW);

            assertThat(summary.candidatesFound()).isZero();
            assertThat(summary.deleted()).isZero();
            assertThat(calculations.findById("calc-saved")).isPresent();
        }

        @Test
        @DisplayName("Saving clears the expiry rather than pushing it into the future")
        void savingClearsTheExpiry() {
            record("calc-save-clears", "DAILY_ACTION");
            expire("calc-save-clears");

            var saved = retention.markUserSaved("calc-save-clears").orElseThrow();

            assertThat(saved.retentionClass()).isEqualTo(RetentionClass.USER_SAVED);
            // Null, not year 9999: the UI must be able to say "will not be
            // deleted" without having to interpret a date.
            assertThat(saved.expiresAt()).isNull();
        }

        @Test
        @DisplayName("Saving is idempotent, and saving something that does not exist is empty")
        void savingIsIdempotent() {
            record("calc-save-twice", "DAILY_ACTION");

            assertThat(retention.markUserSaved("calc-save-twice")).isPresent();
            assertThat(retention.markUserSaved("calc-save-twice")).isPresent();
            assertThat(retention.markUserSaved("does-not-exist")).isEmpty();
        }

        @Test
        @DisplayName("Running twice finds nothing the second time")
        void purgeIsIdempotent() {
            record("calc-purge-idem", "DAILY_ACTION");
            expire("calc-purge-idem");

            assertThat(retention.purgeExpired(NOW).deleted()).isEqualTo(1);

            var second = retention.purgeExpired(NOW);
            assertThat(second.candidatesFound()).isZero();
            assertThat(second.deleted()).isZero();
        }

        @Test
        @DisplayName("A purge records what it did")
        void purgeIsAudited() {
            record("calc-purge-audit", "DAILY_ACTION");
            expire("calc-purge-audit");

            var summary = retention.purgeExpired(NOW);
            var run = runs.findById(summary.runId()).orElseThrow();

            assertThat(run.dryRun()).isFalse();
            assertThat(run.calculationsDeleted()).isEqualTo(1);
            assertThat(run.failures()).isZero();
            assertThat(run.firstFailure()).isNull();
            assertThat(run.completedAt()).isAfterOrEqualTo(run.startedAt());
        }

        @Test
        @DisplayName("A calculation expiring exactly now is included, not left for tomorrow")
        void expiryIsInclusive() {
            record("calc-boundary", "DAILY_ACTION");
            setExpiry("calc-boundary", NOW);

            assertThat(retention.purgeExpired(NOW).deleted()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Batching")
    class Batching
    {
        @Test
        @DisplayName("candidatesFound reports the whole backlog, not just this batch")
        void backlogIsVisible() {
            // The operator-facing property: a run that deletes its batch and
            // reports "candidates = batch size" would look like it had finished.
            // hasMoreWork() is what tells the scheduler to come back.
            for (int i = 0; i < 3; i++) {
                record("calc-batch-" + i, "DAILY_ACTION");
                expire("calc-batch-" + i);
            }

            var summary = retention.dryRun(NOW);

            assertThat(summary.candidatesFound()).isEqualTo(3);
            assertThat(summary.hasMoreWork()).isTrue();
        }

        @Test
        @DisplayName("A run that clears the backlog reports no more work")
        void emptyBacklogReportsDone() {
            record("calc-batch-done", "DAILY_ACTION");
            expire("calc-batch-done");

            var summary = retention.purgeExpired(NOW);

            assertThat(summary.deleted()).isEqualTo(1);
            assertThat(summary.hasMoreWork()).isFalse();
        }
    }

    // ---------- fixtures ----------

    /**
     * Records a full calculation: one engine, one evidence item, one signal
     * citing it, and a fusion result with a conflict. Deliberately not a bare
     * row — see this class's Javadoc.
     */
    private void record(String calculationId, String scenarioId) {
        var context = new CalculationContext(calculationId, "TEST_SCHOOL",
                new MethodologyVersions("1.0", "1.0", "1.0", "1.0"),
                ZoneId.of("Asia/Ho_Chi_Minh"), Locale.forLanguageTag("vi-VN"), 7L,
                Instant.parse("2026-05-01T00:00:00Z"), null, null,
                BirthTimePrecision.EXACT, List.of());

        String evidenceId = "ev-" + calculationId;
        var evidence = new Evidence(evidenceId, "TAROT", "RWS", "TAROT_SEEDED_DRAW", "1.0",
                Dimension.FINANCE, Map.of("cardName", "The Fool"), "seeded-draw", "g1", null);
        var signal = new Signal("sig-" + calculationId, "TAROT", "RWS", Dimension.FINANCE,
                "TAG", Polarity.SUPPORT, Strength.MEDIUM, Applicability.HIGH, false,
                List.of(evidenceId), "g1");

        var execution = new ExecutionOutcome(List.of(new EngineExecution("TAROT",
                EngineResult.success("payload", List.of(evidence), List.of(signal)),
                Duration.ofMillis(10), false)));

        var dimension = new DimensionAnalysis(Dimension.FINANCE, DimensionState.POSITIVE,
                Set.of("TAROT"), Set.of(), Set.of(), Set.of(), List.of(), List.of("R2"));
        var conflict = new Conflict(ConflictType.METHODOLOGY_CONFLICT, Dimension.FINANCE,
                List.of("TAROT", "NUMEROLOGY_PYTHAGOREAN"), "Hai phương pháp không đồng ý.");
        var fusion = new FusionResult(FusionOutcome.CONSENSUS_SUPPORT, List.of(dimension),
                List.of(conflict), List.of(), List.of("R2"), Set.of("TAROT"), Set.of());

        recorder.record(context, scenarioId, execution, fusion);
    }

    /** Backdates the expiry so the row is a cleanup candidate as of {@link #NOW}. */
    private void expire(String calculationId) {
        setExpiry(calculationId, NOW.minus(Duration.ofDays(1)));
    }

    /**
     * Sets {@code expires_at} directly. Plain SQL rather than the entity's own
     * API on purpose: {@code applyRetention} is package-private in the
     * {@code calculation} package precisely so nothing outside it can move an
     * expiry date, and a test should not be the exception that reopens it.
     */
    private void setExpiry(String calculationId, Instant expiresAt) {
        jdbc.update("UPDATE calculations SET expires_at = ? WHERE calculation_id = ?",
                java.sql.Timestamp.from(expiresAt), calculationId);
    }
}
