package io.destinyos.core.retention;

/**
 * How long a stored record is allowed to live
 * (CLAUDE.md §7, DATA_MODEL_AND_RETENTION.md §7).
 *
 * <p>CLAUDE.md §7 is blunt about the problem this solves: <em>"Không lưu mọi
 * JSON khổng lồ mãi mãi trong hot relational tables."</em> Until this enum
 * existed, every calculation — including throwaway daily readings — was kept
 * forever, so the database could only ever grow.
 *
 * <p>The enum is deliberately about <strong>why</strong> a record is kept, not
 * about <em>how long</em>. Durations belong to a configurable policy
 * (DATA_MODEL_AND_RETENTION.md §8: <em>"Policy phải configurable"</em>); the
 * class is what a record intrinsically is, and it does not change when an
 * operator changes a number in a config file.
 */
public enum RetentionClass {

    /**
     * Kept indefinitely because the data is a durable fact about a person —
     * natal chart, birth profile. Never auto-deleted.
     */
    PERSISTENT,

    /**
     * Kept indefinitely because the user asked for it. Never auto-deleted, and
     * DATA_MODEL_AND_RETENTION.md §11 names this explicitly as the one class
     * cleanup must never touch: <em>"không xóa USER_SAVED"</em>.
     */
    USER_SAVED,

    /**
     * A throwaway reading — a daily action, a transient scenario run. Expires,
     * and expiring is the normal, intended end of its life rather than data
     * loss.
     */
    EPHEMERAL,

    /**
     * Immutable audit record. Never auto-deleted by the cleanup job: audit
     * retention is a policy decision made outside this system, and a cleanup
     * job that can quietly shorten an audit trail is worse than no cleanup at
     * all.
     */
    AUDIT;

    /**
     * Whether the automatic cleanup job may ever delete a record in this class.
     *
     * <p>Only {@link #EPHEMERAL} may. This is expressed as a method rather than
     * left to each call site's own {@code if} so that adding a future class
     * forces a decision here, in one place, instead of silently defaulting to
     * deletable in whichever query someone writes next.
     */
    public boolean isAutoDeletable() {
        return this == EPHEMERAL;
    }
}
