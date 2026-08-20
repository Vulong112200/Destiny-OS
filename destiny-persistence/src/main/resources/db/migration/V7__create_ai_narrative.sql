-- V7: AI narrative (Phase 12, ADR D8, AI_NARRATIVE_SPEC.md).
--
-- One row per calculation - regenerating a narrative overwrites the
-- previous one rather than accumulating history, since a narrative is a
-- rendering of the calculation's hard data, not itself a new fact. The hard
-- data behind it (Evidence/Signal/FusionResult, V4-V6) is what stays
-- historically immutable; the narrative is free to be recomputed against a
-- newer prompt/model version without that being a correctness problem.
--
-- calculation_id is the primary key directly (not a surrogate id + unique
-- constraint, unlike fusion_results in V6) because this table genuinely has
-- no other natural key: it IS the calculation's narrative, 1:1, with no
-- history of its own to key against.
CREATE TABLE ai_narratives (
    calculation_id            VARCHAR(100) PRIMARY KEY,

    -- Mirrors io.destinyos.ai.NarrativeSource.
    source                    VARCHAR(20) NOT NULL,
    -- Mirrors io.destinyos.ai.FallbackReason. 'NONE' when source = 'AI_GENERATED'.
    fallback_reason           VARCHAR(30) NOT NULL,

    summary                   TEXT NOT NULL,
    -- List<String> fields, JSON-encoded: read as a whole for display, never
    -- queried piecemeal - the same criterion fusion_results.dimensions_json
    -- (V6) already uses for the same reason.
    key_signals_json          TEXT,
    conflicts_json            TEXT,
    cautions_json             TEXT,
    reflection_questions_json TEXT,

    -- Null when source = 'FALLBACK': no real provider was involved.
    provider_name             VARCHAR(60),
    model                     VARCHAR(100),

    generated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_narratives_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id),
    CONSTRAINT chk_ai_narratives_source
        CHECK (source IN ('AI_GENERATED', 'FALLBACK')),
    CONSTRAINT chk_ai_narratives_fallback_reason
        CHECK (fallback_reason IN (
            'NONE', 'AI_DISABLED', 'NO_API_KEY', 'TIMEOUT', 'RATE_LIMITED',
            'SERVER_ERROR', 'PROVIDER_UNAVAILABLE', 'MALFORMED_JSON', 'EMPTY_RESPONSE'
        ))
);
