-- V5: evidence and signal (DATA_MODEL_AND_RETENTION.md section 5-6,
-- DECISION_LOG C4, FUSION_ENGINE_SPEC.md section 3).
--
-- evidence_id / signal_id are the domain's own String identifiers, matching
-- the calculation table's convention (V4).

CREATE TABLE evidence (
    evidence_id          VARCHAR(100) PRIMARY KEY,
    calculation_id       VARCHAR(100) NOT NULL,
    engine               VARCHAR(60)  NOT NULL,
    school               VARCHAR(200),
    rule_id              VARCHAR(100) NOT NULL,
    rule_version         VARCHAR(30)  NOT NULL,

    -- C4: adopted from DATA_MODEL_AND_RETENTION.md over Master Spec section 5,
    -- which omitted it.
    dimension            VARCHAR(30),

    -- io.destinyos.core.evidence.Evidence#fact as a JSON string. See
    -- pom.xml: TEXT rather than a native JSON/JSONB type, for migration
    -- portability between PostgreSQL and the H2 compatibility mode used
    -- locally.
    fact_json            TEXT,

    source               VARCHAR(200),
    evidence_group_id    VARCHAR(100),

    -- Mirrors io.destinyos.core.evidence.DataConfidence. Absent unless a
    -- methodology defines it (DECISION_LOG C8).
    data_confidence      VARCHAR(30),

    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_evidence_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id)
);

CREATE INDEX idx_evidence_calculation_id ON evidence (calculation_id);
CREATE INDEX idx_evidence_evidence_group_id ON evidence (evidence_group_id);

CREATE TABLE signals (
    signal_id            VARCHAR(100) PRIMARY KEY,
    calculation_id       VARCHAR(100) NOT NULL,
    engine               VARCHAR(60)  NOT NULL,
    school               VARCHAR(200),
    dimension            VARCHAR(30)  NOT NULL,
    tag                  VARCHAR(100) NOT NULL,

    -- Mirrors io.destinyos.core.signal.Polarity/Strength/Applicability.
    -- NEUTRAL/NOT_APPLICABLE are real, distinct values here - never
    -- collapsed together (audit risk RK7).
    polarity             VARCHAR(20) NOT NULL,
    strength             VARCHAR(20) NOT NULL,
    applicability        VARCHAR(20) NOT NULL,

    -- Sole encoding of criticality (DECISION_LOG C3) - strength never
    -- carries a CRITICAL value.
    critical             BOOLEAN NOT NULL DEFAULT FALSE,

    evidence_group_id    VARCHAR(100),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_signals_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id),
    CONSTRAINT chk_signals_polarity
        CHECK (polarity IN ('SUPPORT', 'CAUTION', 'NEGATIVE', 'NEUTRAL')),
    CONSTRAINT chk_signals_strength
        CHECK (strength IN ('WEAK', 'MEDIUM', 'STRONG')),
    CONSTRAINT chk_signals_applicability
        CHECK (applicability IN ('HIGH', 'MEDIUM', 'LOW', 'NOT_APPLICABLE'))
);

CREATE INDEX idx_signals_calculation_id ON signals (calculation_id);
CREATE INDEX idx_signals_dimension ON signals (dimension);

-- io.destinyos.core.signal.Signal#evidenceIds - a signal may cite several
-- pieces of evidence, and (per FUSION_ENGINE_SPEC.md section 5) the same
-- evidence may back more than one signal.
CREATE TABLE signal_evidence_refs (
    signal_id            VARCHAR(100) NOT NULL,
    evidence_id          VARCHAR(100) NOT NULL,

    PRIMARY KEY (signal_id, evidence_id),
    CONSTRAINT fk_signal_evidence_refs_signal
        FOREIGN KEY (signal_id) REFERENCES signals (signal_id),
    CONSTRAINT fk_signal_evidence_refs_evidence
        FOREIGN KEY (evidence_id) REFERENCES evidence (evidence_id)
);
