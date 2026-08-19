package io.destinyos.calendar;

/**
 * Gregorian &lt;-&gt; Vietnamese lunisolar date conversion, including the
 * leap-month (nhuận) rule.
 *
 * <p>Leap-month rule source: Explanatory Supplement to the Astronomical
 * Almanac (P. Kenneth Seidelmann, ed.), as quoted verbatim on
 * https://www.xemamlich.uhm.vn/calrules_en.html: "an intercalary year has
 * thirteen lunar months... the Winter Solstice always falls in month 11...
 * In an intercalary year, a month in which there is no Principal Term is
 * the intercalary month... If two months of an intercalary year contain no
 * Principal Term, only the first such month after the Winter Solstice is
 * considered intercalary." R16 confirms this is structurally identical to
 * the Chinese no-zhongqi rule; the divergence between the two calendars
 * comes entirely from the meridian (105 degrees East here vs 120 degrees
 * East for China), not from a different rule.
 *
 * <p>Algorithm and every numeric constant cross-checked byte-for-byte
 * against https://github.com/vanng822/amlich (lib/amlich-aa98.js,
 * functions {@code getNewMoonDay}, {@code getLunarMonth11},
 * {@code getLeapMonthOffset}, {@code convertSolar2Lunar},
 * {@code convertLunar2Solar}) and Vietnamese Wikipedia's "Mô đun:Âm lịch"
 * Lua module (equivalent {@code LunarMonth11}/{@code LunarYear} functions,
 * a differently-structured but rule-equivalent implementation). Both are
 * used only as cross-check oracles (ADR D3) — this is an independent
 * expression of the same published rule, not a copy of either.
 *
 * <p>{@code timezoneOffsetHours} is a plain numeric offset (e.g. 7.0 for
 * UTC+7), deliberately not a {@code ZoneId}: which offset was actually in
 * force at a given historical (date, region) is R14a/R14b's problem, to be
 * resolved by the caller before this class is invoked — this class does not
 * know about Vietnamese history, only about lunisolar arithmetic given an
 * already-resolved offset.
 */
public final class LunarCalendar {

    private static final double SYNODIC_MONTH = 29.530588853;
    private static final double NEW_MOON_1900_EPOCH_JD = 2415021.076998695;

    private LunarCalendar() {
    }

    private static long floor(double d) {
        return (long) Math.floor(d);
    }

    /** Julian day number of the k-th new moon, localized to the given timezone offset. */
    private static long newMoonDay(int k, double timezoneOffsetHours) {
        return floor(NewMoon.julianDay(k) + 0.5 + timezoneOffsetHours / 24.0);
    }

    /** Julian day number on which lunar month 11 of Gregorian year {@code yy} begins. */
    private static long lunarMonth11Start(int yy, double timezoneOffsetHours) {
        long off = JulianDay.fromDate(31, 12, yy) - 2415021L;
        int k = (int) floor(off / SYNODIC_MONTH);
        long newMoon = newMoonDay(k, timezoneOffsetHours);
        int principalTerm = SolarTerm.principalTermIndexAt(newMoon, timezoneOffsetHours);
        if (principalTerm >= 9) {
            newMoon = newMoonDay(k - 1, timezoneOffsetHours);
        }
        return newMoon;
    }

    /**
     * How many lunar months after month 11 (starting at {@code a11}) the
     * leap month falls, per the Seidelmann rule above.
     */
    private static int leapMonthOffset(long a11, double timezoneOffsetHours) {
        int k = (int) floor((a11 - NEW_MOON_1900_EPOCH_JD) / SYNODIC_MONTH + 0.5);
        int last = 0;
        int i = 1;
        int arc = SolarTerm.principalTermIndexAt(newMoonDay(k + i, timezoneOffsetHours), timezoneOffsetHours);
        do {
            last = arc;
            i++;
            arc = SolarTerm.principalTermIndexAt(newMoonDay(k + i, timezoneOffsetHours), timezoneOffsetHours);
        } while (arc != last && i < 14);
        return i - 1;
    }

    public static LunarDate toLunar(int day, int month, int year, double timezoneOffsetHours) {
        long dayNumber = JulianDay.fromDate(day, month, year);
        int k = (int) floor((dayNumber - NEW_MOON_1900_EPOCH_JD) / SYNODIC_MONTH);
        long monthStart = newMoonDay(k + 1, timezoneOffsetHours);
        if (monthStart > dayNumber) {
            monthStart = newMoonDay(k, timezoneOffsetHours);
        }

        long a11 = lunarMonth11Start(year, timezoneOffsetHours);
        long b11 = a11;
        int lunarYear;
        if (a11 >= monthStart) {
            lunarYear = year;
            a11 = lunarMonth11Start(year - 1, timezoneOffsetHours);
        } else {
            lunarYear = year + 1;
            b11 = lunarMonth11Start(year + 1, timezoneOffsetHours);
        }

        int lunarDay = (int) (dayNumber - monthStart + 1);
        int diff = (int) floor((monthStart - a11) / 29.0);
        boolean lunarLeap = false;
        int lunarMonth = diff + 11;

        if (b11 - a11 > 365) {
            int leapMonthDiff = leapMonthOffset(a11, timezoneOffsetHours);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = true;
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }
        return new LunarDate(lunarDay, lunarMonth, lunarYear, lunarLeap);
    }

    /** @return {@code null} if {@code lunar} names a leap month that does not actually occur in that year */
    public static int[] toGregorian(LunarDate lunar, double timezoneOffsetHours) {
        long a11;
        long b11;
        if (lunar.month() < 11) {
            a11 = lunarMonth11Start(lunar.year() - 1, timezoneOffsetHours);
            b11 = lunarMonth11Start(lunar.year(), timezoneOffsetHours);
        } else {
            a11 = lunarMonth11Start(lunar.year(), timezoneOffsetHours);
            b11 = lunarMonth11Start(lunar.year() + 1, timezoneOffsetHours);
        }
        int k = (int) floor(0.5 + (a11 - NEW_MOON_1900_EPOCH_JD) / SYNODIC_MONTH);
        int off = lunar.month() - 11;
        if (off < 0) {
            off += 12;
        }
        if (b11 - a11 > 365) {
            int leapOff = leapMonthOffset(a11, timezoneOffsetHours);
            int leapMonth = leapOff - 2;
            if (leapMonth < 0) {
                leapMonth += 12;
            }
            if (lunar.leap() && lunar.month() != leapMonth) {
                return null;
            } else if (lunar.leap() || off >= leapOff) {
                off += 1;
            }
        }
        long monthStart = newMoonDay(k + off, timezoneOffsetHours);
        return JulianDay.toDate(monthStart + lunar.day() - 1);
    }
}
