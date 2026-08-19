package io.destinyos.calendar;

import io.destinyos.core.context.BirthTimePrecision;
import io.destinyos.core.context.Uncertainty;
import io.destinyos.core.context.UncertaintyKind;
import io.destinyos.core.version.CalendarMethodologyRef;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves one UTC instant to the Vietnamese lunisolar calendar and Can Chi
 * pillars (ADR D3). The single entry point every future consumer (Bát Tự,
 * Tử Vi) is meant to call once their own algorithms are unblocked — see
 * this module's Javadoc for why it is not itself a {@code MetaphysicalEngine}.
 *
 * <p>Composition, not new algorithm: {@link HistoricalTimezoneRuleTable}
 * resolves which civil offset applies (R14a/R14b), {@link SolarTimeCorrection}
 * optionally refines it to mean solar time (R10), {@link HourBranchResolver}
 * applies the Giờ Tý boundary (R10) to find the hour branch and whether the
 * day pillar rolls over, {@link LunarCalendar} converts to a lunar date
 * (R9/R15/R16), and {@link CanChi} derives all four pillars.
 *
 * <p>Known simplification: which era's timezone rule applies is looked up
 * using the instant's plain UTC calendar date, not its not-yet-known local
 * date. Era boundaries in {@link HistoricalTimezoneRuleTable} are multi-year
 * apart, so this only risks a wrong era exactly at the handful of hours
 * surrounding a transition's UTC midnight - a real but narrow edge case,
 * noted rather than silently accepted as exact.
 */
public final class CalendarEngine {

    public static final String METHODOLOGY_ID = "VN_TRADITIONAL";
    public static final String METHODOLOGY_VERSION = "1.0";
    public static final String METHODOLOGY_SOURCE =
            "Jean Meeus, Astronomical Algorithms (1998) low-precision solar/lunar series; "
                    + "cross-checked against Ho Ngoc Duc's worked tables "
                    + "(xemamlich.uhm.vn/calrules_en.html, 1983-1986) and Vietnamese Wikipedia "
                    + "\"Mo dun:Am lich\" (Can Chi arithmetic); 105 degrees East meridian, "
                    + "no-zhongqi leap rule (R9/R15/R16); historical timezone table per R14a; "
                    + "Gio Ty 23:00 boundary and solar time policy per R10 (DECISION_LOG, 2026-08-19).";

    private CalendarEngine() {
    }

    /**
     * @param utcInstant             the instant to resolve, in UTC
     * @param region                 jurisdiction for historical timezone
     *                               resolution (R14a/R14b); use
     *                               {@link VietnameseRegion#UNKNOWN} when
     *                               not confidently known — never guess
     * @param longitudeDegreesIfKnown birth longitude, positive east, or
     *                               {@code null} to use civil time only
     * @param precision              never treat {@code UNKNOWN} as
     *                               {@code EXACT} (Master Spec section 2) —
     *                               day/hour pillars are omitted, not
     *                               guessed, when precision doesn't support them
     */
    public static CalendarResolution resolve(Instant utcInstant, VietnameseRegion region,
                                             Double longitudeDegreesIfKnown, BirthTimePrecision precision) {
        Objects.requireNonNull(utcInstant, "utcInstant");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(precision, "precision");

        List<Uncertainty> uncertainties = new ArrayList<>();

        LocalDate utcDate = utcInstant.atOffset(ZoneOffset.UTC).toLocalDate();
        Optional<HistoricalTimezoneRule> ruleOpt = HistoricalTimezoneRuleTable.resolve(utcDate, region);

        if (ruleOpt.isEmpty()) {
            uncertainties.add(Uncertainty.of(UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN,
                    "No sourced historical timezone rule covers " + utcDate + " for region " + region
                            + " (R14b: geographic North/South boundary has no source).",
                    "R14b"));
            CalendarMethodologyRef unresolvedRef = new CalendarMethodologyRef(METHODOLOGY_ID, METHODOLOGY_VERSION,
                    null, null, METHODOLOGY_SOURCE, null, null);
            return new CalendarResolution(null, null, null, null, null, null,
                    unresolvedRef, List.copyOf(uncertainties));
        }

        HistoricalTimezoneRule rule = ruleOpt.get();
        uncertainties.add(Uncertainty.informational(UncertaintyKind.HISTORICAL_TIMEZONE_RULE_UNKNOWN,
                "Dựa trên trích dẫn Công Báo qua nguồn thứ cấp (Wikipedia); "
                        + "văn bản gốc chưa được đối chiếu trực tiếp."));

        double utcOffsetHours = rule.utcOffsetHours();
        LocalDateTime civilLocal = LocalDateTime.ofInstant(utcInstant,
                ZoneOffset.ofTotalSeconds((int) Math.round(utcOffsetHours * 3600)));

        LocalDateTime solarLocal = civilLocal;
        if (longitudeDegreesIfKnown != null) {
            var correction = SolarTimeCorrection.meanSolarTimeCorrection(longitudeDegreesIfKnown, utcOffsetHours);
            solarLocal = civilLocal.plus(correction);
        } else {
            uncertainties.add(Uncertainty.of(UncertaintyKind.LONGITUDE_UNKNOWN,
                    "No birth longitude supplied; used civil clock time instead of solar-corrected time.",
                    "R10"));
        }

        LocalDate solarDate = solarLocal.toLocalDate();
        LocalTime solarTime = solarLocal.toLocalTime();

        LunarDate lunar = LunarCalendar.toLunar(solarDate.getDayOfMonth(), solarDate.getMonthValue(),
                solarDate.getYear(), utcOffsetHours);
        CanChiPillar yearPillar = CanChi.yearPillar(lunar.year());
        CanChiPillar monthPillar = CanChi.monthPillar(lunar.year(), lunar.month());

        CanChiPillar dayPillar = null;
        CanChiPillar hourPillar = null;
        if (precision.supportsHourPrecision()) {
            boolean rollsOver = HourBranchResolver.rollsOverToNextDay(solarTime, ZiHourBoundaryPolicy.ZI_HOUR_23_00);
            LocalDate dayPillarDate = rollsOver ? solarDate.plusDays(1) : solarDate;
            long dayPillarJd = JulianDay.fromDate(dayPillarDate.getDayOfMonth(),
                    dayPillarDate.getMonthValue(), dayPillarDate.getYear());
            dayPillar = CanChi.dayPillar(dayPillarJd, utcOffsetHours);
            EarthlyBranch hourBranch = HourBranchResolver.branchAt(solarTime, ZiHourBoundaryPolicy.ZI_HOUR_23_00);
            hourPillar = CanChi.hourPillar(dayPillar.stem(), hourBranch);
        } else {
            uncertainties.add(Uncertainty.of(UncertaintyKind.BIRTH_TIME_IMPRECISE,
                    "Birth time precision is " + precision + "; day and hour pillars require EXACT precision.",
                    null));
        }

        long dayNumberForTerm = JulianDay.fromDate(solarDate.getDayOfMonth(), solarDate.getMonthValue(), solarDate.getYear());
        SolarTerm solarTerm = SolarTerm.at(dayNumberForTerm, utcOffsetHours);

        CalendarMethodologyRef ref = new CalendarMethodologyRef(METHODOLOGY_ID, METHODOLOGY_VERSION,
                rule.from(), rule.toExclusive(), METHODOLOGY_SOURCE, rule.source(), rule.source());

        return new CalendarResolution(lunar, yearPillar, monthPillar, dayPillar, hourPillar, solarTerm,
                ref, List.copyOf(uncertainties));
    }
}
