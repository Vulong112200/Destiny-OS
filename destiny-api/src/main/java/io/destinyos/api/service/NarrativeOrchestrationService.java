package io.destinyos.api.service;

import io.destinyos.ai.NarrativeConflictItem;
import io.destinyos.ai.NarrativeInput;
import io.destinyos.ai.NarrativeResponse;
import io.destinyos.ai.NarrativeResult;
import io.destinyos.ai.NarrativeService;
import io.destinyos.ai.NarrativeSignalItem;
import io.destinyos.api.dto.LabeledValue;
import io.destinyos.api.dto.NarrativeResponseDto;
import io.destinyos.core.signal.Dimension;
import io.destinyos.fusion.DimensionState;
import io.destinyos.i18n.VietnameseLabels;
import io.destinyos.persistence.calculation.CalculationRepository;
import io.destinyos.persistence.calculation.ConflictEntity;
import io.destinyos.persistence.calculation.ConflictRepository;
import io.destinyos.persistence.calculation.DimensionAnalysisSnapshot;
import io.destinyos.persistence.calculation.EvidenceEntity;
import io.destinyos.persistence.calculation.EvidenceRepository;
import io.destinyos.persistence.calculation.FusionResultEntity;
import io.destinyos.persistence.calculation.FusionResultRepository;
import io.destinyos.persistence.calculation.SignalEntity;
import io.destinyos.persistence.calculation.SignalRepository;
import io.destinyos.persistence.narrative.NarrativeEntity;
import io.destinyos.persistence.narrative.NarrativeRecorder;
import io.destinyos.scenario.ScenarioDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * Bridges the persisted calculation record to {@code destiny-ai}'s
 * {@link NarrativeService} (Phase 12, ADR D8): builds a {@link NarrativeInput}
 * from what {@code destiny-persistence} (V4-V6) already has, calls the AI
 * narrative stage, records the result (V7), and returns a Vietnamese-labeled
 * DTO. This is the one place allowed to know both the persistence entities
 * and the {@code destiny-ai} contract - neither module depends on the other.
 *
 * <p>Reads {@code SignalEntity}/{@code ConflictEntity}/{@code FusionResultEntity}
 * directly (not through {@link CalculationQueryService}'s already-labeled
 * DTOs) specifically to keep the real {@code Dimension}/{@code Polarity}/
 * {@code Strength} enums {@link io.destinyos.ai.NarrativePruner} needs for its
 * priority rules, rather than round-tripping them through
 * {@code LabeledValue.technical()}.
 *
 * <p>Also reads {@code EvidenceEntity}, which a narrative stage might not
 * obviously need: a {@code Signal} records that a finding was favourable and
 * how strongly, but the engines' authored Vietnamese interpretation lives on
 * the {@code Evidence} the signal cites ({@code fact.meaning}). Without that
 * join the narrative layer only ever saw categories, so every narrative it
 * produced - AI or fallback - was necessarily written about
 * "TAROT: Sự nghiệp - Thuận lợi (Mạnh)" with no way to know which card that
 * was. The join is what lets this stage restate the deterministic engines'
 * own content instead of improvising around a label (CLAUDE.md Rule B).
 */
@Service
public class NarrativeOrchestrationService {

    private final CalculationRepository calculations;
    private final SignalRepository signalRepo;
    private final EvidenceRepository evidenceRepo;
    private final FusionResultRepository fusionResultRepo;
    private final ConflictRepository conflictRepo;
    private final NarrativeService narrativeService;
    private final NarrativeRecorder narrativeRecorder;

    public NarrativeOrchestrationService(CalculationRepository calculations, SignalRepository signalRepo,
            EvidenceRepository evidenceRepo, FusionResultRepository fusionResultRepo,
            ConflictRepository conflictRepo, NarrativeService narrativeService,
            NarrativeRecorder narrativeRecorder) {
        this.calculations = calculations;
        this.signalRepo = signalRepo;
        this.evidenceRepo = evidenceRepo;
        this.fusionResultRepo = fusionResultRepo;
        this.conflictRepo = conflictRepo;
        this.narrativeService = narrativeService;
        this.narrativeRecorder = narrativeRecorder;
    }

    /** Generates (or regenerates) a narrative for an existing calculation and persists it. */
    public Optional<NarrativeResponseDto> generate(String calculationId) {
        return calculations.findById(calculationId).map(calculation -> {
            NarrativeInput input = buildInput(calculationId, calculation.scenarioId(),
                    calculation.question(), calculation.focusLabel());
            NarrativeResult result = narrativeService.generate(input);
            narrativeRecorder.record(calculationId, result);
            return toDto(calculationId, result);
        });
    }

    /** Returns the last-generated narrative for a calculation, if one exists. */
    public Optional<NarrativeResponseDto> find(String calculationId) {
        return narrativeRecorder.find(calculationId).map(entity -> toDto(calculationId, entity));
    }

