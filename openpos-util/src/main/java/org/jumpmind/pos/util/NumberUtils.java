package org.jumpmind.pos.util;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import static java.util.Arrays.stream;
import static java.util.Comparator.naturalOrder;
import static java.util.Optional.empty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * A set of utilities extending the numeric operations provided by Java and the Apache Commons libraries.
 *
 * @author Jason Weiss
 */
@RequiredArgsConstructor(access = PRIVATE)
@Slf4j
@SuppressWarnings("unused")
public class NumberUtils {
    /**
     * Indicates whether two numbers of the same type are quantitatively identical.
     *
     * @param <T> the type of numbers being compared
     * @param val1 the first number
     * @param val2 the second number
     * @return {@code true} if {@code val1} and {@code val2} are quantitatively identical; {@code true} if both arguments are {@code null};
     * {@code false} if exactly one argument is {@code null}
     */
    public static <T extends Number & Comparable<? super T>> boolean isEqual(T val1, T val2) {
        if (val1 == null) {
            return (val2 == null);
        }
        else if (val2 == null) {
            return false;
        }
        return val1.compareTo(val2) == 0;
    }

    /**
     * Indicates whether the first number is greater than the second.
     *
     * @param <T> the type of numbers being compared
     * @param val1 the comparison subject number
     * @param val2 the comparison object number
     * @return {@code true} if {@code val1} is greater than {@code val2}; {@code false} if either argument is {@code null}
     */
    public static <T extends Number & Comparable<? super T>> boolean isGreaterThan(T val1, T val2) {
        return (val1 != null) && (val2 != null) && (val1.compareTo(val2) > 0);
    }

    /**
     * Indicates whether the first number is greater than or equal to the second.
     *
     * @param <T> the type of numbers being compared
     * @param val1 the comparison subject number
     * @param val2 the comparison object number
     * @return {@code true} if {@code val1} is greater than or equal to {@code val2}; {@code false} if either argument is {@code null}
     */
    public static <T extends Number & Comparable<? super T>> boolean isGreaterThanOrEqual(T val1, T val2) {
        return (val1 != null) && (val2 != null) && (val1.compareTo(val2) >= 0);
    }

    /**
     * Indicates whether the first number is less than the second.
     *
     * @param <T> the type of numbers being compared
     * @param val1 the comparison subject number
     * @param val2 the comparison object number
     * @return {@code true} if {@code val1} is less than {@code val2}; {@code false} if either argument is {@code null}
     */
    public static <T extends Number & Comparable<? super T>> boolean isLessThan(T val1, T val2) {
        return (val1 != null) && (val2 != null) && (val1.compareTo(val2) < 0);
    }

    /**
     * Indicates whether the first number is less than or equal to the second.
     *
     * @param <T> the type of numbers being compared
     * @param val1 the comparison subject number
     * @param val2 the comparison object number
     * @return {@code true} if {@code val1} is less than or equal to {@code val2}; {@code false} if either argument is {@code null}
     */
    public static <T extends Number & Comparable<? super T>> boolean isLessThanOrEqual(T val1, T val2) {
        return (val1 != null) && (val2 != null) && (val1.compareTo(val2) <= 0);
    }

    /**
     * Indicates whether the specified number is less than zero (i.e. a negative number).
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is negative; {@code false} if {@code val} is {@code null}
     */
    public static boolean isNegative(Number val) {
        return (val != null) && (numberToLongBits(val) < 0);
    }

    /**
     * Indicates whether the specified number is greater than or equal to zero (i.e. a non-negative number).
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is non-negative; {@code false} if {@code val} is {@code null}
     */
    public static boolean isNonNegative(Number val) {
        return (val != null) && (numberToLongBits(val) >= 0);
    }

    /**
     * Indicates whether the specified number is less than or equal to zero (i.e. a non-positive number).
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is non-positive; {@code false} if {@code val} is {@code null}
     */
    public static boolean isNonPositive(Number val) {
        return (val != null) && (numberToLongBits(val) <= 0);
    }

