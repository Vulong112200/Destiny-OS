package io.destinyos.calendar;

/**
 * A Vietnamese lunisolar calendar date.
 *
 * @param day        day of the lunar month (1-based)
 * @param month      lunar month number (1-12; a leap month shares its
 *                   preceding month's number, distinguished only by
 *                   {@code leap})
 * @param year       lunar year, numbered by the Gregorian year the lunar
 *                   year mostly falls within (matches every reference
 *                   implementation's convention)
 * @param leap       whether this is the intercalary (nhuận) occurrence of
 *                   {@code month}
 */
public record LunarDate(int day, int month, int year, boolean leap) {
}
