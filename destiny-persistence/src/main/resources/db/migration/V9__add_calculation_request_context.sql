-- V9: the user's own question, recorded with the run that answered it.
--
-- Why this migration exists: the question was already being accepted by the
-- API (TarotRequest.question) and then thrown away. It reached the Tarot
-- engine's input record, no engine read it, and nothing wrote it down - so a
-- reading persisted in V4 could never be shown next to the question that
-- produced it, and re-reading a saved result told the user what the cards
-- said without recalling what they had asked. That is a data-integrity gap,
-- not a UI one: the answer was durable and the question was not.
--
-- These columns live on `calculations` rather than in a side table because
-- they are 1:1 with the run, are always read together with it, and are small.
-- DATA_MODEL_AND_RETENTION.md section 3's rule against parking large JSON in
-- hot relational tables is about unbounded blobs; three short, bounded,
-- queryable strings are exactly what this table is for.
--
-- Nullable with no default and no backfill: a run recorded before this
-- migration genuinely had no question attached, and inventing an empty string
-- for it would make "asked nothing" indistinguishable from "asked and we lost
-- it". NULL says the honest thing.

ALTER TABLE calculations
    ADD COLUMN question VARCHAR(500);

-- focus_id / focus_label are a user-intent label from the UI ("doi-viec" /
-- "Đổi việc / nhảy việc"): presentation and narrative framing only.
--
-- They are recorded, never acted on. Nothing in the deterministic path reads
-- them: they do not select a school (CLAUDE.md Rule D), do not change an
-- engine input, do not change applicability, and do not affect any signal,
-- score or fused outcome. They exist so the reading can be presented next to
-- the intent the user expressed, and so a later audit can see what the user
-- was looking at when they asked. If a focus value ever needs to change what
-- is *computed*, that is a ScenarioDefinition with a sourced policy, not a
-- string from the client.
--
-- Deliberately not constrained to an enumerated set: the UI's shortcut
-- buttons are product copy that changes without a schema change, and a CHECK
-- constraint here would turn a copy edit into a migration while protecting
-- nothing that matters (the value is never branched on).
ALTER TABLE calculations
    ADD COLUMN focus_id VARCHAR(100);

ALTER TABLE calculations
    ADD COLUMN focus_label VARCHAR(200);
