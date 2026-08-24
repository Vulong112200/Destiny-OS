package io.destinyos.engines.iching;

/**
 * Which classical procedure produced a reading. Each carries its own
 * probability distribution over {@link LineValue} (Three Coins and Yarrow
 * genuinely differ — see {@code docs/research_drafts/VERIFICATION_OPUS_R12.md}
 * §B/§C) or its own deterministic arithmetic (the two Mai Hoa methods), so
 * this is a real methodological distinction, not an implementation detail —
 * CLAUDE.md Rule D requires it be named explicitly on every reading, never
 * silently defaulted.
 */
public enum CastingMethod {
    /** 三錢起卦 — three coins tossed six times. */
    THREE_COINS,
    /** 大衍筮法 / 蓍草筮法 — the 50-stalk yarrow procedure from 繫辭傳. */
    YARROW,
    /** 梅花易數 數字起卦 — two supplied numbers (not a single number; see class Javadoc on {@link IChingEngine}). */
    MAI_HOA_NUMBER,
    /** 梅花易數 年月日時起例 — derived from the casting instant's lunar year/month/day/hour. */
    MAI_HOA_TIME
}
