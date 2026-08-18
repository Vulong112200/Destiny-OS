-- V1: identity foundation (DATA_MODEL_AND_RETENTION.md section 2).
--
-- Deliberately minimal. Authentication (password hashing, OAuth, JWT -
-- Master Spec section 28) is not specified anywhere in the current
-- documentation and is not designed here. Building it now would be
-- inventing a security scheme without a citation, which is the same
-- failure CLAUDE.md Rule C forbids for calculation algorithms. This table
-- exists only so birth_profiles has an owner to reference.
--
-- Portable SQL: written to run unchanged on PostgreSQL (the production
-- target, ADR D1) and on H2 in PostgreSQL-compatibility mode (local
-- verification, in the absence of Docker/Postgres in this environment -
-- see destiny-persistence/pom.xml).

CREATE TABLE users (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email               VARCHAR(320) NOT NULL,
    display_name        VARCHAR(200),
    -- Master Spec section 26: production UI is Vietnamese by default.
    locale              VARCHAR(20)  NOT NULL DEFAULT 'vi-VN',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Person / birth profile (Master Spec section 2).
--
-- birth_region is free-form VARCHAR rather than a constrained enum on
-- purpose: research item R17 (which granularity "region" should have -
-- province, north/south jurisdiction, coordinates) is still
-- DECISION_REQUIRED. Hard-coding a fixed set of region codes here would
-- silently make that decision. The column exists so the schema is ready;
-- its permitted values are not fixed until R17 resolves.
CREATE TABLE birth_profiles (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    full_name               VARCHAR(200) NOT NULL,

    birth_date              DATE NOT NULL,
    birth_time              TIME,
    -- Mirrors io.destinyos.core.context.BirthTimePrecision. Defaults to the
    -- cautious value: Master Spec section 2 forbids treating UNKNOWN as
    -- EXACT, so an unset precision must never silently read as EXACT.
    birth_time_precision    VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',

    birth_timezone          VARCHAR(60),
    birth_location          VARCHAR(300),
    -- See comment above: intentionally unconstrained pending R17.
    birth_region            VARCHAR(100),
    latitude                NUMERIC(9, 6),
    longitude               NUMERIC(9, 6),

    -- "gender/sex only where methodology requires" (Master Spec section 2).
    -- Nullable: most engines never read it.
    gender                  VARCHAR(20),

    locale                  VARCHAR(20) NOT NULL DEFAULT 'vi-VN',

    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_birth_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_birth_profiles_precision
        CHECK (birth_time_precision IN ('EXACT', 'APPROXIMATE', 'UNKNOWN'))
);

CREATE INDEX idx_birth_profiles_user_id ON birth_profiles (user_id);
