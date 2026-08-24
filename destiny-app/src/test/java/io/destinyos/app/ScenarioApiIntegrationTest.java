package io.destinyos.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.api.dto.BaziRequest;
import io.destinyos.api.dto.EvidenceDto;
import io.destinyos.api.dto.FengShuiRequest;
import io.destinyos.api.dto.RetentionDto;
import io.destinyos.api.dto.NumerologyRequest;
import io.destinyos.api.dto.ScenarioRunRequest;
import io.destinyos.api.dto.ScenarioRunResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * The one true end-to-end test: real HTTP, real {@code TarotEngine} and
 * {@code NumerologyEngine} (wired by {@code EngineWiringConfig}), real
 * {@code FusionEngine}, and a real database round-trip through
 * {@code CalculationRecorder} — everything {@code destiny-api}'s unit and
 * slice tests deliberately mock or stub out to stay within their own module
 * boundary.
 *
 * <p>Uses {@code ScenarioType.BUSINESS} rather than {@code DAILY_ACTION}:
 * per {@code ScenarioRegistry}, BUSINESS is the only defined-policy scenario
 * whose applicable-engines list includes both {@code TAROT} and
 * {@code NUMEROLOGY_PYTHAGOREAN} — the two engines this MVP actually has.
 *
 * <p>Both engines now emit real signals (research items R11/R8's Vietnamese
 * interpretive content, authored 2026-08-19) — this is the first test in the
 * project to exercise a genuine, non-{@code INSUFFICIENT_EVIDENCE} Fusion
 * result end to end. For this fixed seed, the three drawn Tarot cards carry
 * disagreeing polarities, so the real, honest outcome is
 * {@code MAJOR_CONFLICT} — Rule E's "conflict is a valid result" made
 * concrete, not a consensus forced where none exists.
 */
