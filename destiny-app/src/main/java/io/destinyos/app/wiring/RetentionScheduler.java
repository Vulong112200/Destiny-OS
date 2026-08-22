package io.destinyos.app.wiring;

import io.destinyos.persistence.retention.CalculationRetentionService;
import io.destinyos.persistence.retention.RetentionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

/**
 * Runs the retention cleanup on a schedule
 * (DATA_MODEL_AND_RETENTION.md §11: <em>"Cron/job"</em>).
 *
 * <p><strong>The bean does not exist unless cleanup is switched on.</strong>
 * {@code @ConditionalOnProperty} is doing real work here rather than
 * decorating: an {@code if (properties.enabled())} inside the method would
 * leave a live scheduled task in the container that fires on time and does
 * nothing, and one refactor away from firing and doing something. No bean, no
 * task, no possibility of an accidental deletion in a deployment that never
 * asked for one.
 *
 * <p>{@code @EnableScheduling} sits here for the same reason — scheduling is
 * turned on by the thing that needs it, not globally by the application class
 * where it would silently apply to any future {@code @Scheduled} anyone adds.
 *
 * <p>Lives in {@code destiny-app} because this is assembly: the deletion logic
 * and its policy belong to {@code destiny-persistence}, and <em>when</em> to
 * invoke them is a deployment concern.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "destiny.retention.enabled", havingValue = "true")
public class RetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetentionScheduler.class);

    private final CalculationRetentionService retention;

    public RetentionScheduler(CalculationRetentionService retention, RetentionProperties properties) {
        this.retention = retention;
        log.info("Retention cleanup is ENABLED: cron={} dailyDuration={} transientDuration={} "
                        + "batchSize={}", properties.cron(), properties.dailyDuration(),
                properties.transientDuration(), properties.batchSize());
    }

    /**
     * One batch per tick, deliberately — not a loop until the queue is empty.
     *
     * <p>A backlog (say, the first run after this feature ships) drains over
     * several nights instead of in one enormous pass, which keeps the blast
     * radius of a misconfiguration to {@code batchSize} rows and leaves an
     * operator a night to notice. {@code hasMoreWork()} says so in the log
     * rather than leaving the operator to infer it.
     */
    @Scheduled(cron = "${destiny.retention.cron}")
    public void purgeExpiredCalculations() {
        var summary = retention.purgeExpired(Instant.now());
        if (summary.hasMoreWork()) {
            log.info("Retention backlog remains after run {}: {} candidates still expired. "
                            + "The next scheduled run will continue.",
                    summary.runId(), summary.candidatesFound() - summary.deleted());
        }
        if (summary.failures() > 0) {
            log.error("Retention run {} could not delete {} calculation(s). First failure: {}",
                    summary.runId(), summary.failures(), summary.firstFailure());
        }
    }
}
