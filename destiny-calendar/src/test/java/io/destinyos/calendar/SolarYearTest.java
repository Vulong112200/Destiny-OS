package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Solar-term month boundaries, tested at the layer that owns them.
 *
 * <p><strong>Why this file exists separately from {@code LuckCycleTest}.</strong>
 * {@link SolarYear#solarMonthStartJulianDate} and
 * {@link SolarYear#nextSolarMonthStartJulianDate} were added for Bát Tự's Đại
 * Vận (R2) and were initially covered only through it — two golden vectors in
 * {@code destiny-engine-bazi}. That is the wrong place for the only coverage:
 * every other calendar derivation here ({@code SolarTermInstantTest},
 * {@code HiddenStemsTest}, {@code CanChiAttributesTest}) is proven at its own
 * layer rather than through whichever feature happened to need it first, and
 * an engine-level test cannot distinguish "the boundary search is wrong" from
 * "the conversion that consumes it is wrong".
 *
 * <p>The invariants below are deliberately structural rather than a second
 * copy of R2's golden vectors: a root-finder is exactly the kind of code where
 * a sign error or a wraparound bug produces plausible-looking output for most
 * inputs and nonsense for a narrow band.
 */
class SolarYearTest {

    /** UTC+7, the offset Vietnam has used nationally since 1975. */
    private static final double VN_OFFSET = 7.0;

    private static double jd(int year, int month, int day, int hour) {
        return JulianDay.fromLocalDateTime(
                LocalDateTime.of(year, month, day, hour, 0), VN_OFFSET);
    }

    @Nested
    @DisplayName("The boundary is the Tiết that opened the current month")
    class BoundaryIdentity {

        @Test
        @DisplayName("A mid-February birth sits in the month Lập Xuân opened")
        void lapXuanOpensMonthOne() {
            // Lập Xuân 1984 is independently published at 23:18:44 Beijing on
            // 4 February; SolarTermInstantTest pins this project's own value
            // and its known deviation. Here the point is only that the search
            // lands on that term, not on a neighbouring one.
            double start = SolarYear.solarMonthStartJulianDate(jd(1984, 2, 20, 12));

            assertThat(SolarTerm.atJulianDate(start + 1e-6)).isEqualTo(SolarTerm.LAP_XUAN);
            assertThat(SolarYear.solarMonthIndex(jd(1984, 2, 20, 12)))
                    .as("Dần is solar month 1").isEqualTo(1);
        }

        @Test
        @DisplayName("Every one of the twelve boundaries is a sectional term, never a principal one")
        void allBoundariesAreSectionalTerms() {
            // The load-bearing claim behind R2: these instants are the twelve
            // "Tiết" (節), disjoint from the twelve "Trung Khí" (中氣) that
            // SolarTerm flags as principal. In this enum principal terms sit
            // at even ordinals (Xuân Phân 0, Cốc Vũ 2, ... Vũ Thủy 22), so a
            // sectional term is exactly an odd ordinal.
            for (int month = 1; month <= 12; month++) {
                // Sample near the middle of each calendar month across a year,
                // which necessarily visits all twelve solar months.
                double instant = jd(2024, month, 15, 12);
                SolarTerm opening = SolarTerm.atJulianDate(
                        SolarYear.solarMonthStartJulianDate(instant) + 1e-6);

                assertThat(opening.ordinal() % 2)
                        .as("%s (ordinal %d) must be a sectional term, not a principal one",
                                opening, opening.ordinal())
                        .isEqualTo(1);
            }
        }

        @Test
        @DisplayName("The twelve boundaries found across a year are exactly the twelve Tiết")
        void theTwelveBoundariesAreTheExpectedSet() {
            java.util.Set<SolarTerm> found = new java.util.HashSet<>();
            for (int month = 1; month <= 12; month++) {
                found.add(SolarTerm.atJulianDate(
                        SolarYear.solarMonthStartJulianDate(jd(2024, month, 15, 12)) + 1e-6));
            }

            assertThat(found).containsExactlyInAnyOrder(
                    SolarTerm.LAP_XUAN, SolarTerm.KINH_TRAP, SolarTerm.THANH_MINH,
                    SolarTerm.LAP_HA, SolarTerm.MANG_CHUNG, SolarTerm.TIEU_THU,
                    SolarTerm.LAP_THU, SolarTerm.BACH_LO, SolarTerm.HAN_LO,
                    SolarTerm.LAP_DONG, SolarTerm.DAI_TUYET, SolarTerm.TIEU_HAN);
        }
    }

    @Nested
    @DisplayName("Structural invariants a root-finder must satisfy")
    class Invariants {

        @Test
        @DisplayName("The current boundary is in the past and the next one is in the future")
        void boundariesBracketTheInstant() {
            for (int month = 1; month <= 12; month++) {
                for (int day : new int[] {2, 15, 27}) {
                    double instant = jd(2023, month, day, 6);
                    assertThat(SolarYear.solarMonthStartJulianDate(instant))
                            .as("start for %d-%d", month, day).isLessThanOrEqualTo(instant);
                    assertThat(SolarYear.nextSolarMonthStartJulianDate(instant))
                            .as("next for %d-%d", month, day).isGreaterThan(instant);
                }
            }
        }

        @Test
        @DisplayName("A solar month lasts between 29 and 32 days — never a degenerate zero or a double span")
        void monthLengthIsPlausible() {
            // A sign error or a bad bracket typically shows up here as either
            // a near-zero span (found the same boundary twice) or a ~61-day
            // span (skipped one).
            for (int month = 1; month <= 12; month++) {
                double instant = jd(2023, month, 15, 12);
                double span = SolarYear.nextSolarMonthStartJulianDate(instant)
                        - SolarYear.solarMonthStartJulianDate(instant);

                assertThat(span).as("span of the month containing 2023-%d-15", month)
                        .isBetween(29.0, 32.0);
            }
        }

        @Test
        @DisplayName("One month's next boundary is the following month's current boundary")
        void boundariesChainWithoutGaps() {
            double instant = jd(2024, 6, 10, 9);
            double next = SolarYear.nextSolarMonthStartJulianDate(instant);

            // Step just past the boundary and ask again: the month that has
            // begun must report the same instant as its own start.
            assertThat(SolarYear.solarMonthStartJulianDate(next + 1e-4))
                    .isCloseTo(next, org.assertj.core.data.Offset.offset(1e-3));
        }

        @Test
        @DisplayName("The boundary instant belongs to the month it opens, not the one it ends")
        void boundaryBelongsToTheMonthItOpens() {
            double instant = jd(2024, 9, 20, 15);
            int monthIndex = SolarYear.solarMonthIndex(instant);
            double start = SolarYear.solarMonthStartJulianDate(instant);

            assertThat(SolarYear.solarMonthIndex(start + 1e-6)).isEqualTo(monthIndex);
            assertThat(SolarYear.solarMonthIndex(start - 1e-4)).isNotEqualTo(monthIndex);
        }
    }

    @Nested
    @DisplayName("The 360°/0° wraparound")
    class Wraparound {

        @Test
        @DisplayName("Solar month 2 (Mão) spans 345° to 15°, crossing zero — both boundaries still resolve")
        void monthSpanningZeroDegrees() {
            // This is the one month whose span crosses the discontinuity: it
            // opens at Kinh Trập (345°) and closes at Thanh Minh (15°). A
            // normalisation bug in the signed-gap function shows up here and
            // essentially nowhere else.
            double instant = jd(2024, 3, 20, 12);

            assertThat(SolarYear.solarMonthIndex(instant)).as("Mão is solar month 2").isEqualTo(2);
            assertThat(SolarTerm.atJulianDate(
                    SolarYear.solarMonthStartJulianDate(instant) + 1e-6))
                    .isEqualTo(SolarTerm.KINH_TRAP);
            assertThat(SolarTerm.atJulianDate(
                    SolarYear.nextSolarMonthStartJulianDate(instant) + 1e-6))
                    .isEqualTo(SolarTerm.THANH_MINH);
        }

        @Test
        @DisplayName("Late December sits in month 11, whose next boundary is Tiểu Hàn in the new year")
        void boundaryCrossingTheGregorianYear() {
            double instant = jd(2023, 12, 20, 12);

            assertThat(SolarYear.solarMonthIndex(instant)).as("Tý is solar month 11").isEqualTo(11);
            double next = SolarYear.nextSolarMonthStartJulianDate(instant);
            assertThat(SolarTerm.atJulianDate(next + 1e-6)).isEqualTo(SolarTerm.TIEU_HAN);
            assertThat(next).as("Tiểu Hàn falls in early January, so past the year end")
                    .isGreaterThan(jd(2024, 1, 1, 0));
        }
    }

    @Nested
    @DisplayName("Lập Xuân-based year")
    class LapXuanYear {

        @Test
        @DisplayName("1 January keeps the previous year; 20 December keeps the current one")
        void januaryBelongsToThePreviousYear() {
            double newYearsDay = jd(2000, 1, 1, 12);
            assertThat(SolarYear.lapXuanBasedYear(
                    java.time.LocalDate.of(2000, 1, 1),
                    SolarYear.solarMonthIndex(newYearsDay))).isEqualTo(1999);

            double december = jd(2024, 12, 20, 12);
            assertThat(SolarYear.lapXuanBasedYear(
                    java.time.LocalDate.of(2024, 12, 20),
                    SolarYear.solarMonthIndex(december))).isEqualTo(2024);
        }
    }
}
