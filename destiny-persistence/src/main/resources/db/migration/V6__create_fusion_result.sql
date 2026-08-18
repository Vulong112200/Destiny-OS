-- V6: fusion result and conflicts (DATA_MODEL_AND_RETENTION.md section 2,
-- FUSION_ENGINE_SPEC.md section 10 explainability contract).
--
-- A dedicated ScenarioEvaluation table was considered and deliberately not
-- created: DATA_MODEL_AND_RETENTION.md section 2 names it but gives it no
-- fields anywhere, unlike Calculation/EngineResult/Evidence/Signal which
-- all get explicit field blocks. Its apparent role - "the record of one
-- scenario run" - is already covered by `calculations` (V4, which already
-- carries scenario_id) plus this table's fusion_results row. Adding a
-- second, field-less table for the same concept would be schema growth
-- with no specified content behind it.

-- One row per calculation (a calculation is already scoped to one scenario
-- run - see V4). dimensions_json is the per-dimension breakdown
-- (DimensionAnalysis[]) serialized as a single JSON payload: it is read as
-- a whole for display (the "Vì sao có kết quả này?" panel), never queried
-- piecemeal, which is exactly the case DATA_MODEL_AND_RETENTION.md reserves
-- JSON payloads for.
CREATE TABLE fusion_results (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    calculation_id       VARCHAR(100) NOT NULL,

    -- Mirrors io.destinyos.fusion.FusionOutcome (DECISION_LOG C2, the union
    -- of Master Spec section 9 and FUSION_ENGINE_SPEC.md section 7).
    overall_outcome      VARCHAR(40) NOT NULL,

    dimensions_json      TEXT,

    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fusion_results_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id),
    CONSTRAINT uq_fusion_results_calculation
        UNIQUE (calculation_id),
    CONSTRAINT chk_fusion_results_outcome
        CHECK (overall_outcome IN (
            'CONSENSUS_SUPPORT', 'CONSENSUS_CAUTION', 'CONSENSUS_NEGATIVE',
            'SUPPORT_WITH_CAUTION', 'SUPPORT_WITH_CRITICAL_CAUTION',
            'CAUTION_WITH_SUPPORT', 'CAUTION_WITH_CRITICAL_SUPPORT',
            'MIXED', 'MAJOR_CONFLICT', 'METHODOLOGY_CONFLICT',
            'INSUFFICIENT_EVIDENCE', 'NOT_APPLICABLE'
        ))
);

-- rulesApplied (List<String>, order matters - "which rule fired first").
CREATE TABLE fusion_result_rules (
    fusion_result_id     BIGINT NOT NULL,
    sequence_no          INT NOT NULL,
    rule_code            VARCHAR(10) NOT NULL,

    PRIMARY KEY (fusion_result_id, sequence_no),
    CONSTRAINT fk_fusion_result_rules_fusion_result
        FOREIGN KEY (fusion_result_id) REFERENCES fusion_results (id)
);

-- supportingSources / cautionSources (Set<String> engine ids) - kept as
-- simple join tables, matching the style already used for
-- methodology_version_research_refs, rather than folded into the JSON
-- blob: "which calculations had engine X as a supporting source" is a
-- plausible relational query, unlike the dimension breakdown above.
CREATE TABLE fusion_result_supporting_sources (
    fusion_result_id     BIGINT NOT NULL,
    engine               VARCHAR(60) NOT NULL,

    PRIMARY KEY (fusion_result_id, engine),
    CONSTRAINT fk_fusion_result_supp_src_fusion_result
        FOREIGN KEY (fusion_result_id) REFERENCES fusion_results (id)
);

CREATE TABLE fusion_result_caution_sources (
    fusion_result_id     BIGINT NOT NULL,
    engine               VARCHAR(60) NOT NULL,

    PRIMARY KEY (fusion_result_id, engine),
    CONSTRAINT fk_fusion_result_caut_src_fusion_result
        FOREIGN KEY (fusion_result_id) REFERENCES fusion_results (id)
);

-- One row per detected conflict (FUSION_ENGINE_SPEC.md section 8). Never
-- resolved away - a METHODOLOGY_CONFLICT in particular must reach the user
-- as two named positions (Master Spec section 10 Rule F7), which is why
-- this is its own table rather than a summary field on fusion_results.
CREATE TABLE conflicts (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    calculation_id       VARCHAR(100) NOT NULL,

    -- Mirrors io.destinyos.fusion.ConflictType.
    type                 VARCHAR(40) NOT NULL,
    dimension            VARCHAR(30),
    involved_engines_json TEXT,
    description          TEXT NOT NULL,

    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conflicts_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id),
    CONSTRAINT chk_conflicts_type
        CHECK (type IN (
            'DIRECT_CONFLICT', 'SCOPE_CONFLICT', 'METHODOLOGY_CONFLICT',
            'INPUT_SENSITIVITY_CONFLICT', 'TEMPORAL_CONFLICT'
        ))
);

CREATE INDEX idx_conflicts_calculation_id ON conflicts (calculation_id);
