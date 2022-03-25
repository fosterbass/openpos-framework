package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

/**
 * A set of utilities generating random {@link BigDecimal} values for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("unused")
public class RandomBigDecimals {
    private static final long MAX_VALUE = Long.MAX_VALUE - 1L;
    private static final int UNSCALED = 0;

    /**
     * Generates a random, unscaled {@link BigDecimal}.
     *
     * @param minInclusive the minimum value of the generated {@code BigDecimal} (inclusive); must be >= 0
     * @return an unscaled {@code BigDecimal} having a value between {@code minInclusive} and {@link Long#MAX_VALUE}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalAtLeast(long minInclusive) {
        return randomBigDecimalAtLeast(minInclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal}.
     *
     * @param minInclusive the minimum value of the generated {@code BigDecimal} (inclusive); must be >= 0
     * @param scale the number of decimal places in the generated {@code BigDecimal}; must be >= 0
     * @return a {@code BigDecimal} having a value between {@code minInclusive} and {@link Long#MAX_VALUE}, scaled to {@code scale} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalAtLeast(long minInclusive, int scale) {
        return randomBigDecimalImpl(minInclusive, MAX_VALUE, scale);
    }

    /**
     * Generates a random, unscaled {@link BigDecimal}.
     *
     * @param maxInclusive the maximum value of the generated {@code BigDecimal} (inclusive); must be >= 0
     * @return an unscaled {@code BigDecimal} having a value between {@code 0} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalAtMost(long maxInclusive) {
        return randomBigDecimalAtMost(maxInclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal}.
     *
     * @param maxInclusive the maximum value of the generated {@code BigDecimal} (inclusive); must be >= 0
     * @param scale the number of decimal places in the generated {@code BigDecimal}; must be >= 0
     * @return a {@code BigDecimal} having a value between {@code 0} and {@code maxInclusive}, scaled to {@code scale} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalAtMost(long maxInclusive, int scale) {
        return randomBigDecimalImpl(0, maxInclusive, scale);
    }

    /**
     * Generates a random, unscaled {@link BigDecimal}.
     *
     * @param minInclusive the minimum value of the generated {@code BigDecimal} (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated {@code BigDecimal} (inclusive); must be >= 0 and >= {@code minInclusive}
     * @return an unscaled {@code BigDecimal} having a value between {@code minInclusive} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalBetween(long minInclusive, long maxInclusive) {
        return randomBigDecimalBetween(minInclusive, maxInclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal}.
     *
     * @param minInclusive the minimum value of the generated {@code BigDecimal} (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated {@code BigDecimal} (inclusive); must be >= 0 and >= {@code minInclusive}
     * @param scale the number of decimal places in the generated {@code BigDecimal}; must be >= 0
     * @return a {@code BigDecimal} having a value between {@code minInclusive} and {@code maxInclusive}, scaled to {@code scale} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalBetween(long minInclusive, long maxInclusive, int scale) {
        return randomBigDecimalImpl(minInclusive, maxInclusive, scale);
    }

    /**
     * Generates a random, unscaled {@link BigDecimal}.
     *
     * @param minExclusive the minimum value of the generated {@code BigDecimal} (exclusive); must be >= 0
     * @return an unscaled {@code BigDecimal} having a value between {@code minExclusive + 1} and {@link Long#MAX_VALUE}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalGreaterThan(long minExclusive) {
        return randomBigDecimalGreaterThan(minExclusive + 1L, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal}.
     *
     * @param minExclusive the minimum value of the generated {@code BigDecimal} (exclusive); must be >= 0
     * @param scale the number of decimal places in the generated {@code BigDecimal}; must be >= 0
     * @return a {@code BigDecimal} having a value between {@code minExclusive + 1} and {@link Long#MAX_VALUE}, scaled to {@code scale} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalGreaterThan(long minExclusive, int scale) {
        return randomBigDecimalImpl(minExclusive + 1L, MAX_VALUE, scale);
    }

    /**
     * Generates a random, unscaled {@link BigDecimal}.
     *
     * @param maxExclusive the maximum value of the generated {@code BigDecimal} (exclusive); must be > 0
     * @return an unscaled {@code BigDecimal} having a value between {@code 0} and {@code maxExclusive - 1}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalLessThan(long maxExclusive) {
        return randomBigDecimalLessThan(maxExclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal}.
     *
     * @param maxExclusive the maximum value of the generated {@code BigDecimal} (exclusive); must be > 0
     * @param scale the number of decimal places in the generated {@code BigDecimal}; must be >= 0
     * @return a {@code BigDecimal} having a value between {@code 0} and {@code maxExclusive - 1}, scaled to {@code scale} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomBigDecimalLessThan(long maxExclusive, int scale) {
        return randomBigDecimalImpl(0, maxExclusive - 1, scale);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage; <i>e.g.</i> "134%" will be reported as a {@code
     * BigDecimal} having an unscaled value of {@code 13400} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param maxInclusive the maximum value of the generated percentage (inclusive); must be >= 0
     * @return a whole-number, un-shifted percentage between {@code 0} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentAtMost(int maxInclusive) {
        return randomPercentAtMost(maxInclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage; <i>e.g.</i> "134.25%" will be reported as a {@code BigDecimal}
     * having an unscaled value of {@code 13425} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param maxInclusive the maximum value of the generated percentage (inclusive); must be >= 0
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code 0} and {@code maxInclusive}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentAtMost(int maxInclusive, int precision) {
        return randomPercentImpl(0, maxInclusive, precision);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage; <i>e.g.</i> "134%" will be reported as a {@code
     * BigDecimal} having an unscaled value of {@code 13400} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param minInclusive the minimum amount of the generated percentage (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated percentage (inclusive); must be >= 0 and >= {@code minInclusive}
     * @return a whole-number, un-shifted percentage between {@code minInclusive} and {@code maxInclusive}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentBetween(int minInclusive, int maxInclusive) {
        return randomPercentBetween(minInclusive, maxInclusive, 0);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage; <i>e.g.</i> "134.25%" will be reported as a {@code BigDecimal}
     * having an unscaled value of {@code 13425} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param minInclusive the minimum amount of the generated percentage (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated percentage (inclusive); must be >= 0 and >= {@code minInclusive}
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code minInclusive} and {@code maxInclusive}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentBetween(int minInclusive, int maxInclusive, int precision) {
        return randomPercentImpl(minInclusive, maxInclusive, precision);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage; <i>e.g.</i> "134%" will be reported as a {@code
     * BigDecimal} having an unscaled value of {@code 13400} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param maxExclusive the maximum value of the generated percentage (exclusive); must be > 0
     * @return a whole-number, un-shifted percentage between {@code 0} and {@code maxExclusive - 1}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentLessThan(int maxExclusive) {
        return randomPercentLessThan(maxExclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage; <i>e.g.</i> "134.25%" will be reported as a {@code BigDecimal}
     * having an unscaled value of {@code 13425} and a scale of <em>2</em> and <strong>not</strong> a scale of <em>4</em>.
     *
     * @param maxExclusive the maximum value of the generated percentage (exclusive); must be > 0
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code 0} and {@code maxExclusive - 1}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentLessThan(int maxExclusive, int precision) {
        return randomPercentImpl(0, maxExclusive - 1, precision);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage between 0% and 100%.  <i>E.g.</i> "38%" will be
     * reported as a {@code BigDecimal} having an unscaled value of {@code 3800} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @return a whole-number, un-shifted percentage between {@code 0} and {@code 100}
     */
    public static BigDecimal randomPercentOf100() {
        return randomPercentOf100(UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage between 0% and 100%.  <i>E.g.</i> "38.25%" will be reported as a
     * {@code BigDecimal} having an unscaled value of {@code 3825} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code 0} and {@code 100}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentOf100(int precision) {
        return randomPercentImpl(0, 100, precision);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage not exceeding 100%.  <i>E.g.</i> "38%" will be
     * reported as a {@code BigDecimal} having an unscaled value of {@code 3800} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @param minInclusive the minimum amount of the generated percentage (inclusive); must be >= 0
     * @return a whole-number, un-shifted percentage between {@code minInclusive} and {@code 100}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentOf100AtLeast(int minInclusive) {
        return randomPercentOf100AtLeast(minInclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage not exceeding 100%.  <i>E.g.</i> "38.25%" will be reported as a
     * {@code BigDecimal} having an unscaled value of {@code 3825} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @param minInclusive the minimum amount of the generated percentage (inclusive); must be >= 0
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code minInclusive} and {@code 100}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentOf100AtLeast(int minInclusive, int precision) {
        return randomPercentImpl(minInclusive, 100, precision);
    }

    /**
     * Generates a random {@link BigDecimal} representing a whole-number, un-shifted percentage not exceeding 100%.  <i>E.g.</i> "38%" will be
     * reported as a {@code BigDecimal} having an unscaled value of {@code 3800} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @param minExclusive the minimum amount of the generated percentage (exclusive); must be > 0
     * @return a whole-number, un-shifted percentage between {@code minExclusive + 1} and {@code 100}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentOf100GreaterThan(int minExclusive) {
        return randomPercentOf100GreaterThan(minExclusive, UNSCALED);
    }

    /**
     * Generates a random {@link BigDecimal} representing an un-shifted percentage not exceeding 100%.  <i>E.g.</i> "38.25%" will be reported as a
     * {@code BigDecimal} having an unscaled value of {@code 3825} and a scale of <em>2</em> rather than a scale of <em>4</em>.
     *
     * @param minExclusive the minimum amount of the generated percentage (exclusive); must be > 0
     * @param precision the number of decimal places in the generated percentage; must be >= 0
     * @return an un-shifted percentage between {@code minExclusive + 1} and {@code 100}, scaled to {@code precision + 2} decimal places
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static BigDecimal randomPercentOf100GreaterThan(int minExclusive, int precision) {
        return randomPercentImpl(minExclusive + 1, 100, precision);
    }

    private static BigDecimal randomBigDecimalImpl(long minInclusive, long maxInclusive, int scale) {
        Validate.isTrue(minInclusive >= 0, "min must be >= 0");
        Validate.isTrue(maxInclusive >= 0, "max must be >= 0");
        Validate.isTrue(scale >= 0, "scale must be non-negative");

        final long scaledMin = minInclusive * (int) Math.pow(10, scale);
        final long scaledMax = maxInclusive * (int) Math.pow(10, scale);

        // Enforce our min/max boundaries.
        final long boundedMin = Math.min(MAX_VALUE, scaledMin);
        final long boundedMax = Math.min(MAX_VALUE, scaledMax);

        Validate.isTrue(boundedMin <= boundedMax, "min must be <= max");

        return BigDecimal.valueOf(RandomUtils.nextLong(scaledMin, scaledMax + 1L), scale);
    }

    private static BigDecimal randomPercentImpl(int minInclusive, int maxInclusive, int scale) {
        Validate.isTrue(scale >= 0, "scale must be >= 0");
        return randomBigDecimalImpl(minInclusive, maxInclusive, scale + 2);
    }
}