    private NarrativeInput buildInput(String calculationId, String scenarioId, String question,
                                      String focusLabel) {
        ScenarioDefinition definition = ScenarioDefinitions.byId(scenarioId);
        String scenarioNameVi = definition != null ? definition.displayNameVi() : "Kết quả tính toán";
        Set<Dimension> relevantDimensions = definition != null ? definition.dimensions() : Set.of();

        // Evidence is loaded here purely to recover the authored interpretive
        // text a signal cites but does not carry (see
        // #authoredMeaning/#authoredTitle). Indexed by evidenceId up front
        // rather than queried per signal: a Tarot draw alone produces up to
        // fifteen signals over three evidence rows, and a per-signal lookup
        // would be fifteen round trips to re-read the same three rows.
        Map<String, EvidenceEntity> evidenceById = evidenceRepo.findByCalculationId(calculationId).stream()
                .collect(java.util.stream.Collectors.toMap(EvidenceEntity::evidenceId, Function.identity(),
                        (first, duplicate) -> first, LinkedHashMap::new));

        List<NarrativeSignalItem> signals = signalRepo.findByCalculationId(calculationId).stream()
                .map(signal -> toNarrativeSignalItem(signal, evidenceById))
                .toList();
        List<NarrativeConflictItem> conflicts = conflictRepo.findByCalculationId(calculationId).stream()
                .map(this::toNarrativeConflictItem)
                .toList();
        Map<String, Object> hardDataSummary = fusionResultRepo.findByCalculationId(calculationId)
                .map(this::toHardDataSummary)
                .orElse(Map.of());

        // Uncertainty (CalculationContext.uncertainties()) is not persisted
        // anywhere by CalculationRecorder (V4-V6) - a pre-existing gap this
        // service does not paper over. warnings/limitations are honestly
        // empty rather than fabricated until that gap is closed.
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("calculationId", calculationId);
        if (scenarioId != null) {
            metadata.put("scenarioId", scenarioId);
        }

        return new NarrativeInput(scenarioNameVi, question, focusLabel, relevantDimensions, hardDataSummary,
                signals, conflicts, List.of(), List.of(), metadata);
    }

    private NarrativeSignalItem toNarrativeSignalItem(SignalEntity entity,
                                                      Map<String, EvidenceEntity> evidenceById) {
        Map<String, Object> meaningFact = authoredMeaningFact(entity, evidenceById);
        Map<String, Object> evidenceFact = citedFact(entity, evidenceById);

        return new NarrativeSignalItem(
                entity.engine(),
                entity.dimension(), VietnameseLabels.of(entity.dimension()),
                entity.polarity(), VietnameseLabels.of(entity.polarity()),
                entity.strength(), VietnameseLabels.of(entity.strength()),
                entity.critical(),
                entity.tag(),
                authoredTitle(evidenceFact),
                authoredMeaning(entity.dimension(), meaningFact));
    }

