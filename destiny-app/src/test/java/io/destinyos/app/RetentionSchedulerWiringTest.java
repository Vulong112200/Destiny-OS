package io.destinyos.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.app.wiring.RetentionScheduler;
import io.destinyos.persistence.retention.RetentionProperties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Whether the cleanup job exists, and whether the policy really binds.
 *
 * <p>Two separate contexts, because the property under test decides bean
 * existence rather than bean behaviour. The default-off case is the one that
 * matters most: a deployment that never asked for automatic deletion must not
 * end up with a live scheduled task that deletes rows, and the only way to be
 * sure of that is to assert the bean is absent — a test of the method's
 * internals would still pass with a live task in the container.
 */
class RetentionSchedulerWiringTest {

    @Nested
    @DisplayName("Default configuration")
    @SpringBootTest(classes = DestinyOsApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("test")
    class Disabled {

        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("The cleanup job does not exist unless it is switched on")
        void schedulerBeanIsAbsentByDefault() {
            assertThat(context.getBeansOfType(RetentionScheduler.class))
                    .as("A deletion job must be opted into, not inherited")
                    .isEmpty();
        }

        @Test
        @DisplayName("The policy still binds, so results are classified even with cleanup off")
        void policyBindsEvenWhenCleanupIsOff() {
            // Classification and deletion are separate concerns: rows must carry
            // an honest expiry from the day this ships, so that switching the job
            // on later does not have to backfill anything.
            var properties = context.getBean(RetentionProperties.class);

            assertThat(properties.enabled()).isFalse();
            assertThat(properties.dailyDuration()).isEqualTo(Duration.ofDays(30));
            assertThat(properties.transientDuration()).isEqualTo(Duration.ofDays(90));
            assertThat(properties.batchSize()).isPositive();
            assertThat(properties.dailyScenarioIds()).contains("DAILY_ACTION");
        }
    }

    @Nested
    @DisplayName("Cleanup explicitly enabled")
    @SpringBootTest(classes = DestinyOsApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "destiny.retention.enabled=true",
            // A cron that will not fire during the test: this asserts wiring,
            // not that a deletion happens on a timer.
            "destiny.retention.cron=0 0 0 29 2 ?",
            "destiny.retention.daily-duration=7d",
            "destiny.retention.transient-duration=30d",
            "destiny.retention.batch-size=50"
    })
    class Enabled {

        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("The cleanup job exists once switched on")
        void schedulerBeanIsPresentWhenEnabled() {
            assertThat(context.getBeansOfType(RetentionScheduler.class)).hasSize(1);
        }

        @Test
        @DisplayName("Operator-supplied durations override the defaults")
        void configuredDurationsWin() {
            // DATA_MODEL_AND_RETENTION.md §8 requires the policy to be
            // configurable, and 7 days is the spec's own lower bound for daily
            // readings - so an operator must actually be able to reach it.
            var properties = context.getBean(RetentionProperties.class);

            assertThat(properties.dailyDuration()).isEqualTo(Duration.ofDays(7));
            assertThat(properties.transientDuration()).isEqualTo(Duration.ofDays(30));
            assertThat(properties.batchSize()).isEqualTo(50);
        }
    }
}