    /**
     * Indicates whether the specified number is greater than zero (i.e. a positive number).
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is positive; {@code false} if {@code val} is {@code null}
     */
    public static boolean isPositive(Number val) {
        return (val != null) && (numberToLongBits(val) > 0);
    }

    private static long numberToLongBits(Number val) {
        return Double.doubleToRawLongBits(val.doubleValue());
    }

    /**
     * Indicates whether the specified number is zero.  This will report {@code true} for all zeroes, regardless of data type or precision.
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is zero; {@code false} if {@code val} is {@code null}
     */
    public static boolean isZero(Number val) {
        return (val != null) && (numberToLongBits(val) == 0);
    }

    /**
     * Indicates whether the specified number is zero or {@code null}.  This will report {@code true} for all zeroes, regardless of data type or
     * precision.
     *
     * @param val the number to test
     * @return {@code true} if {@code val} is zero or {@code null}
     */
    public static boolean isZeroOrNull(Number val) {
        return (val == null) || isZero(val);
    }

    /**
     * Reports the maximum number among a set of numbers, per their natural ordering.
     *
     * @param <T> the type of numbers being evaluated and returned
     * @param vals the numbers to evaluate; {@code null} elements will be ignored
     * @return the maximum among all non-{@code null} numbers in {@code vals}; {@link Optional#empty()} if {@code vals} is {@code null}, empty, or
     * contains only {@code null} elements
     */
    @SafeVarargs
    public static <T extends Number & Comparable<? super T>> Optional<T> max(T... vals) {
        return (ArrayUtils.isEmpty(vals)) ? empty() : stream(vals).filter(Objects::nonNull).max(naturalOrder());
    }

    /**
     * Reports the minimum number among a set of numbers, per their natural ordering.
     *
     * @param <T> the type of numbers being evaluated and returned
     * @param vals the numbers to evaluate; {@code null} elements will be ignored
     * @return the minimum among all non-{@code null} numbers in {@code vals}; {@link Optional#empty()} if {@code vals} is {@code null}, empty, or
     * contains only {@code null} elements
     */
    @SafeVarargs
    public static <T extends Number & Comparable<? super T>> Optional<T> min(T... vals) {
        return (ArrayUtils.isEmpty(vals)) ? empty() : stream(vals).filter(Objects::nonNull).min(naturalOrder());
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@link BigDecimal#ZERO} otherwise
     */
    public static BigDecimal zeroIfNull(BigDecimal val) {
        return defaultIfNull(val, BigDecimal.ZERO);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@link BigInteger#ZERO} otherwise
     */
    public static BigInteger zeroIfNull(BigInteger val) {
        return defaultIfNull(val, BigInteger.ZERO);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Double.valueOf(0d)} otherwise
     */
    public static Double zeroIfNull(Double val) {
        return defaultIfNull(val, 0d);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Float.valueOf(0f)} otherwise
     */
    public static Float zeroIfNull(Float val) {
        return defaultIfNull(val, 0f);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Long.valueOf(0L)} otherwise
     */
    public static Long zeroIfNull(Long val) {
        return defaultIfNull(val, 0L);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Integer.valueOf(0)} otherwise
     */
    public static Integer zeroIfNull(Integer val) {
        return defaultIfNull(val, 0);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Short.valueOf(0)} otherwise
     */
    public static Short zeroIfNull(Short val) {
        return defaultIfNull(val, (short) 0);
    }

    /**
     * Returns the specified number if it is non-{@code null} and zero otherwise.
     *
     * @param val the number to evaluate
     * @return {@code val} if it is non-{@code null}; {@code Byte.valueOf(0)} otherwise
     */
    public static Byte zeroIfNull(Byte val) {
        return defaultIfNull(val, (byte) 0);
    }
}
