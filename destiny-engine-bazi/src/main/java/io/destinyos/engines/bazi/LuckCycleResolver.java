package io.destinyos.engines.bazi;

import io.destinyos.calendar.CanChi;
import io.destinyos.calendar.CanChiPillar;
import io.destinyos.calendar.JulianDay;
import io.destinyos.calendar.SolarTerm;
import io.destinyos.calendar.SolarYear;
import io.destinyos.core.context.Gender;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the Đại Vận sequence (research item R2).
 *
 * <p><strong>The three rules, and where each comes from.</strong>
 *
 * <ol>
 *   <li><strong>Direction</strong> — {@link LuckCycleDirection}. Unanimous
 *       across sources.</li>
 *   <li><strong>Which instant to count to</strong> — the adjacent
 *       <em>Tiết</em> (sectional term), i.e. the boundary of the solar-term
 *       month, from {@link SolarYear}. Not a choice made here: those twelve
 *       instants are already what this project's month pillar turns on, so any
 *       other answer would contradict a golden-tested result. Bát Tự sources
 *       state the same restriction outright ("推算大運要以節來推算，不能用氣來推算").</li>
 *   <li><strong>Converting that distance into an age</strong> — three days of
 *       distance to one year of age, and proportionally below that. See
 *       {@link #toStartAge}.</li>
 * </ol>
 *
 * <p><strong>What this deliberately does not do.</strong> It does not round
 * the start age to a whole number. Descriptions of rounding exist (drop a
 * remainder of one day; round at the half-year) but no worked example in any
 * source consulted applies them — all six reproduce the exact proportional
 * conversion instead. Rounding is therefore treated as presentation, left to
 * whoever displays the result, rather than baked in here where it would be
 * unrecoverable.
 */
final class LuckCycleResolver {

    /**
     * Seconds of distance per day of age.
     *
     * <p>Derived, not chosen: three days of distance make one year of age, and
     * the tradition's year here is twelve thirty-day months, so one day of age
     * is {@code 3 × 86400 / 360 = 720} seconds. Every finer equivalence the
     * sources quote falls out of this same constant — one day of distance to
     * four months of age, one canh giờ to ten days, one hour to five days,
     * twelve minutes to one day — which is why they are quoted as one
     * consistent chain rather than as competing conventions.
     */
    private static final long SECONDS_PER_AGE_DAY = 720L;

    private static final int DAYS_PER_AGE_MONTH = 30;
    private static final int MONTHS_PER_AGE_YEAR = 12;
    private static final int DAYS_PER_AGE_YEAR = DAYS_PER_AGE_MONTH * MONTHS_PER_AGE_YEAR;

    private LuckCycleResolver() {
    }

    /**
     * @param baziYear        the Lập Xuân-based year the month pillar was built from
     * @param solarMonthIndex 1-12 with Dần = 1, likewise
     * @param julianDateUt    the birth instant, as fed to the rest of the chart
     * @param solarLocal      the same instant in local solar time, for dating
     *                        the boundary and the periods
     * @param utcOffsetHours  the civil offset in force (R14a)
     */
    static LuckCycles resolve(CanChiPillar yearPillar,
                              int baziYear,
                              int solarMonthIndex,
                              Gender gender,
                              double julianDateUt,
                              LocalDateTime solarLocal,
                              double utcOffsetHours) {

        LuckCycleDirection direction =
                LuckCycleDirection.forBirth(yearPillar.stem(), gender);

        double boundaryJd = direction == LuckCycleDirection.THUAN
                ? SolarYear.nextSolarMonthStartJulianDate(julianDateUt)
                : SolarYear.solarMonthStartJulianDate(julianDateUt);

        // Always non-negative: forward looks ahead, backward looks behind.
        double distanceDays = direction == LuckCycleDirection.THUAN
                ? boundaryJd - julianDateUt
                : julianDateUt - boundaryJd;
        Duration distance = Duration.ofSeconds(
                Math.max(0L, Math.round(distanceDays * 86400.0)));

        Period startAge = toStartAge(distance);
        LocalDate birthDate = solarLocal.toLocalDate();
        LocalDate startDate = birthDate.plus(startAge);

        List<LuckPillar> pillars = new ArrayList<>(LuckCycles.PERIOD_COUNT);
        for (int i = 1; i <= LuckCycles.PERIOD_COUNT; i++) {
            CanChiPillar pillar = CanChi.monthPillarOffset(
                    baziYear, solarMonthIndex, i * direction.step());
            Period age = startAge.plusYears((long) (i - 1) * LuckCycles.YEARS_PER_PERIOD);
            pillars.add(new LuckPillar(i, pillar.stem(), pillar.branch(),
                    age.normalized(), birthDate.plus(age)));
        }

        return new LuckCycles(direction,
                boundaryTerm(boundaryJd),
                toLocal(boundaryJd, utcOffsetHours),
                distance,
                startAge,
                startDate,
                pillars);
    }

    /**
     * Distance to the boundary, converted into an age.
     *
     * <p>Integer arithmetic throughout, on seconds — so the result is exact
     * for the whole-day counts every published worked example uses, and no
     * floating point enters the domain (ADR D6). 25 days in gives exactly
     * 8 years 4 months, which is what the source that publishes that example
     * states.
     */
    static Period toStartAge(Duration distance) {
        long ageDays = distance.getSeconds() / SECONDS_PER_AGE_DAY;
        int years = (int) (ageDays / DAYS_PER_AGE_YEAR);
        long withinYear = ageDays % DAYS_PER_AGE_YEAR;
        int months = (int) (withinYear / DAYS_PER_AGE_MONTH);
        int days = (int) (withinYear % DAYS_PER_AGE_MONTH);
        return Period.of(years, months, days);
    }

    private static SolarTerm boundaryTerm(double boundaryJd) {
        // Nudge inside the new term: the boundary instant sits exactly on the
        // edge, where rounding could land the lookup in the term just ending.
        return SolarTerm.atJulianDate(boundaryJd + 1e-6);
    }

    private static LocalDateTime toLocal(double julianDateUt, double utcOffsetHours) {
        double local = julianDateUt + utcOffsetHours / 24.0;
        long dayNumber = (long) Math.floor(local + 0.5);
        int[] date = JulianDay.toDate(dayNumber);
        double dayFraction = local + 0.5 - Math.floor(local + 0.5);
        long secondOfDay = Math.round(dayFraction * 86400.0);
        // Guard the 86400 case: rounding up a fraction of 0.99999... would
        // otherwise produce an invalid time-of-day rather than the next day.
        if (secondOfDay >= 86400L) {
            secondOfDay -= 86400L;
            date = JulianDay.toDate(dayNumber + 1);
        }
        return LocalDateTime.of(date[2], date[1], date[0],
                (int) (secondOfDay / 3600), (int) ((secondOfDay % 3600) / 60),
                (int) (secondOfDay % 60));
    }
}
