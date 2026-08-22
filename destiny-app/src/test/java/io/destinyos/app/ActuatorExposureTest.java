package io.destinyos.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

/**
 * What the operational endpoints do and do not expose (Phase 14; Master Spec §28
 * on secrets).
 *
 * <p>Adding Actuator adds HTTP surface, and the two endpoints most useful to a
 * developer — {@code /actuator/env} and {@code /actuator/configprops} — are the
 * two that would render {@code OPENROUTER_API_KEY} and the database password to
 * anyone who can reach the port. Boot's own defaults are conservative today, but
 * "conservative today" is not a guarantee across versions, and the cost of a
 * regression here is a leaked credential rather than a failed test.
 *
 * <p>So the exposure list is pinned in {@code application.yml} and asserted
 * here. This is a test about what is <em>absent</em>, which is the kind that
 * only exists if someone deliberately writes it.
 */
@SpringBootTest(classes = DestinyOsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorExposureTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("Health is reachable, so a load balancer has something to ask")
    void healthIsExposed() {
        var response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Health does not name components, hosts or driver versions")
    void healthHidesDetails() {
        // show-details: never. A health response that enumerates its components
        // tells an unauthenticated caller which database and which optional
        // services exist - useful reconnaissance, no operational benefit.
        var response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getBody())
                .doesNotContain("components")
                .doesNotContain("jdbc")
                .doesNotContain("database");
    }

    @Test
    @DisplayName("Engine metrics are exposed, and carry no user-derived tag")
    void engineMetricsAreExposed() {
        // Run one scenario so the meter exists, then read it back through the
        // same endpoint an operator would use.
        rest.postForEntity("/api/v1/scenarios/business",
                new io.destinyos.api.dto.ScenarioRunRequest(
                        new io.destinyos.api.dto.NumerologyRequest(
                                "Nguyễn Văn C", java.time.LocalDate.of(1991, 4, 4)),
                        null, null, null),
                io.destinyos.api.dto.ScenarioRunResponse.class);

        var response = rest.getForEntity("/actuator/metrics/destiny.engine.executions",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .as("the counter must exist once an engine has run")
                .contains("destiny.engine.executions")
                .contains("engine")
                .contains("status");

        // Tag cardinality: nothing derived from the request may become a tag. A
        // per-calculation or per-name tag would grow time series without bound.
        assertThat(response.getBody())
                .doesNotContain("Nguyễn")
                .doesNotContain("calculationId");
    }

    @Test
    @DisplayName("The concurrency-wait timer exists, so queueing is distinguishable from slowness")
    void concurrencyWaitMetricIsExposed() {
        rest.postForEntity("/api/v1/scenarios/business",
                new io.destinyos.api.dto.ScenarioRunRequest(
                        new io.destinyos.api.dto.NumerologyRequest(
                                "Trần Thị D", java.time.LocalDate.of(1987, 2, 2)),
                        null, null, null),
                io.destinyos.api.dto.ScenarioRunResponse.class);

        var response = rest.getForEntity("/actuator/metrics/destiny.engine.concurrency.wait",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("No endpoint that could render a secret is reachable")
    void secretBearingEndpointsAreNotExposed() {
        // The load-bearing assertion in this file. /env and /configprops render
        // resolved configuration, which includes the OpenRouter API key and the
        // datasource password (Master Spec §28). /heapdump would contain both
        // and more.
        for (String endpoint : new String[] {
                "/actuator/env",
                "/actuator/env/OPENROUTER_API_KEY",
                "/actuator/configprops",
                "/actuator/beans",
                "/actuator/heapdump",
                "/actuator/threaddump",
                "/actuator/loggers"
        }) {
            var response = rest.getForEntity(endpoint, String.class);

            assertThat(response.getStatusCode())
                    .as("%s must not be exposed", endpoint)
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("The actuator root does not advertise an endpoint that is not exposed")
    void actuatorIndexListsOnlyExposedEndpoints() {
        var response = rest.getForEntity("/actuator", String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody())
                    .doesNotContain("\"env\"")
                    .doesNotContain("configprops")
                    .doesNotContain("heapdump");
        }
    }
}
