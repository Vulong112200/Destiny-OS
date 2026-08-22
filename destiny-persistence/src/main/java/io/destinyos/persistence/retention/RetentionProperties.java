package io.destinyos.persistence.retention;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The configurable half of the retention policy
 * (DATA_MODEL_AND_RETENTION.md §8: <em>"Policy phải configurable"</em>).
 *
 * <p>The defaults below sit inside the ranges §8 states rather than at their
 * edges, and each is the more conservative end:
 *
 * <table>
 *   <caption>Spec range vs default chosen</caption>
 *   <tr><th>Reading</th><th>§8 range</th><th>Default here</th></tr>
 *   <tr><td>Daily Action / Daily Tarot</td><td>7–30 days</td><td>30 days</td></tr>
 *   <tr><td>Transient scenario</td><td>30–90 days</td><td>90 days</td></tr>
 *   <tr><td>Saved report, natal chart</td><td>persistent</td><td>never expires</td></tr>
 * </table>
 *
 * <p>Keeping longer than the minimum is the safe direction to be wrong in: a
 * user who comes back after three weeks still finds their reading, and an
 * operator who wants aggressive pruning can set a smaller number without a
 * code change. The reverse mistake destroys data.
 *
 * <p>{@code enabled} defaults to {@code false}. A scheduled job that deletes
 * rows must be switched on deliberately by whoever runs the system — not
 * arrive switched on because a dependency was added.
 *
 * @param enabled           whether the scheduled cleanup runs at all
 * @param dailyDuration     lifetime of a daily reading
 * @param transientDuration lifetime of any other non-saved scenario run
 * @param batchSize         maximum calculations deleted per run
 *                          (DATA_MODEL_AND_RETENTION.md §11 "batch delete") —
 *                          bounds both the transaction and the damage a
 *                          misconfiguration can do in one pass
 * @param dailyScenarioIds  which scenario ids count as "daily" for
 *                          {@code dailyDuration}; anything else that is not
 *                          saved gets {@code transientDuration}
 * @param cron              schedule for the cleanup job, read by
 *                          {@code RetentionScheduler} in {@code destiny-app}.
 *                          Bound here rather than read straight from the
 *                          environment so an invalid retention config fails at
 *                          startup in one place, next to the durations it
 *                          belongs with
 */
@ConfigurationProperties(prefix = "destiny.retention")
public record RetentionProperties(
        boolean enabled,
        Duration dailyDuration,
        Duration transientDuration,
        int batchSize,
        Set<String> dailyScenarioIds,
        String cron
) {
    /** Master Spec's "Daily Action" scenario, the one §8 names by that word. */
    private static final Set<String> DEFAULT_DAILY_SCENARIOS = Set.of("DAILY_ACTION");

    public RetentionProperties {
        dailyDuration = dailyDuration == null ? Duration.ofDays(30) : dailyDuration;
        transientDuration = transientDuration == null ? Duration.ofDays(90) : transientDuration;
        batchSize = batchSize <= 0 ? 500 : batchSize;
        dailyScenarioIds = dailyScenarioIds == null || dailyScenarioIds.isEmpty()
                ? DEFAULT_DAILY_SCENARIOS
                : Set.copyOf(dailyScenarioIds);
        // 03:30 daily: outside any plausible usage peak, and staggered off the
        // hour so it does not collide with every other cron on the host.
        cron = cron == null || cron.isBlank() ? "0 30 3 * * *" : cron;

        if (dailyDuration.isNegative() || dailyDuration.isZero()) {
            throw new IllegalArgumentException(
                    "destiny.retention.daily-duration must be positive; a zero or negative "
                            + "lifetime would delete a reading before the user could read it.");
        }
        if (transientDuration.isNegative() || transientDuration.isZero()) {
            throw new IllegalArgumentException(
                    "destiny.retention.transient-duration must be positive.");
        }
    }

    /** Defaults, for tests and for a context that binds nothing. */
    public static RetentionProperties defaults() {
        return new RetentionProperties(false, null, null, 0, null, null);
    }

    /** Same durations, cleanup switched on — the shape a real deployment uses. */
    public RetentionProperties withCleanupEnabled() {
        return new RetentionProperties(true, dailyDuration, transientDuration, batchSize,
                dailyScenarioIds, cron);
    }

    /** Same policy with a different batch size, for tests that need a small batch. */
    public RetentionProperties withBatchSize(int newBatchSize) {
        return new RetentionProperties(enabled, dailyDuration, transientDuration, newBatchSize,
                dailyScenarioIds, cron);
    }
}
