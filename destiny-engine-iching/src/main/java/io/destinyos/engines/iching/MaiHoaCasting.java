package io.destinyos.engines.iching;

/**
 * 梅花易數起卦法 — Mai Hoa Dịch Số's two deterministic (non-random) casting
 * methods, both quoted directly from 梅花易數 卷一 (see
 * {@code docs/research_drafts/R12_iching_maihoa.md} §5, verified verbatim
 * against Wikisource in {@code VERIFICATION_OPUS_R12.md} §A5).
 *
 * <p>Unlike Three Coins or Yarrow, these methods do not draw each of the 6
 * lines independently — they derive the upper and lower trigrams directly
 * from supplied numbers (or time components), then locate exactly one moving
 * line by dividing the grand (un-reduced) total by 6.
 */
final class MaiHoaCasting {

    private MaiHoaCasting() {
    }

    /**
     * 數字起卦 (two-number form only — see {@link IChingEngine}'s class
     * Javadoc for why a single multi-digit number is not accepted in this
     * version). Quoted example (R12 draft §5): "數字三作為上卦，數字六作為下
     * 卦" — the first number is the upper trigram, the second the lower,
     * each reduced mod 8 (卦以八除).
     *
     * <p>The moving line uses the same un-reduced grand total the
     * 年月日時起例 passage uses for its own moving-line step ("以上年月日
     * 共計幾數，以八除之...以六除，餘數作動爻" — the same running total feeds
     * both the mod-8 and the mod-6 steps) — here, simply the sum of the two
     * supplied numbers.
     */
    static MaiHoaCast fromNumbers(int upperNumber, int lowerNumber) {
        IChingTrigram upper = IChingTrigram.fromNumber(upperNumber);
        IChingTrigram lower = IChingTrigram.fromNumber(lowerNumber);
        int movingLine = movingLineFromTotal(upperNumber + lowerNumber);
        return new MaiHoaCast(upper, lower, movingLine);
    }

    /**
     * 年月日時起例, quoted in full in {@code VERIFICATION_OPUS_R12.md} §A5:
     * 「年月日為上卦。年月日加時總數為下卦...以上年月日共計幾數，以八除之，以
     * 餘數作卦。...就將年月日數加時之數，總計幾數，以八除之，餘數作下卦；以
     * 六除，餘數作動爻。」
     *
     * @param yearBranchIndex  Chi năm, Tý=1...Hợi=12 ({@link io.destinyos.calendar.EarthlyBranch#index()})
     * @param lunarMonth       tháng âm lịch, 1-12 (a leap month uses its base month number — the source text does not address leap months for this method)
     * @param lunarDay         ngày âm lịch, 1-30
     * @param hourBranchIndex  Chi giờ tại thời điểm gieo quẻ, Tý=1...Hợi=12
     */
    static MaiHoaCast fromDateTime(int yearBranchIndex, int lunarMonth, int lunarDay, int hourBranchIndex) {
        int upperRaw = yearBranchIndex + lunarMonth + lunarDay;
        int lowerRaw = upperRaw + hourBranchIndex;
        IChingTrigram upper = IChingTrigram.fromNumber(upperRaw);
        IChingTrigram lower = IChingTrigram.fromNumber(lowerRaw);
        int movingLine = movingLineFromTotal(lowerRaw);
        return new MaiHoaCast(upper, lower, movingLine);
    }

    /**
     * 爻以六除 (Chi năm giờ tra: 「凡起動爻，以重卦總數除六，以餘數作動爻」).
     *
     * <p><strong>Rule D decision (DECISION_LOG.md, 2026-08-24), not a
     * classical statement.</strong> The source text is genuinely silent on
     * what happens when the total divides evenly by 6 — it covers "less than
     * 6" and "more than 6" but never "exactly divisible". This project
     * decided remainder 0 → line 6 (the topmost line), by direct analogy
     * with the same text's own explicit rule for the *other* division it
     * defines (「如得八數整，即坤卦，更不必除也」 — dividing by 8 with no
     * remainder is stated outright to mean 8, not 0), and with the same
     * convention independently confirmed for a third division in the same
     * tradition (Yarrow's own 揲之以四 step, {@link YarrowCasting}). This is
     * an analogy, not a direct citation for this specific division — see the
     * decision log entry for the alternative this project did not find
     * evidence for but also did not rule out.
     */
    private static int movingLineFromTotal(int total) {
        int remainder = total % 6;
        return remainder == 0 ? 6 : remainder;
    }

    record MaiHoaCast(IChingTrigram upper, IChingTrigram lower, int movingLinePosition) {
    }
}
