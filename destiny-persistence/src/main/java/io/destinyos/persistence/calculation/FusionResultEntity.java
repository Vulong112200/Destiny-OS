package io.destinyos.persistence.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.destinyos.fusion.DimensionAnalysis;
import io.destinyos.fusion.FusionOutcome;
import io.destinyos.fusion.FusionResult;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Persisted form of {@link FusionResult} (V6 migration,
 * FUSION_ENGINE_SPEC.md section 10). The per-dimension breakdown is stored
 * as a JSON snapshot ({@link DimensionAnalysisSnapshot}) rather than fully
 * normalized — it is read as a whole for explainability display, never
 * queried piecemeal (DATA_MODEL_AND_RETENTION.md's stated criterion for
 * when a JSON payload, not more tables, is the right choice).
 */
@Entity
@Table(name = "fusion_results")
public class FusionResultEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<ArrayList<DimensionAnalysisSnapshot>> DIMENSIONS_TYPE =
            new TypeReference<>() { };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calculation_id", nullable = false, length = 100)
    private String calculationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_outcome", nullable = false, length = 40)
    private FusionOutcome overallOutcome;

    @Column(name = "dimensions_json", columnDefinition = "TEXT")
    private String dimensionsJson;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fusion_result_rules", joinColumns = @JoinColumn(name = "fusion_result_id"))
    @Column(name = "rule_code", length = 10)
    @OrderColumn(name = "sequence_no")
    private List<String> rulesApplied = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fusion_result_supporting_sources",
            joinColumns = @JoinColumn(name = "fusion_result_id"))
    @Column(name = "engine", length = 60)
    private Set<String> supportingSources = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fusion_result_caution_sources",
            joinColumns = @JoinColumn(name = "fusion_result_id"))
    @Column(name = "engine", length = 60)
    private Set<String> cautionSources = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FusionResultEntity() {
        // JPA
    }

    public FusionResultEntity(String calculationId, FusionResult result) {
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(result, "result");
        this.overallOutcome = result.overallOutcome();
        this.rulesApplied = new ArrayList<>(result.rulesApplied());
        this.supportingSources = new LinkedHashSet<>(result.supportingSources());
        this.cautionSources = new LinkedHashSet<>(result.cautionSources());
        this.dimensionsJson = serializeDimensions(result.dimensions());
    }

    private static String serializeDimensions(List<DimensionAnalysis> dimensions) {
        try {
            List<DimensionAnalysisSnapshot> snapshots = dimensions.stream()
                    .map(DimensionAnalysisSnapshot::from).toList();
            return MAPPER.writeValueAsString(snapshots);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize dimension analyses", e);
        }
    }

    public List<DimensionAnalysisSnapshot> dimensions() {
        if (dimensionsJson == null || dimensionsJson.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(dimensionsJson, DIMENSIONS_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not deserialize dimensions JSON", e);
        }
    }

    public Long id() {
        return id;
    }

    public String calculationId() {
        return calculationId;
    }

    public FusionOutcome overallOutcome() {
        return overallOutcome;
    }

    public List<String> rulesApplied() {
        return List.copyOf(rulesApplied);
    }

    public Set<String> supportingSources() {
        return Set.copyOf(supportingSources);
    }

    public Set<String> cautionSources() {
        return Set.copyOf(cautionSources);
    }

    public Instant createdAt() {
        return createdAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
