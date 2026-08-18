-- V4: calculation records (DATA_MODEL_AND_RETENTION.md section 3-4,
-- CLAUDE.md section 6, DECISION_LOG C7).
--
-- A "calculation" is one scenario run: it can involve several engines,
-- each recorded as its own calculation_engine_results row (Rule F failure
-- isolation - one engine's status never overwrites another's).
--
-- calculation_id is the domain's own String identifier
-- (CalculationContext.calculationId()), not a surrogate key, so a caller
-- can look up a calculation by the same id it used to request it.

CREATE TABLE calculations (
    calculation_id       VARCHAR(100) PRIMARY KEY,
    user_id              BIGINT,
    birth_profile_id     BIGINT,
    scenario_id          VARCHAR(60),

    input_hash           VARCHAR(128) NOT NULL,
    methodology_version  VARCHAR(30)  NOT NULL,
    algorithm_version    VARCHAR(30)  NOT NULL,
    rule_version         VARCHAR(30)  NOT NULL,
    calendar_version     VARCHAR(30),
    timezone             VARCHAR(60)  NOT NULL,
    seed                 BIGINT,

    -- C7: DATA_MODEL_AND_RETENTION.md section 3 omitted this while section 4
    -- (engine result) and section 10 (snapshot integrity) both require a
    -- hash. Added here per that decision.
    result_hash          VARCHAR(128),

    -- Mirrors io.destinyos.core.result.EngineStatus, rolled up across every
    -- engine that ran (see io.destinyos.execution.ExecutionOutcome#overallStatus).
    status                VARCHAR(30) NOT NULL,

    started_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at          TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_calculations_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_calculations_birth_profile
        FOREIGN KEY (birth_profile_id) REFERENCES birth_profiles (id)
);

CREATE INDEX idx_calculations_user_id ON calculations (user_id);
CREATE INDEX idx_calculations_birth_profile_id ON calculations (birth_profile_id);

-- One row per engine invocation within a calculation (Rule F: an engine's
-- failure or timeout is recorded against ITS row only, never affecting
-- another engine's row).
CREATE TABLE calculation_engine_results (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    calculation_id       VARCHAR(100) NOT NULL,
    engine               VARCHAR(60)  NOT NULL,

    -- Mirrors io.destinyos.core.result.EngineStatus.
    status               VARCHAR(30) NOT NULL,

    -- For large raw results kept in object storage rather than the row
    -- itself (DATA_MODEL_AND_RETENTION.md section 9). Unused until R2/S3
    -- storage is wired up; the column exists so that is additive, not a
    -- migration.
    result_uri           VARCHAR(500),
    result_hash          VARCHAR(128),
    error_code           VARCHAR(60),

    duration_ms          BIGINT,
    timed_out            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_calc_engine_results_calculation
        FOREIGN KEY (calculation_id) REFERENCES calculations (calculation_id),
    CONSTRAINT chk_calc_engine_results_status
        CHECK (status IN (
            'SUCCESS', 'PARTIAL', 'NOT_APPLICABLE', 'RESEARCH_REQUIRED',
            'NOT_IMPLEMENTED', 'INVALID_INPUT', 'FAILED_RECOVERABLE', 'FAILED_FATAL'
        ))
);

CREATE INDEX idx_calc_engine_results_calculation_id
    ON calculation_engine_results (calculation_id);