@SpringBootTest(classes = DestinyOsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ScenarioApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @DisplayName("A BUSINESS scenario run with both engines persists and returns a full explainability record")
    void runsBusinessScenarioAndPersistsIt() {
        var request = new ScenarioRunRequest(
                new NumerologyRequest("Nguyễn Văn A", LocalDate.of(1990, 5, 15)),
                new io.destinyos.api.dto.TarotRequest("PAST_PRESENT_FUTURE", 42L, "Tôi có nên mở rộng kinh doanh không?"),
                null, null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.policyDefined()).isTrue();
        assertThat(body.calculationId()).isNotBlank();
        assertThat(body.resultHash()).isNotBlank();

        assertThat(body.engines()).hasSize(2);
        assertThat(body.engines()).allSatisfy(engine ->
                assertThat(engine.status().labelVi()).isEqualTo("Thành công"));

        // Both engines compute honestly but neither has interpretive content
        // yet (R8/R11) - evidence is real, signals are not fabricated to fill
        // the gap.
        assertThat(body.evidence()).isNotEmpty();
        // Both engines now emit real signals (R11/R8 content authored) - 3
        // Tarot cards x 5 dimension fields + 5 Numerology numbers, for this
        // fixed seed/name/birthdate.
        assertThat(body.signals()).hasSize(20);

        assertThat(body.fusion()).isNotNull();
        // A genuine, honest MAJOR_CONFLICT: the three Tarot cards drawn for
        // this seed carry different polarities (SUPPORT, CAUTION, NEGATIVE),
        // so every dimension they touch shows real internal disagreement -
        // Rule E's "conflict is a valid result" is not aspirational, this is
        // the first real Fusion output the project has ever produced from
        // authored engine content, and it correctly does not force a false
        // consensus.
        assertThat(body.fusion().overallOutcome().technical()).isEqualTo("MAJOR_CONFLICT");
        assertThat(body.fusion().dimensions()).isNotEmpty();
        assertThat(body.fusion().dimensions()).allSatisfy(dimension ->
                assertThat(dimension.state().technical()).isEqualTo("CONFLICT"));

        ResponseEntity<ScenarioRunResponse> replay = rest.getForEntity(
                "/api/v1/calculations/" + body.calculationId(), ScenarioRunResponse.class);

        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().resultHash()).isEqualTo(body.resultHash());
        assertThat(replay.getBody().scenarioId()).isEqualTo("BUSINESS");

        // Phase 12 (AI Narrative, ADR D8): the test profile leaves
        // destiny.ai.enabled at its real default (false) rather than
        // stubbing it out - this is deliberately exercising the actual
        // "AI absent" path through the real Spring context, not a mock.
        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> narrative = rest.postForEntity(
                "/api/v1/calculations/" + body.calculationId() + "/narrative", null,
                io.destinyos.api.dto.NarrativeResponseDto.class);

        assertThat(narrative.getStatusCode()).isEqualTo(HttpStatus.OK);
        var narrativeBody = narrative.getBody();
        assertThat(narrativeBody).isNotNull();
        assertThat(narrativeBody.source().technical()).isEqualTo("FALLBACK");
        assertThat(narrativeBody.fallbackReason().technical()).isEqualTo("AI_DISABLED");
        assertThat(narrativeBody.summary()).isNotBlank();
        // The fallback digest is built from this run's own real conflict -
        // never fabricated, and traceable straight back to Fusion's output.
        assertThat(narrativeBody.conflicts()).isNotEmpty();

        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> replayedNarrative = rest.getForEntity(
                "/api/v1/calculations/" + body.calculationId() + "/narrative",
                io.destinyos.api.dto.NarrativeResponseDto.class);

        assertThat(replayedNarrative.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replayedNarrative.getBody()).isNotNull();
        assertThat(replayedNarrative.getBody().summary()).isEqualTo(narrativeBody.summary());
    }

    @Test
    @DisplayName("Requesting a narrative for an unknown calculation is a 404, and reading one before it's generated is too")
    void narrativeEndpointsAre404ForUnknownOrUngeneratedCalculations() {
        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> generate = rest.postForEntity(
                "/api/v1/calculations/does-not-exist/narrative", null,
                io.destinyos.api.dto.NarrativeResponseDto.class);
        assertThat(generate.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<io.destinyos.api.dto.NarrativeResponseDto> readBeforeGenerating = rest.getForEntity(
                "/api/v1/calculations/does-not-exist/narrative", io.destinyos.api.dto.NarrativeResponseDto.class);
        assertThat(readBeforeGenerating.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("A scenario with no defined applicability policy runs zero engines rather than guessing one")
    void undefinedPolicyScenarioRunsNothing() {
        // COMPATIBILITY, not CAREER: CAREER gained a real policy on
        // 2026-08-23 (docs/DECISION_LOG.md). COMPATIBILITY stays undefined -
        // its strongest evidence (Bát Tự hợp hôn, Tử Vi xem tuổi, Chiêm tinh
        // synastry) is dual-chart, which this system cannot represent yet.
        var request = new ScenarioRunRequest(null, null, null, null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/compatibility", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.policyDefined()).isFalse();
        assertThat(body.fusion()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/methodologies lists a research-blocked methodology honestly, not as a 404")
    void methodologyRegistryListsBlockedMethodologies() {
        ResponseEntity<io.destinyos.api.dto.MethodologyDto[]> response = rest.getForEntity(
                "/api/v1/methodologies", io.destinyos.api.dto.MethodologyDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).anySatisfy(m -> {
            assertThat(m.methodologyId()).isEqualTo("BAZI");
            assertThat(m.calculable()).isFalse();
            assertThat(m.status().labelVi()).isEqualTo("Cần xác minh thuật toán");
        });
    }

    @Test
    @DisplayName("A BUSINESS run with Bát Tự returns a real Tứ Trụ chart as evidence and no signals")
    void baziContributesChartEvidenceButNoSignals() {
        // The golden vector from BaziEngineGoldenTest, driven through real HTTP
        // this time: 5 February 1984 is the day after Lập Xuân, so the chart is
        // Giáp Tý / Bính Dần / Kỷ Tỵ. Asserting it here proves the whole path -
        // request DTO, region/precision translation in BaziTaskFactory, the
        // engine, evidence mapping, and the database round-trip - agrees with
        // the published table, not just the engine in isolation.
        var request = new ScenarioRunRequest(null, null,
                new BaziRequest(LocalDate.of(1984, 2, 5), LocalTime.of(12, 0), "UNKNOWN", null, null),
                null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.engines())
                .as("BAZI must actually have run, not be reported unavailable")
                .anySatisfy(engine -> {
                    assertThat(engine.engine()).isEqualTo("BAZI");
                    assertThat(engine.status().technical()).isEqualTo("PARTIAL");
                    assertThat(engine.status().labelVi()).isEqualTo("Một phần");
                });
        assertThat(body.unavailableEngines()).doesNotContain("BAZI");

        assertThat(pillar(body.evidence(), "BAZI_PILLAR_YEAR"))
                .containsEntry("stem", "GIAP").containsEntry("branch", "RAT");
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_MONTH"))
                .containsEntry("stem", "BINH").containsEntry("branch", "TIGER");
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_DAY"))
                .containsEntry("stem", "KY").containsEntry("branch", "SNAKE");

        // Thập Thần relative to the Day Master Kỷ: the year stem Giáp is Mộc
        // khắc Thổ with opposite polarity, i.e. Chính Quan.
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_YEAR"))
                .containsEntry("stemTenGod", "CHINH_QUAN");

        // The three blocked sections travel to the client as evidence too, so a
        // UI cannot render a chart that silently lacks a Dụng Thần.
        // Đại Vận left this list on 2026-08-22 when R2 closed - no gender was
        // supplied in this request, so the chart still has no luck cycles, but
        // for a different reason (missing input, not unresolved research).
        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("BAZI_BLOCKED_DUNG_THAN", "BAZI_BLOCKED_NHAT_CHU_CUONG_DO")
                .doesNotContain("BAZI_BLOCKED_DAI_VAN");

        // And Bát Tự casts no vote: every signal in this run came from
        // somewhere else (here, nowhere - no other engine was supplied).
        assertThat(body.signals()).noneMatch(signal -> signal.engine().equals("BAZI"));
    }

    @Test
    @DisplayName("Bát Tự without a birth time returns two pillars, not four, and says so")
    void baziWithoutBirthTimeDegradesHonestly() {
        // Master Spec section 2: UNKNOWN is never treated as EXACT. The client
        // omitted the time, so there is no Day Master and therefore no Thập
        // Thần anywhere - not a Thập Thần computed against something else.
        var request = new ScenarioRunRequest(null, null,
                new BaziRequest(LocalDate.of(1990, 3, 15), null, null, null, null), null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("BAZI_PILLAR_YEAR", "BAZI_PILLAR_MONTH")
                .doesNotContain("BAZI_PILLAR_DAY", "BAZI_PILLAR_HOUR");
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_YEAR"))
                .doesNotContainKey("stemTenGod");
        assertThat(pillar(body.evidence(), "BAZI_BOUNDARY"))
                .containsEntry("hasHourPrecision", false);
    }

    @Test
    @DisplayName("Bát Tự with a gender returns the Đại Vận sequence through real HTTP (R2)")
    void baziLuckCyclesReachTheClient() {
        // The published backward vector from LuckCycleTest, driven through the
        // whole stack this time: btime.com gives 1990-01-01 11:10 male as
        // 己巳 丙子 丙寅, counting back 25 days to Đại Tuyết for a start age of
        // 8 years 4 months. The source states Beijing time; Vietnam is UTC+7,
        // so the same wall-clock reading here is one hour earlier in absolute
        // terms - which moves the distance by an hour, not by a day, and the
        // start age by five days out of eight years.
        var request = new ScenarioRunRequest(null, null,
                new BaziRequest(LocalDate.of(1990, 1, 1), LocalTime.of(10, 10), "UNKNOWN",
                        null, "MALE"),
                null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        // The published pillars, as a check that this is the same chart the
        // source was describing.
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_YEAR"))
                .containsEntry("stem", "KY").containsEntry("branch", "SNAKE");
        assertThat(pillar(body.evidence(), "BAZI_PILLAR_MONTH"))
                .containsEntry("stem", "BINH").containsEntry("branch", "RAT");

        var luck = pillar(body.evidence(), "BAZI_LUCK_CYCLES");
        assertThat(luck)
                .as("Kỷ is a yin stem and the subject is male, so the cycle runs backward")
                .containsEntry("direction", "NGHICH")
                .containsEntry("boundaryTerm", "DAI_TUYET")
                .containsEntry("startAgeYears", 8);

        @SuppressWarnings("unchecked")
        var periods = (java.util.List<java.util.Map<String, Object>>) luck.get("pillars");
        assertThat(periods).hasSize(8);
        assertThat(periods.get(0)).containsEntry("ordinal", 1);

        // Still no vote. A luck cycle is chart data; calling any period
        // fortunate needs R1 and R3, which are still open.
        assertThat(body.signals()).noneMatch(signal -> signal.engine().equals("BAZI"));
    }

    @Test
    @DisplayName("Bát Tự without a gender keeps the whole chart and omits only Đại Vận")
    void baziWithoutGenderOmitsOnlyLuckCycles() {
        var request = new ScenarioRunRequest(null, null,
                new BaziRequest(LocalDate.of(1990, 1, 1), LocalTime.of(10, 10), "UNKNOWN",
                        null, null),
                null, null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        // The distinction that matters: the chart is complete, and exactly one
        // section is missing. Declining the whole calculation over a field only
        // one section needs would withhold a chart the engine built correctly.
        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("BAZI_PILLAR_YEAR", "BAZI_PILLAR_MONTH", "BAZI_PILLAR_DAY",
                        "BAZI_PILLAR_HOUR")
                .doesNotContain("BAZI_LUCK_CYCLES");
    }

    @Test
    @DisplayName("GET /api/v1/labels gives the frontend every Vietnamese label it needs for a chart")
    void labelEndpointCoversCanChiAndThapThan() {
        @SuppressWarnings("unchecked")
        ResponseEntity<java.util.Map<String, java.util.Map<String, String>>> response =
                (ResponseEntity<java.util.Map<String, java.util.Map<String, String>>>)
                        (ResponseEntity<?>) rest.getForEntity("/api/v1/labels", java.util.Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var labels = response.getBody();
        assertThat(labels).isNotNull();
        assertThat(labels.get("HeavenlyStem")).containsEntry("GIAP", "Giáp");
        assertThat(labels.get("EarthlyBranch"))
                .containsEntry("RAT", "Tý")
                .containsEntry("SNAKE", "Tỵ");
        assertThat(labels.get("TenGod")).containsEntry("CHINH_QUAN", "Chính Quan");
        assertThat(labels.get("FiveElement")).containsEntry("WOOD", "Mộc");
    }

    @Test
    @DisplayName("The registry lists both halves of Phase 8 with their different statuses")
    void registryListsBothBaziMethodologies() {
        ResponseEntity<io.destinyos.api.dto.MethodologyDto[]> response = rest.getForEntity(
                "/api/v1/methodologies", io.destinyos.api.dto.MethodologyDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody()).anySatisfy(m -> {
            assertThat(m.methodologyId()).isEqualTo("BAZI_TUBINH_CHART");
            assertThat(m.calculable()).isTrue();
            assertThat(m.status().labelVi()).isEqualTo("Thiếu nội dung diễn giải");
            assertThat(m.researchIds()).containsExactlyInAnyOrder("R18", "R19");
        });
        assertThat(response.getBody()).anySatisfy(m -> {
            assertThat(m.methodologyId()).isEqualTo("BAZI");
            assertThat(m.calculable()).isFalse();
            // R2 moved to the chart half on 2026-08-22 when Đại Vận was
            // verified and implemented. What remains here is what genuinely
            // still needs a school chosen: the Dụng Thần and Day Master
            // strength.
            // R20-R22 joined on 2026-08-23 from the Master Spec §13 audit:
            // combinations/clashes, Liu Nian/Yue/Ri and Shen Sha were named
            // in the specification with no research id, so nothing could
            // report them as missing.
            assertThat(m.researchIds())
                    .containsExactlyInAnyOrder("R1", "R3", "R20", "R21", "R22");
        });
    }

    private static java.util.Map<String, Object> pillar(List<EvidenceDto> evidence, String ruleId) {
        return evidence.stream()
                .filter(e -> e.ruleId().equals(ruleId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No evidence with ruleId " + ruleId))
                .fact();
    }

    @Test
    @DisplayName("Every result states how long it will be kept, and the user can keep it for good")
    void resultCarriesRetentionAndCanBeSaved() {
        // CLAUDE.md section 7. Before retention existed every calculation was
        // kept forever; now a run is EPHEMERAL with a real expiry, and the whole
        // round trip - run, read back, save, read back again - has to agree
        // about it, which is why this exercises all four steps rather than just
        // asserting the field is present.
        var request = new ScenarioRunRequest(
                new NumerologyRequest("Nguyễn Văn B", LocalDate.of(1988, 7, 7)), null, null, null, null, null);

        ResponseEntity<ScenarioRunResponse> run = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);
        assertThat(run.getStatusCode()).isEqualTo(HttpStatus.OK);
        String calculationId = run.getBody().calculationId();

        assertThat(run.getBody().retention()).isNotNull().satisfies(retention -> {
            assertThat(retention.retentionClass().technical()).isEqualTo("EPHEMERAL");
            assertThat(retention.retentionClass().labelVi()).contains("tự động xóa");
            assertThat(retention.expiresAt()).isNotNull();
            assertThat(retention.canBeSaved()).isTrue();
        });

        // Reading the calculation back must report the same retention state the
        // run reported - the read and write paths share one mapper precisely so
        // they cannot drift.
        ResponseEntity<ScenarioRunResponse> readBack = rest.getForEntity(
                "/api/v1/calculations/" + calculationId, ScenarioRunResponse.class);
        assertThat(readBack.getBody().retention().expiresAt())
                .isEqualTo(run.getBody().retention().expiresAt());

        ResponseEntity<RetentionDto> saved = rest.postForEntity(
                "/api/v1/calculations/" + calculationId + "/save", null, RetentionDto.class);
        assertThat(saved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(saved.getBody().retentionClass().technical()).isEqualTo("USER_SAVED");
        assertThat(saved.getBody().expiresAt())
                .as("saving clears the expiry rather than deferring it")
                .isNull();
        assertThat(saved.getBody().canBeSaved()).isFalse();

        ResponseEntity<ScenarioRunResponse> afterSave = rest.getForEntity(
                "/api/v1/calculations/" + calculationId, ScenarioRunResponse.class);
        assertThat(afterSave.getBody().retention().retentionClass().technical())
                .isEqualTo("USER_SAVED");
        assertThat(afterSave.getBody().retention().expiresAt()).isNull();
    }

    @Test
    @DisplayName("A daily reading is kept for less time than a business scenario")
    void dailyReadingsExpireSoonerThanBusinessRuns() {
        // DATA_MODEL_AND_RETENTION.md section 8 gives daily readings a shorter
        // life than other transient runs. Asserted through real HTTP because
        // the mapping from scenario id to lifetime crosses three layers.
        var tarot = new io.destinyos.api.dto.TarotRequest("PAST_PRESENT_FUTURE", 7L, null);

        var daily = rest.postForEntity("/api/v1/scenarios/daily_action",
                new ScenarioRunRequest(null, tarot, null, null, null, null), ScenarioRunResponse.class);
        var business = rest.postForEntity("/api/v1/scenarios/business",
                new ScenarioRunRequest(null, tarot, null, null, null, null), ScenarioRunResponse.class);

        assertThat(daily.getBody().retention().expiresAt())
                .isBefore(business.getBody().retention().expiresAt());
    }

    @Test
    @DisplayName("Bát Trạch with a facing direction produces a real signal through the full stack")
    void fengShuiFacingDirectionYieldsRealSignal() {
        // Phase 10's payoff: the first Phong Thủy signal to reach Fusion. A male
        // born 1990 is cung Khảm (published fact), and Khảm's Sinh Khí is Đông
        // Nam, so a house facing Đông Nam is thượng cát - SUPPORT at STRONG,
        // read off the tradition rather than assigned by this project.
        var request = new ScenarioRunRequest(null, null, null,
                new FengShuiRequest(LocalDate.of(1990, 8, 20), null, "MALE", "UNKNOWN",
                        null, "SOUTHEAST"), null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.engines()).anySatisfy(engine -> {
            assertThat(engine.engine()).isEqualTo("FENGSHUI_KUA");
            assertThat(engine.status().technical()).isEqualTo("SUCCESS");
        });
        assertThat(body.unavailableEngines()).doesNotContain("FENGSHUI_KUA");

        assertThat(pillar(body.evidence(), "FENGSHUI_KUA_NUMBER"))
                .containsEntry("trigram", "KHAM")
                .containsEntry("kuaNumber", 1)
                .containsEntry("group", "EAST")
                .containsEntry("boundaryConventionsAgree", true);
        assertThat(pillar(body.evidence(), "FENGSHUI_FACING_ASSESSMENT"))
                .containsEntry("facingDirection", "SOUTHEAST")
                .containsEntry("relation", "SINH_KHI")
                .containsEntry("auspicious", true);

        assertThat(body.signals())
                .filteredOn(signal -> signal.engine().equals("FENGSHUI_KUA"))
                .isNotEmpty()
                .allSatisfy(signal -> {
                    assertThat(signal.polarity().technical()).isEqualTo("SUPPORT");
                    assertThat(signal.polarity().labelVi()).isEqualTo("Thuận lợi");
                    assertThat(signal.strength().technical()).isEqualTo("STRONG");
                });

        // And it actually reached Fusion, which is the whole point of a signal.
        assertThat(body.fusion()).isNotNull();
        assertThat(body.fusion().supportingSources()).contains("FENGSHUI_KUA");
    }

    @Test
    @DisplayName("Bát Trạch without a facing direction returns the eight directions and no signal")
    void fengShuiWithoutDirectionEmitsNoSignal() {
        // Bát Trạch judges a person against a direction. With no direction there
        // is a profile but nothing to judge, and inventing a polarity for the
        // profile alone would be fabrication.
        var request = new ScenarioRunRequest(null, null, null,
                new FengShuiRequest(LocalDate.of(1990, 8, 20), null, "MALE", null, null, null), null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.signals()).noneMatch(s -> s.engine().equals("FENGSHUI_KUA"));

        var directions = pillar(body.evidence(), "FENGSHUI_BAT_TRACH_DIRECTIONS");
        assertThat(directions).hasSize(8);
        // Khảm is Đông tứ, so its four cát directions are the East group's.
        assertThat(directions)
                .containsEntry("SOUTHEAST", "SINH_KHI")
                .containsEntry("EAST", "THIEN_Y")
                .containsEntry("SOUTH", "DIEN_NIEN")
                .containsEntry("NORTH", "PHUC_VI")
                .containsEntry("SOUTHWEST", "TUYET_MENH");
    }

    @Test
    @DisplayName("A missing gender means Bát Trạch does not run at all, rather than guessing one")
    void fengShuiWithoutGenderDoesNotRun() {
        // The male and female Kua formulas differ and are not symmetric, so a
        // default would hand half of users someone else's Kua number - a
        // confident wrong answer rather than a degraded one.
        var request = new ScenarioRunRequest(null, null, null,
                new FengShuiRequest(LocalDate.of(1990, 8, 20), null, null, null, null,
                        "SOUTHEAST"), null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.engines()).noneMatch(e -> e.engine().equals("FENGSHUI_KUA"));
        assertThat(body.unavailableEngines()).contains("FENGSHUI_KUA");
        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .doesNotContain("FENGSHUI_KUA_NUMBER");
    }

    @Test
    @DisplayName("A birth between Tết and Lập Xuân reports both Kua numbers and emits no signal")
    void fengShuiYearBoundaryDisagreementIsSurfaced() {
        // R7's one remaining open item, end to end. Tết 1984 fell on 2 February
        // and Lập Xuân on the 4th: for a male born on the 3rd the Lập Xuân
        // convention gives Cấn and the Tết convention Đoài. Neither is presented
        // as the answer.
        var request = new ScenarioRunRequest(null, null, null,
                new FengShuiRequest(LocalDate.of(1984, 2, 3), null, "MALE", "UNKNOWN",
                        null, "SOUTHEAST"), null, null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(pillar(body.evidence(), "FENGSHUI_KUA_NUMBER"))
                .containsEntry("boundaryConventionsAgree", false)
                .containsEntry("trigram", "CAN")
                .containsEntry("trigramByTet", "DOAI");
        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .as("one trigram's directions would present the Lập Xuân answer as the answer")
                .doesNotContain("FENGSHUI_BAT_TRACH_DIRECTIONS");
        assertThat(body.signals()).noneMatch(s -> s.engine().equals("FENGSHUI_KUA"));
    }

    @Test
    @DisplayName("A BUSINESS run with Western astrology returns real chart angles as evidence and no signal")
    void astrologyContributesChartEvidenceButNoSignals() {
        // 16 June 1994, 18:00 UT at 0N/0E is Meeus's own worked GMST example
        // (Astronomical Algorithms ch. 12) - the same fixture ChartAnglesTest
        // and SiderealTimeTest already verify numerically, driven through the
        // whole stack this time: request DTO, timezone conversion in
        // AstrologyTaskFactory, the engine, and the database round-trip.
        // The request's local time is Vietnam civil time (Asia/Ho_Chi_Minh,
        // UTC+7), so 01:00 local on 1994-06-17 is 18:00 UT on 1994-06-16.
        var request = new ScenarioRunRequest(null, null, null, null,
                new io.destinyos.api.dto.AstrologyRequest(
                        LocalDate.of(1994, 6, 17), LocalTime.of(1, 0), 0.0, 0.0), null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.engines())
                .as("WESTERN_ASTROLOGY must actually have run, not be reported unavailable")
                .anySatisfy(engine -> {
                    assertThat(engine.engine()).isEqualTo("WESTERN_ASTROLOGY");
                    assertThat(engine.status().technical()).isEqualTo("PARTIAL");
                });
        assertThat(body.unavailableEngines()).doesNotContain("WESTERN_ASTROLOGY");

        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("ASTROLOGY_SUN", "ASTROLOGY_MIDHEAVEN", "ASTROLOGY_ASCENDANT",
                        "ASTROLOGY_WHOLE_SIGN_HOUSES", "ASTROLOGY_FRAME");

        // The two registered blocked sections travel to the client too, so a
        // UI cannot render a chart that silently lacks the other planets.
        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("ASTROLOGY_BLOCKED_PLANETS_BEYOND_SUN", "ASTROLOGY_BLOCKED_ASPECTS");

        // Chart evidence only, same as Bát Tự's chart half - no vote yet.
        assertThat(body.signals()).noneMatch(signal -> signal.engine().equals("WESTERN_ASTROLOGY"));
    }

    @Test
    @DisplayName("Western astrology without a birth time or location does not run at all, rather than guessing")
    void astrologyWithoutRequiredFieldsDoesNotRun() {
        // The Ascendant moves roughly 1 degree every 4 minutes; guessing a
        // time or place would produce a confidently wrong chart, not a
        // degraded one, so the task factory declines instead.
        var request = new ScenarioRunRequest(null, null, null, null,
                new io.destinyos.api.dto.AstrologyRequest(
                        LocalDate.of(1994, 6, 17), null, 0.0, 0.0), null);

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/business", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.engines()).noneMatch(e -> e.engine().equals("WESTERN_ASTROLOGY"));
        assertThat(body.unavailableEngines()).contains("WESTERN_ASTROLOGY");
    }

    @Test
    @DisplayName("A GENERAL_DECISION run with I Ching returns a real hexagram as evidence and no signal")
    void ichingContributesHexagramEvidenceButNoSignals() {
        var request = new ScenarioRunRequest(null, null, null, null, null,
                new io.destinyos.api.dto.IChingRequest("THREE_COINS", 42L, null, null));

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/general_decision", request, ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();

        assertThat(body.engines())
                .as("ICHING must actually have run, not be reported unavailable")
                .anySatisfy(engine -> {
                    assertThat(engine.engine()).isEqualTo("ICHING");
                    assertThat(engine.status().technical()).isEqualTo("PARTIAL");
                });
        assertThat(body.unavailableEngines()).doesNotContain("ICHING");

        assertThat(body.evidence()).extracting(EvidenceDto::ruleId)
                .contains("ICHING_CAST", "ICHING_ORIGINAL_HEXAGRAM", "ICHING_MOVING_LINES",
                        "ICHING_DRAWN_LINES", "ICHING_BLOCKED_LINE_JUDGMENT_TEXT");

        assertThat(pillar(body.evidence(), "ICHING_CAST"))
                .containsEntry("method", "THREE_COINS")
                .containsEntry("seed", 42);

        // Chart evidence only, same as Bát Tự's chart half - no vote yet.
        assertThat(body.signals()).noneMatch(signal -> signal.engine().equals("ICHING"));
    }

    @Test
    @DisplayName("I Ching Mai Hoa Number method without both numbers does not run at all, rather than guessing")
    void ichingMaiHoaWithoutBothNumbersDoesNotRun() {
        var request = new ScenarioRunRequest(null, null, null, null, null,
                new io.destinyos.api.dto.IChingRequest("MAI_HOA_NUMBER", null, 3, null));

        ResponseEntity<ScenarioRunResponse> response = rest.postForEntity(
                "/api/v1/scenarios/general_decision", request, ScenarioRunResponse.class);

        ScenarioRunResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.engines()).noneMatch(e -> e.engine().equals("ICHING"));
        assertThat(body.unavailableEngines()).contains("ICHING");
    }

    @Test
    @DisplayName("An unknown calculation id is a 404, never a fabricated result")
    void unknownCalculationIdIs404() {
        ResponseEntity<ScenarioRunResponse> response = rest.getForEntity(
                "/api/v1/calculations/does-not-exist", ScenarioRunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