    /**
     * The evidence row a signal cites, or {@code null}.
     *
     * <p>A signal may cite several evidence items; the first one that is
     * actually present wins. In practice every signal-emitting engine today
     * cites exactly one (a Tarot card draw, a numerology number), and picking
     * the first is what "the finding this signal came from" means. A cited id
     * with no row behind it is possible for an older calculation whose
     * evidence has been pruned, and is treated as absence rather than an error:
     * losing the interpretive text must not cost the user the signal itself.
     */
    private Map<String, Object> citedFact(SignalEntity entity, Map<String, EvidenceEntity> evidenceById) {
        for (String evidenceId : entity.evidenceIds()) {
            EvidenceEntity evidence = evidenceById.get(evidenceId);
            if (evidence != null && evidence.fact() != null) {
                return evidence.fact();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> authoredMeaningFact(SignalEntity entity,
                                                    Map<String, EvidenceEntity> evidenceById) {
        Map<String, Object> fact = citedFact(entity, evidenceById);
        if (fact == null) {
            return null;
        }
        Object meaning = fact.get("meaning");
        return meaning instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    /**
     * The engine's authored interpretive text for this signal, or {@code null}
     * when it authored none.
     *
     * <p>Resolved by convention over the evidence's {@code meaning} sub-map
     * rather than by branching on the engine id, because the convention is
     * what the engines actually follow and an engine id list here would go
     * stale the first time a seventh engine authors content:
     *
     * <ol>
     *   <li>a key named after the signal's own {@code Dimension}
     *       ({@code career}, {@code finance}, {@code relationship},
     *       {@code decision}) - {@code TarotEngine} emits one signal per
     *       authored meaning field and names them exactly this way, so a
     *       CAREER signal gets the card's career reading and not its general
     *       one;</li>
     *   <li>otherwise {@code general} - the same engine's catch-all, which is
     *       what its {@code OTHER}-dimension signals were derived from;</li>
     *   <li>otherwise {@code text} - {@code NumerologyEngine}'s single authored
     *       paragraph per number, which has no per-dimension split at all.</li>
     * </ol>
     *
     * <p>Falls through to {@code null} rather than to a nearby field. A signal
     * whose dimension has no authored meaning is a real state (Tarot cards
     * routinely have some fields authored and others not), and substituting a
     * different dimension's text would be presenting content as being about
     * something it is not - a fabrication with a genuine-looking source,
     * which is worse than an honest gap.
     */
    private String authoredMeaning(Dimension dimension, Map<String, Object> meaningFact) {
        if (meaningFact == null) {
            return null;
        }
        String byDimension = text(meaningFact.get(dimension.name().toLowerCase(Locale.ROOT)));
        if (byDimension != null) {
            return byDimension;
        }
        String general = text(meaningFact.get("general"));
        return general != null ? general : text(meaningFact.get("text"));
    }

    /**
     * A short human-facing name for what produced this signal - the Tarot card
     * and how it fell, the numerology number - or {@code null} when the
     * evidence carries nothing identifying.
     *
     * <p>Read off the evidence fact by key, again by convention rather than by
     * engine id. The orientation words are rendered here rather than through
     * {@code VietnameseLabels} because {@code TarotOrientation} lives in
     * {@code destiny-engine-tarot} and {@code destiny-api} is forbidden by
     * {@code ArchitectureRulesTest#controllersStayThin} from importing any
     * engine type - so there is no enum to look up, only the string the engine
     * already wrote into the fact. Two orientation words are the whole of it;
     * if this ever grows into real per-engine display logic it belongs in
     * {@code destiny-i18n} with the engine on its classpath, the way Bát Tự and
     * Bát Trạch labels already are.
     */
    private String authoredTitle(Map<String, Object> fact) {
        if (fact == null) {
            return null;
        }
        String cardName = text(fact.get("cardName"));
        if (cardName != null) {
            String orientation = text(fact.get("orientation"));
            if ("REVERSED".equals(orientation)) {
                return cardName + " (ngược)";
            }
            return "UPRIGHT".equals(orientation) ? cardName + " (xuôi)" : cardName;
        }
        Object value = fact.get("value");
        return value instanceof Number number ? "Số " + number : null;
    }

    /** Non-blank string content, or {@code null} - blank authored text is no authored text. */
    private String text(Object raw) {
        if (!(raw instanceof String string)) {
            return null;
        }
        String trimmed = string.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Engine ids are translated here, not downstream, for the same reason
     * {@code typeLabelVi} already is: {@code destiny-ai} does not depend on
     * {@code destiny-i18n}, so this service is the layer that owns labelling.
     *
     * <p>Without this, the deterministic fallback wrote a Vietnamese reflection
     * question containing raw ids — "Giữa ICHING và TAROT, bạn thấy…" — and a
     * free model given the same ids would echo them just as readily. Both are
     * {@code CLAUDE.md} §9 breaches: a technical name shown to an end user.
     */
    private NarrativeConflictItem toNarrativeConflictItem(ConflictEntity entity) {
        return new NarrativeConflictItem(
                VietnameseLabels.of(entity.type()),
                entity.dimension() == null ? null : VietnameseLabels.of(entity.dimension()),
                entity.involvedEngines().stream().map(VietnameseLabels::engineName).toList(),
                entity.description());
    }

    private Map<String, Object> toHardDataSummary(FusionResultEntity entity) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallOutcome", VietnameseLabels.of(entity.overallOutcome()));

        Map<String, String> dimensionStates = new LinkedHashMap<>();
        for (DimensionAnalysisSnapshot snapshot : entity.dimensions()) {
            Dimension dimension = Dimension.valueOf(snapshot.dimension());
            DimensionState state = DimensionState.valueOf(snapshot.state());
            dimensionStates.put(VietnameseLabels.of(dimension), VietnameseLabels.of(state));
        }
        summary.put("dimensionStates", dimensionStates);
        return summary;
    }

    private NarrativeResponseDto toDto(String calculationId, NarrativeResult result) {
        NarrativeResponse response = result.response();
        return new NarrativeResponseDto(
                calculationId,
                LabeledValue.of(result.source(), VietnameseLabels.of(result.source())),
                LabeledValue.of(result.fallbackReason(), VietnameseLabels.of(result.fallbackReason())),
                response.summary(),
                response.keySignals(),
                response.conflicts(),
                response.cautions(),
                response.reflectionQuestions(),
                result.providerName(),
                result.model(),
                null);
    }

    private NarrativeResponseDto toDto(String calculationId, NarrativeEntity entity) {
        return new NarrativeResponseDto(
                calculationId,
                LabeledValue.of(entity.source(), VietnameseLabels.of(entity.source())),
                LabeledValue.of(entity.fallbackReason(), VietnameseLabels.of(entity.fallbackReason())),
                entity.summary(),
                entity.keySignals(),
                entity.conflicts(),
                entity.cautions(),
                entity.reflectionQuestions(),
                entity.providerName(),
                entity.model(),
                entity.generatedAt() == null ? null : entity.generatedAt().toString());
    }
}
