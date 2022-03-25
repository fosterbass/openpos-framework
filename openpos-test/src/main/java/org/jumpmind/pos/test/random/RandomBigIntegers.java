package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigInteger;

/**
 * A set of utilities generating random {@link BigInteger} values for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("unused")
public class RandomBigIntegers {
    private static final long MAX_VALUE = Long.MAX_VALUE - 1L;

    /**
     * Generates a random {@link BigInteger}.
     *
     * @param minInclusive the minimum value of the generated {@code BigInteger} (inclusive); must be >= 0
     * @return a {@code BigInteger} having a value between {@code minInclusive} and {@link Long#MAX_VALUE}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigInteger randomBigIntegerAtLeast(long minInclusive) {
        return randomBigIntegerImpl(minInclusive, MAX_VALUE);
    }

    /**
     * Generates a random {@link BigInteger}.
     *
     * @param maxInclusive the maximum value of the generated {@code BigInteger} (inclusive); must be >= 0
     * @return a {@code BigInteger} having a value between {@code 0} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigInteger randomBigIntegerAtMost(long maxInclusive) {
        return randomBigIntegerImpl(0, maxInclusive);
    }

    /**
     * Generates a random {@link BigInteger}.
     *
     * @param minInclusive the minimum value of the generated {@code BigInteger} (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated {@code BigInteger} (inclusive); must be >= 0 and >= {@code minInclusive}
     * @return a {@code BigInteger} having a value between {@code minInclusive} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigInteger randomBigIntegerBetween(long minInclusive, long maxInclusive) {
        return randomBigIntegerImpl(minInclusive, maxInclusive);
    }

    /**
     * Generates a random {@link BigInteger}.
     *
     * @param minExclusive the minimum value of the generated {@code BigInteger} (exclusive); must be >= 0
     * @return a {@code BigInteger} having a value between {@code minExclusive + 1} and {@link Long#MAX_VALUE}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigInteger randomBigIntegerGreaterThan(long minExclusive) {
        return randomBigIntegerImpl(minExclusive + 1L, MAX_VALUE);
    }

    /**
     * Generates a random {@link BigInteger}.
     *
     * @param maxExclusive the maximum value of the generated {@code BigInteger} (exclusive); must be > 0
     * @return a {@code BigInteger} having a value between {@code 0} and {@code maxExclusive - 1}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigInteger randomBigIntegerLessThan(long maxExclusive) {
        return randomBigIntegerImpl(0, maxExclusive - 1);
    }

    private static BigInteger randomBigIntegerImpl(long minInclusive, long maxInclusive) {
        Validate.isTrue(minInclusive >= 0, "min must be >= 0");
        Validate.isTrue(maxInclusive >= 0, "max must be >= 0");

        // Enforce our min/max boundaries.
        final long boundedMin = Math.min(MAX_VALUE, minInclusive);
        final long boundedMax = Math.min(MAX_VALUE, maxInclusive);

        Validate.isTrue(boundedMin <= boundedMax, "min must be <= max");

        return BigInteger.valueOf(RandomUtils.nextLong(boundedMin, boundedMax + 1L));
    }
}
