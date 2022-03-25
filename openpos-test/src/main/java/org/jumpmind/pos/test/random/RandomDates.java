package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.apache.commons.lang3.Validate.notNull;

import static java.time.temporal.ChronoUnit.DAYS;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * A set of utilities generating random {@link Date} values for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("unused")
public class RandomDates {
    private static final Date EARLIEST_DATE = new Date(0);
    private static final Date LATEST_DATE = Date.from(LocalDate.of(2200, 12, 31)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant());

    /**
     * Generates a random {@link Date}.
     *
     * @return a random {@code Date}
     */
    public static Date randomDate() {
        return randomDateImpl(EARLIEST_DATE, LATEST_DATE);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param earliestExclusive the earliest date (exclusive); cannot be {@code null}
     * @return a random {@code Date} after {@code earliestExclusive}
     * @throws NullPointerException if {@code earliestExclusive} is {@code null}
     */
    public static Date randomDateAfter(Date earliestExclusive) {
        return randomDateImpl(instantAfter(notNull(earliestExclusive, "earliestExclusive cannot be null")), LATEST_DATE);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param refDate the reference date; cannot be {@code null}
     * @param minDaysAfter the minimum number of days after {@code refDate}; must be > 0
     * @return a random {@code Date} at least {@code minDaysAfter} days after {@code refDate}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code refDate} is {@code null}
     */
    public static Date randomDateAtLeastDaysAfter(Date refDate, int minDaysAfter) {
        Validate.notNull(refDate);
        Validate.isTrue(minDaysAfter > 0, "minDaysAfter must be > 0");

        return randomDateImpl(daysInFuture(refDate, minDaysAfter), LATEST_DATE);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param refDate the reference date; cannot be {@code null}
     * @param minDaysBefore the minimum number of days before {@code refDate}; must be > 0
     * @return a random {@code Date} at least {@code minDaysBefore} days before {@code refDate}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code refDate} is {@code null}
     */
    public static Date randomDateAtLeastDaysBefore(Date refDate, int minDaysBefore) {
        Validate.notNull(refDate);
        Validate.isTrue(minDaysBefore > 0, "minDaysBefore must be > 0");

        return randomDateImpl(EARLIEST_DATE, daysInPast(refDate, minDaysBefore));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param refDate the reference date; cannot be {@code null}
     * @param maxDaysAfter the maximum number of days after {@code refDate}; must be > 0
     * @return a random {@code Date} at most {@code maxDaysAfter} days after {@code refDate}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code refDate} is {@code null}
     */
    public static Date randomDateAtMostDaysAfter(Date refDate, int maxDaysAfter) {
        Validate.notNull(refDate);
        Validate.isTrue(maxDaysAfter > 0, "maxDaysAfter must be > 0");

        return randomDateImpl(refDate, daysInFuture(refDate, maxDaysAfter));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param refDate the reference date; cannot be {@code null}
     * @param maxDaysBefore the maximum number of days before {@code refDate}; must be > 0
     * @return a random {@code Date} at most {@code maxDaysBefore} days before {@code refDate}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code refDate} is {@code null}
     */
    public static Date randomDateAtMostDaysBefore(Date refDate, int maxDaysBefore) {
        Validate.notNull(refDate);
        Validate.isTrue(maxDaysBefore > 0, "maxDaysBefore must be > 0");

        return randomDateImpl(daysInPast(refDate, maxDaysBefore), refDate);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param latestExclusive the latest date (exclusive); cannot be {@code null}
     * @return a random {@code Date} before {@code latestExclusive}
     * @throws NullPointerException if {@code latestExclusive} is {@code null}
     */
    public static Date randomDateBefore(Date latestExclusive) {
        return randomDateImpl(EARLIEST_DATE, instantBefore(notNull(latestExclusive, "latestExclusive cannot be null")));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param earliestInclusive the earliest date (inclusive); cannot be {@code null} and must be < {@code latestExclusive}
     * @param latestExclusive the latest date (exclusive); cannot be {@code null} and must be > {@code earliestInclusive}
     * @return a random {@code Date} between {@code earliestInclusive} and {@code latestExclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code refDate} is {@code null}
     */
    public static Date randomDateBetween(Date earliestInclusive, Date latestExclusive) {
        return randomDateImpl(
                notNull(earliestInclusive, "earliestInclusive cannot be null"),
                notNull(latestExclusive, "latestExclusive cannot be null"));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @return a random {@link Date} in the future
     */
    public static Date randomFutureDate() {
        return randomDateImpl(instantAfterNow(), LATEST_DATE);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param minDaysAfterToday the minimum number of days after today; must be > 0
     * @return a random {@code Date} at least {@code minDaysAfterToday} days after today
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Date randomFutureDateAtLeastDaysFromNow(int minDaysAfterToday) {
        Validate.isTrue(minDaysAfterToday > 0, "minDaysAfterToday must be > 0");
        return randomDateImpl(daysInFuture(now(), minDaysAfterToday), LATEST_DATE);
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param maxDaysAfterToday the maximum number of days after today; must be > 0
     * @return a random {@code Date} at most {@code maxDaysAfterToday} days after today
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Date randomFutureDateAtMostDaysFromNow(int maxDaysAfterToday) {
        Validate.isTrue(maxDaysAfterToday > 0, "maxDaysAfterToday must be > 0");
        return randomDateImpl(instantAfterNow(), daysInFuture(now(), maxDaysAfterToday));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @return a random {@link Date} in the past
     */
    public static Date randomPastDate() {
        return randomDateImpl(EARLIEST_DATE, instantBeforeNow());
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param minDaysBeforeToday the minimum number of days before today; must be > 0
     * @return a random {@code Date} at least {@code minDaysBeforeToday} days before today
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Date randomPastDateAtLeastDaysBefore(int minDaysBeforeToday) {
        Validate.isTrue(minDaysBeforeToday > 0, "minDaysBeforeToday must be > 0");
        return randomDateImpl(EARLIEST_DATE, daysInPast(now(), minDaysBeforeToday));
    }

    /**
     * Generates a random {@link Date}.
     *
     * @param maxDaysBeforeToday the maximum number of days before today; must be > 0
     * @return a random {@code Date} at most {@code maxDaysBeforeToday} days before today
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Date randomPastDateAtMostDaysBefore(int maxDaysBeforeToday) {
        Validate.isTrue(maxDaysBeforeToday > 0, "maxDaysAfterToday must be > 0");
        return randomDateImpl(daysInPast(now(), maxDaysBeforeToday), now());
    }

    private static Date daysInFuture(Date refDate, int daysInFuture) {
        Validate.notNull(refDate);
        Validate.isTrue(daysInFuture > 0, "daysInFuture must be > 0");

        return Date.from(refDate.toInstant().plus(daysInFuture, DAYS));
    }

    private static Date daysInPast(Date refDate, int daysInPast) {
        Validate.notNull(refDate);
        Validate.isTrue(daysInPast > 0, "daysInPast must be > 0");

        return Date.from(refDate.toInstant().minus(daysInPast, DAYS));
    }

    private static Date instantAfter(Date refDate) {
        return new Date(refDate.getTime() + 1L);
    }

    private static Date instantAfterNow() {
        return instantAfter(now());
    }

    private static Date instantBefore(Date refDate) {
        return new Date(refDate.getTime() - 1L);
    }

    private static Date instantBeforeNow() {
        return instantBefore(now());
    }

    private static Date now() {
        return new Date();
    }

    private static Date randomDateImpl(Date earliestInclusive, Date latestExclusive) {
        // Make sure the earliest and latest dates don't step outside our min/max bounds or we'll risk exceptions.
        return new Date(nextLong(
                Math.max(EARLIEST_DATE.getTime(), earliestInclusive.getTime()),
                Math.min(LATEST_DATE.getTime(), latestExclusive.getTime())));
    }
}
