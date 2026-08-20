package io.destinyos.persistence.narrative;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.destinyos.ai.FallbackReason;
import io.destinyos.ai.NarrativeResponse;
import io.destinyos.ai.NarrativeResult;
import io.destinyos.ai.NarrativeSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Persisted form of {@link NarrativeResult} (V7 migration). One row per
 * calculation - see the migration's own comment for why this is an upsert
 * table rather than an append-only history, unlike {@code fusion_results}.
 */
@Entity
@Table(name = "ai_narratives")
public class NarrativeEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<ArrayList<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    @Id
    @Column(name = "calculation_id", length = 100)
    private String calculationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private NarrativeSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "fallback_reason", nullable = false, length = 30)
    private FallbackReason fallbackReason;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "key_signals_json", columnDefinition = "TEXT")
    private String keySignalsJson;

    @Column(name = "conflicts_json", columnDefinition = "TEXT")
    private String conflictsJson;

    @Column(name = "cautions_json", columnDefinition = "TEXT")
    private String cautionsJson;

    @Column(name = "reflection_questions_json", columnDefinition = "TEXT")
    private String reflectionQuestionsJson;

    @Column(name = "provider_name", length = 60)
    private String providerName;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected NarrativeEntity() {
        // JPA
    }

    public NarrativeEntity(String calculationId, NarrativeResult result) {
        this.calculationId = Objects.requireNonNull(calculationId, "calculationId");
        Objects.requireNonNull(result, "result");
        this.source = result.source();
        this.fallbackReason = result.fallbackReason();
        this.providerName = result.providerName();
        this.model = result.model();

        NarrativeResponse response = result.response();
        this.summary = response.summary();
        this.keySignalsJson = serialize(response.keySignals());
        this.conflictsJson = serialize(response.conflicts());
        this.cautionsJson = serialize(response.cautions());
        this.reflectionQuestionsJson = serialize(response.reflectionQuestions());
    }

    /** Applies a newly-generated result onto this existing row (regenerate/upsert). */
    public void update(NarrativeResult result) {
        Objects.requireNonNull(result, "result");
        this.source = result.source();
        this.fallbackReason = result.fallbackReason();
        this.providerName = result.providerName();
        this.model = result.model();

        NarrativeResponse response = result.response();
        this.summary = response.summary();
        this.keySignalsJson = serialize(response.keySignals());
        this.conflictsJson = serialize(response.conflicts());
        this.cautionsJson = serialize(response.cautions());
        this.reflectionQuestionsJson = serialize(response.reflectionQuestions());
    }

    private static String serialize(List<String> values) {
        try {
            return MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize narrative field", e);
        }
    }

    private static List<String> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not deserialize narrative field", e);
        }
    }

    public String calculationId() {
        return calculationId;
    }

    public NarrativeSource source() {
        return source;
    }

    public FallbackReason fallbackReason() {
        return fallbackReason;
    }

    public String summary() {
        return summary;
    }

    public List<String> keySignals() {
        return deserialize(keySignalsJson);
    }

    public List<String> conflicts() {
        return deserialize(conflictsJson);
    }

    public List<String> cautions() {
        return deserialize(cautionsJson);
    }

    public List<String> reflectionQuestions() {
        return deserialize(reflectionQuestionsJson);
    }

    public String providerName() {
        return providerName;
    }

    public String model() {
        return model;
    }

    public Instant generatedAt() {
        return generatedAt;
    }

    /** Reconstructs the {@link NarrativeResponse} half of {@link NarrativeResult} for API display. */
    public NarrativeResponse toResponse() {
        return new NarrativeResponse(summary, keySignals(), conflicts(), cautions(), reflectionQuestions());
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        generatedAt = Instant.now();
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        generatedAt = Instant.now();
    }
}
