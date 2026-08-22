package io.destinyos.calendar;

/**
 * Year, Month, Day and Hour Can Chi (sexagenary) pillar arithmetic.
 *
 * <p>Year/Month/Day formulas source: Vietnamese Wikipedia "Mô đun:Âm lịch"
 * Lua module (function {@code canchi} and its call sites
 * {@code canchinam}/{@code canchithang}/{@code canchingay} inside
 * {@code p.main}) — a long-standing, publicly visible, community-maintained
 * module actually rendering lunar/Can-Chi dates across Vietnamese
 * Wikipedia. These are closed-form collapses of the traditional
 * "five tigers" (month-from-year) and continuous 60-day cycle rules, not
 * independently invented arithmetic.
 *
 * <p>Hour formula source: the traditional "Ngũ Thử Độn" (Five Rats) mnemonic
 * — "Giáp Kỷ hoàn gia Giáp, Ất Canh Bính tác sơ, Bính Tân tòng Mậu khởi,
 * Đinh Nhâm Canh Tý cư, Mậu Quý hà phương phát, Nhâm Tý thị chân đồ" — which
 * states, for each day-stem pair, which stem the Tý hour takes (Giáp/Kỷ →
 * Giáp; Ất/Canh → Bính; Bính/Tân → Mậu; Đinh/Nhâm → Canh; Mậu/Quý → Nhâm).
 * The closed form below was verified algebraically against all five cases
 * of that mnemonic before being trusted here, not assumed to be a valid
 * generalization.
 */
public final class CanChi {

    private CanChi() {
    }

    private static int wrap(long value, int modulus) {
        int m = (int) Math.floorMod(value, modulus);
        return m == 0 ? modulus : m;
    }

    private static CanChiPillar pillarOf(long x) {
        return new CanChiPillar(HeavenlyStem.fromIndex(wrap(x, 10)), EarthlyBranch.fromIndex(wrap(x, 12)));
    }

    /** @param lunarYear the Gregorian-numbered lunar year (see {@link LunarDate#year()}) */
    public static CanChiPillar yearPillar(int lunarYear) {
        return pillarOf(lunarYear + 57L);
    }

    /** @param lunarMonth 1-12; a leap month uses the same number as its preceding month */
    public static CanChiPillar monthPillar(int lunarYear, int lunarMonth) {
        return pillarOf((long) lunarYear * 12 + lunarMonth + 14);
    }

    /**
     * The month pillar {@code steps} positions away from {@code (lunarYear,
     * lunarMonth)} through the sexagenary cycle — forward for positive,
     * backward for negative.
     *
     * <p>Exists because Bát Tự's Đại Vận (R2) is exactly this: a walk along
     * the month-pillar sequence starting at the birth month, in whichever
     * direction the year stem's polarity and gender select. Expressed here
     * rather than by passing an out-of-range month to {@link #monthPillar} —
     * that happens to work, since the formula is pure arithmetic, but relying
     * on it would depend on an undocumented property of a method whose
     * contract says 1-12.
     */
    public static CanChiPillar monthPillarOffset(int lunarYear, int lunarMonth, int steps) {
        return pillarOf((long) lunarYear * 12 + lunarMonth + 14 + steps);
    }

    /**
     * @param noonReferencedJulianDay the calendar date's Julian day number
     *                                in the noon-referenced convention
     *                                {@link JulianDay#fromDate} and
     *                                {@link LunarCalendar} use throughout
     *                                this module
     * @param utcOffsetHours          the civil UTC offset in force for
     *                                that date/region (R14a)
     *
     *                                <p>Internally converts to the 0h-UT-referenced
     *                                local JD the source formula is defined
     *                                against ({@code LocalToJD(D,M,Y,utc)} in
     *                                Wikipedia's module, which differs from the
     *                                noon-referenced convention by exactly 0.5) -
     *                                pushed in here, once, rather than left for
     *                                every caller to remember; an earlier draft
     *                                of this method took a noon-referenced JD
     *                                directly and was off by one full day-pillar
     *                                step, caught by cross-checking 1 January 2000
     *                                (independently documented as Mậu Ngọ) before
     *                                this method was trusted.
     */
    public static CanChiPillar dayPillar(long noonReferencedJulianDay, double utcOffsetHours) {
        double localJdAtMidnight = noonReferencedJulianDay - 0.5 - utcOffsetHours / 24.0;
        return pillarOf((long) Math.floor(localJdAtMidnight + 51.5));
    }

    /**
     * The hour branch is supplied by the caller (from
     * {@link HourBranchResolver}, per R10's Giờ Tý policy) since it depends
     * on time-of-day, not on any Can Chi arithmetic. Only the hour stem is
     * derived here, from the day stem.
     */
    public static CanChiPillar hourPillar(HeavenlyStem dayStem, EarthlyBranch hourBranch) {
        // Deliberately NOT wrap(): the mnemonic's "remainder 0" case maps to
        // stem index 1 (Giap), not index 10 - a different convention from
        // pillarOf's year/month/day formulas. Verified against all 5 cases
        // of the mnemonic in this class's Javadoc before trusting it here.
        int raw = (dayStem.index() - 1) * 2 + (hourBranch.index() - 1);
        int stemIndex = Math.floorMod(raw, 10) + 1;
        return new CanChiPillar(HeavenlyStem.fromIndex(stemIndex), hourBranch);
    }
}
