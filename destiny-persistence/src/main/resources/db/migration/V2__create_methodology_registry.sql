-- V2: methodology registry (DATA_MODEL_AND_RETENTION.md section 2,
-- ADR D7, RESEARCH_BLOCKERS.md).
--
-- This is the table that makes ADR D7 real: a research-blocked methodology
-- is REGISTERED here with its true status, not omitted. A row with
-- status = 'RESEARCH_REQUIRED' is a correct, complete, queryable answer -
-- "this does not exist yet, and here is exactly why" - never a missing row
-- that looks like an oversight.
--
-- Registered before any engine (V4+) can write a result: CLAUDE.md
-- section 6 requires every calculation to name the methodology version
-- that produced it, so nothing downstream may persist without one to
-- point at.

CREATE TABLE methodologies (
    methodology_id      VARCHAR(60) PRIMARY KEY,
    display_name_vi     VARCHAR(200) NOT NULL,
    -- Free-form grouping (e.g. 'EASTERN', 'WESTERN'). Not constrained to a
    -- fixed list: DESTINY_OS_MASTER_SPECIFICATION.md section 1 groups
    -- systems this way in prose, but nothing in the spec defines it as a
    -- closed enum, so the column stays advisory rather than enforced.
    domain               VARCHAR(60),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- One row per school/version within a methodology (CLAUDE.md Rule D: never
-- merge schools into one average). Two schools that disagree are two rows,
-- not one row with a blended answer.
CREATE TABLE methodology_versions (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    methodology_id       VARCHAR(60) NOT NULL,
    version              VARCHAR(30) NOT NULL,

    -- Required (see chk_methodology_versions_calculable below) whenever
    -- status permits producing a result. Mirrors the same constructor
    -- check in io.destinyos.engine.EngineMetadata: an engine that may
    -- calculate but names no school or cites no source is exactly the
    -- silent-school-selection Rule D forbids.
    school               VARCHAR(200),
    source               TEXT,

    -- Mirrors io.destinyos.engine.MethodologyStatus.
    status               VARCHAR(30) NOT NULL,

    -- What is missing, or why this school was chosen over known variants.
    -- Free text because the reasons are genuinely heterogeneous - see
    -- RESEARCH_BLOCKERS.md for the level of detail expected here.
    notes                TEXT,

    effective_from       DATE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_methodology_versions_methodology
        FOREIGN KEY (methodology_id) REFERENCES methodologies (methodology_id),
    CONSTRAINT uq_methodology_versions_id_version
        UNIQUE (methodology_id, version),
    CONSTRAINT chk_methodology_versions_status
        CHECK (status IN (
            'PRODUCTION_READY', 'RESEARCH_REQUIRED', 'DECISION_REQUIRED',
            'CONTENT_REQUIRED', 'NOT_IMPLEMENTED', 'OUT_OF_SCOPE'
        )),
    -- PRODUCTION_READY and CONTENT_REQUIRED are the two statuses under
    -- which MethodologyStatus.mayCalculate() is true. A version in either
    -- state must name its school and cite a source - the database-level
    -- mirror of the EngineMetadata constructor guard from Phase 1.
    CONSTRAINT chk_methodology_versions_calculable
        CHECK (
            status NOT IN ('PRODUCTION_READY', 'CONTENT_REQUIRED')
            OR (school IS NOT NULL AND source IS NOT NULL)
        )
);

CREATE INDEX idx_methodology_versions_methodology_id
    ON methodology_versions (methodology_id);

-- Which RESEARCH_BLOCKERS.md items a methodology version is waiting on, or
-- was resolved by. One version can cite several (e.g. BAZI cites R1, R2
-- and R3), and one research item can be cited by several versions (R12
-- covers both I Ching and Mai Hoa).
CREATE TABLE methodology_version_research_refs (
    methodology_version_id  BIGINT NOT NULL,
    research_id              VARCHAR(20) NOT NULL,

    PRIMARY KEY (methodology_version_id, research_id),
    CONSTRAINT fk_mvrr_methodology_version
        FOREIGN KEY (methodology_version_id) REFERENCES methodology_versions (id)
);

-- Structural only in this phase: no methodology version yet has an actual
-- rule table to version (every calculable methodology so far - Numerology
-- Pythagorean, Tarot - is either not yet decided (R8) or content-gated
-- (R11)). The table is created now, per IMPLEMENTATION_PLAN.md section 4.1,
-- so it is ready the moment a real rule table needs a version, rather than
-- being retrofitted under time pressure later.
CREATE TABLE rule_versions (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    methodology_version_id      BIGINT NOT NULL,
    rule_table_name              VARCHAR(120) NOT NULL,
    version                      VARCHAR(30) NOT NULL,
    source                       TEXT,
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rule_versions_methodology_version
        FOREIGN KEY (methodology_version_id) REFERENCES methodology_versions (id),
    CONSTRAINT uq_rule_versions_table_version
        UNIQUE (methodology_version_id, rule_table_name, version)
);
