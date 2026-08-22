-- V8: retention classes and the cleanup audit trail
-- (CLAUDE.md section 7, DATA_MODEL_AND_RETENTION.md sections 7, 8 and 11).
--
-- Why this migration exists: every calculation written before it was kept
-- forever. CLAUDE.md section 7 forbids exactly that ("Khong luu moi JSON
-- khong lo mai mai trong hot relational tables"), and a daily reading is the
-- clearest case - it is useful for a day and then it is landfill.
--
-- Two columns, not one. retention_class says WHY a row is kept and never
-- changes when an operator retunes a duration; expires_at is the derived
-- instant the policy produced. Keeping both means a policy change applies to
-- new rows without silently rewriting the meaning of old ones, and the
-- cleanup query stays a plain indexed range scan instead of recomputing a
-- policy per row.

ALTER TABLE calculations
    ADD COLUMN retention_class VARCHAR(20) NOT NULL DEFAULT 'EPHEMERAL';

ALTER TABLE calculations
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

-- Mirrors io.destinyos.core.retention.RetentionClass.
ALTER TABLE calculations
    ADD CONSTRAINT chk_calculations_retention_class
        CHECK (retention_class IN ('PERSISTENT', 'USER_SAVED', 'EPHEMERAL', 'AUDIT'));

-- The cleanup job's only query: "which EPHEMERAL rows are past their expiry".
-- Leading with retention_class matters - it is the far more selective column
-- once saved and persistent rows accumulate, and it lets the planner skip
-- every non-deletable row without touching expires_at at all.
CREATE INDEX idx_calculations_retention
    ON calculations (retention_class, expires_at);

-- DEFAULT 'EPHEMERAL' above deliberately applies to rows written before this
-- migration too, but with expires_at left NULL. A NULL expiry never matches
-- the cleanup predicate, so pre-existing rows are classified honestly
-- ("this was a transient run") while still being immune to deletion: no row
-- gets destroyed by a policy that did not exist when it was created.

-- DATA_MODEL_AND_RETENTION.md section 11 requires the cleanup job itself to
-- be auditable ("cron/job: ... dry-run; audit; batch delete; retry"). Without
-- this table a deletion run leaves no trace, which is the one thing a
-- destructive scheduled job must never do.
CREATE TABLE retention_runs (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- TRUE for a dry run: candidates were counted, nothing was deleted.
    dry_run             BOOLEAN NOT NULL,

    -- The instant the run treated as "now" when comparing against
    -- expires_at. Recorded rather than inferred from started_at so a
    -- backfill or a replay is reproducible.
    cutoff              TIMESTAMP WITH TIME ZONE NOT NULL,

    candidates_found    INTEGER NOT NULL,
    calculations_deleted INTEGER NOT NULL,
    failures            INTEGER NOT NULL,

    -- Non-null when at least one calculation could not be deleted; carries
    -- the first failure's message so a failing run is diagnosable from the
    -- audit trail alone.
    first_failure       VARCHAR(500),

    started_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_retention_runs_started_at ON retention_runs (started_at);
