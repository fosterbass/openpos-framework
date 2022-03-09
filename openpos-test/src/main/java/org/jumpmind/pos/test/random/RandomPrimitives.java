package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * A set of utilities generating random values of various primitive types for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
public class RandomPrimitives {
    private static final int MAX_INT = Integer.MAX_VALUE - 1;
    private static final long MAX_LONG = Long.MAX_VALUE - 1;
    private static final double MAX_DOUBLE = Double.MAX_VALUE - 1d;

    /**
     * Generates a random {@code boolean}.
     *
     * @return a randomly-generated {@code true} or {@code false}
     */
    public static boolean randomBoolean() {
        return nextBoolean();
    }

    /**
     * Generates a random {@code boolean} represented as an integer.
     *
     * @return {@code 0} if a {@code false} value is generated; {@code 1} if a {@code true} value is generated
     */
    public static int randomBooleanAsInt() {
        final boolean val = randomBoolean();
        return (val) ? 1 : 0;
    }

    /**
     * Selects a random {@code boolean} from among the members of a specified collection.
     *
     * @param candidate1 the first boolean eligible for selection
     * @param candidatesN any additional booleans eligible for selection
     * @return a randomly-selected {@code boolean} among the non-{@code null} members of {@code candidate1...candidatesN}; {@code false} if
     * {@code candidate1} is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code null} elements
     */
    public static boolean randomBooleanFrom(Boolean candidate1, Boolean... candidatesN) {
        final Boolean selection = RandomSelections.selectRandomFrom(candidate1, candidatesN);
        return (selection != null) && selection;
    }

    /**
     * Selects a random {@code boolean} from among the members of a specified collection.
     *
     * @param candidates all booleans eligible for selection
     * @return a randomly-selected {@code boolean} among the non-{@code null} members of {@code candidates}; {@code false} if {@code candidates} is
     * empty, {@code null}, or includes only {@code null} members
     */
    public static boolean randomBooleanFrom(Iterable<Boolean> candidates) {
        final Boolean selection = RandomSelections.selectRandomFrom(candidates);
        return (selection != null) && selection;
    }

    /**
     * Generates a random {@code double}.
     *
     * @return a randomly-generated, non-negative {@code double}
     */
    public static double randomDouble() {
        return randomDoubleImpl(0, MAX_DOUBLE);
    }

    /**
     * Generates a random {@code double}.
     *
     * @param min the minimum result (inclusive); must be >= 0
     * @return a randomly-generated {@code double} whose value is >= {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static double randomDoubleAtLeast(double min) {
        return randomDoubleImpl(min, MAX_DOUBLE);
    }

    /**
     * Generates a random {@code double}.
     *
     * @param max the maximum result (inclusive); must be >= 0
     * @return a randomly-generated {@code double} whose value is <= {@code max}
     * @throws IllegalArgumentException if {@code max} is < 0
     */
    public static double randomDoubleAtMost(double max) {
        return randomDoubleImpl(0, max);
    }

    /**
     * Generates a random {@code double}.
     *
     * @param min the minimum result (inclusive); must be >= 0 and <= {@code max}
     * @param max the maximum result (inclusive); must be >= 0 and >= {@code min}
     * @return a randomly-generated {@code double} whose value is >= {@code min} and <= {@code max}
     * @throws IllegalArgumentException if {@code min} is < 0, {@code max} < 0, or {@code min} > {@code max}
     */
    public static double randomDoubleBetween(double min, double max) {
        return randomDoubleImpl(min, max);
    }

    /**
     * Selects a random {@code double} from among the members of a specified collection.
     *
     * @param candidate1 the first double eligible for selection
     * @param candidatesN any additional doubles eligible for selection
     * @return a randomly-selected {@code double} among the non-{@code null} members of {@code candidate1...candidatesN};
     * {@link OptionalDouble#empty()} if {@code candidate1} is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code
     * null} elements
     */
    public static OptionalDouble randomDoubleFrom(Double candidate1, Double... candidatesN) {
        final Double selection = RandomSelections.selectRandomFrom(candidate1, candidatesN);
        return (selection == null) ? OptionalDouble.empty() : OptionalDouble.of(selection);
    }

    /**
     * Selects a random {@code double} from among the members of a specified collection.
     *
     * @param candidates all doubles eligible for selection
     * @return a randomly-selected {@code double} among the non-{@code null} members of {@code candidates}; {@link OptionalDouble#empty()} if
     * {@code candidates} is empty, {@code null}, or includes only {@code null} members
     */
    public static OptionalDouble randomDoubleFrom(Iterable<Double> candidates) {
        final Double selection = RandomSelections.selectRandomFrom(candidates);
        return (selection == null) ? OptionalDouble.empty() : OptionalDouble.of(selection);
    }

    /**
     * Generates a random {@code double}.
     *
     * @param min the minimum result (exclusive); must be >= 0
     * @return a randomly-generated {@code double} whose value is > {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static double randomDoubleGreaterThan(double min) {
        return randomDoubleImpl(min + 1, MAX_DOUBLE);
    }

    /**
     * Generates a random {@code double}.
     *
     * @param max the maximum result (exclusive); must be > 0
     * @return a randomly-generated {@code double} whose value is < {@code max}
     * @throws IllegalArgumentException if {@code max} is <= 0
     */
    public static double randomDoubleLessThan(double max) {
        return randomDoubleImpl(0, max - 1);
    }

    /**
     * Generates a random {@code int}.
     *
     * @return a randomly-generated, non-negative {@code int}
     */
    public static int randomInt() {
        return randomIntImpl(0, MAX_INT);
    }

    /**
     * Generates a random {@code int}.
     *
     * @param min the minimum result (inclusive); must be >= 0
     * @return a randomly-generated {@code int} whose value is >= {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static int randomIntAtLeast(int min) {
        return randomIntImpl(min, MAX_INT);
    }

    /**
     * Generates a random {@code int}.
     *
     * @param max the maximum result (inclusive); must be >= 0
     * @return a randomly-generated {@code int} whose value is <= {@code max}
     * @throws IllegalArgumentException if {@code max} is < 0
     */
    public static int randomIntAtMost(int max) {
        return randomIntImpl(0, max);
    }

    /**
     * Generates a random {@code int}.
     *
     * @param min the minimum result (inclusive); must be >= 0 and <= {@code max}
     * @param max the maximum result (inclusive); must be >= 0 and >= {@code min}
     * @return a randomly-generated {@code int} whose value is >= {@code min} and <= {@code max}
     * @throws IllegalArgumentException if {@code min} is < 0, {@code max} < 0, or {@code min} > {@code max}
     */
    public static int randomIntBetween(int min, int max) {
        return randomIntImpl(min, max);
    }

    /**
     * Selects a random {@code int} from among the members of a specified collection.
     *
     * @param candidate1 the first integer eligible for selection
     * @param candidatesN any additional integers eligible for selection
     * @return a randomly-selected {@code int} among the non-{@code null} members of {@code candidate1...candidatesN}; {@link OptionalInt#empty()} if
     * {@code candidate1} is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code null} elements
     */
    public static OptionalInt randomIntFrom(Integer candidate1, Integer... candidatesN) {
        final Integer selection = RandomSelections.selectRandomFrom(candidate1, candidatesN);
        return (selection == null) ? OptionalInt.empty() : OptionalInt.of(selection);
    }

    /**
     * Selects a random {@code int} from among the members of a specified collection.
     *
     * @param candidates all integers eligible for selection
     * @return a randomly-selected {@code int} among the non-{@code null} members of {@code candidates}; {@link OptionalInt#empty()} if
     * {@code candidates} is empty, {@code null}, or includes only {@code null} members
     */
    public static OptionalInt randomIntFrom(Iterable<Integer> candidates) {
        final Integer selection = RandomSelections.selectRandomFrom(candidates);
        return (selection == null) ? OptionalInt.empty() : OptionalInt.of(selection);
    }

    /**
     * Generates a random {@code int}.
     *
     * @param min the minimum result (exclusive); must be >= 0
     * @return a randomly-generated {@code int} whose value is > {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static int randomIntGreaterThan(int min) {
        return randomIntImpl(min + 1, MAX_INT);
    }

    /**
     * Generates a random {@code int}.
     *
     * @param max the maximum result (exclusive); must be > 0
     * @return a randomly-generated {@code int} whose value is < {@code max}
     * @throws IllegalArgumentException if {@code max} is <= 0
     */
    public static int randomIntLessThan(int max) {
        return randomIntImpl(0, max - 1);
    }

    /**
     * Generates a random {@code long}.
     *
     * @return a randomly-generated, non-negative {@code long}
     */
    public static long randomLong() {
        return randomLongImpl(0, MAX_LONG);
    }

    /**
     * Generates a random {@code long}.
     *
     * @param min the minimum result (inclusive); must be >= 0
     * @return a randomly-generated {@code long} whose value is >= {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static long randomLongAtLeast(long min) {
        return randomLongImpl(min, MAX_LONG);
    }

    /**
     * Generates a random {@code long}.
     *
     * @param max the maximum result (inclusive); must be >= 0
     * @return a randomly-generated {@code long} whose value is <= {@code max}
     * @throws IllegalArgumentException if {@code max} is < 0
     */
    public static long randomLongAtMost(long max) {
        return randomLongImpl(0, max);
    }

    /**
     * Generates a random {@code long}.
     *
     * @param min the minimum result (inclusive); must be >= 0 and <= {@code max}
     * @param max the maximum result (inclusive); must be >= 0 and >= {@code min}
     * @return a randomly-generated {@code long} whose value is >= {@code min} and <= {@code max}
     * @throws IllegalArgumentException if {@code min} is < 0, {@code max} < 0, or {@code min} > {@code max}
     */
    public static long randomLongBetween(long min, long max) {
        return randomLongImpl(min, max);
    }

    /**
     * Selects a random {@code long} from among the members of a specified collection.
     *
     * @param candidate1 the first long eligible for selection
     * @param candidatesN any additional longs eligible for selection
     * @return a randomly-selected {@code long} among the non-{@code null} members of {@code candidate1...candidatesN}; {@link OptionalLong#empty()}
     * if {@code candidate1} is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code null} elements
     */
    public static OptionalLong randomLongFrom(Long candidate1, Long... candidatesN) {
        final Long selection = RandomSelections.selectRandomFrom(candidate1, candidatesN);
        return (selection == null) ? OptionalLong.empty() : OptionalLong.of(selection);
    }

    /**
     * Selects a random {@code long} from among the members of a specified collection.
     *
     * @param candidates all longs eligible for selection
     * @return a randomly-selected {@code long} among the non-{@code null} members of {@code candidates}; {@link OptionalLong#empty()} if
     * {@code candidates} is empty, {@code null}, or includes only {@code null} members
     */
    public static OptionalLong randomLongFrom(Iterable<Long> candidates) {
        final Long selection = RandomSelections.selectRandomFrom(candidates);
        return (selection == null) ? OptionalLong.empty() : OptionalLong.of(selection);
    }

    /**
     * Generates a random {@code long}.
     *
     * @param min the minimum result (exclusive); must be >= 0
     * @return a randomly-generated {@code long} whose value is > {@code min}
     * @throws IllegalArgumentException if {@code min} is < 0
     */
    public static long randomLongGreaterThan(long min) {
        return randomLongImpl(min + 1, MAX_LONG);
    }

    /**
     * Generates a random {@code long}.
     *
     * @param max the maximum result (exclusive); must be > 0
     * @return a randomly-generated {@code long} whose value is < {@code max}
     * @throws IllegalArgumentException if {@code max} is <= 0
     */
    public static long randomLongLessThan(long max) {
        return randomLongImpl(0, max - 1);
    }

    private static double randomDoubleImpl(double min, double max) {
        Validate.isTrue(min >= 0, "min must be >= 0");
        Validate.isTrue(max >= 0, "max must be >= 0");

        // Enforce our min/max boundaries.
        final double boundedMin = Math.max(0, Math.min(MAX_DOUBLE, min));
        final double boundedMax = Math.min(MAX_DOUBLE, Math.max(0, max));

        Validate.isTrue(min <= max, "min must be <= max");

        // Add 1 to max to make the bound inclusive rather than exclusive.
        return RandomUtils.nextDouble(boundedMin, boundedMax + 1d);
    }

    private static int randomIntImpl(int min, int max) {
        Validate.isTrue(min >= 0, "min must be >= 0");
        Validate.isTrue(max >= 0, "max must be >= 0");

        // Enforce our min/max boundaries.
        final int boundedMin = Math.max(0, Math.min(MAX_INT, min));
        int boundedMax = Math.min(MAX_INT, Math.max(0, max));

        Validate.isTrue(boundedMin <= boundedMax, "min must be <= max");

        // Add 1 to max to make the bound inclusive rather than exclusive.
        return RandomUtils.nextInt(boundedMin, boundedMax + 1);
    }

    private static long randomLongImpl(long min, long max) {
        Validate.isTrue(min >= 0, "min must be >= 0");
        Validate.isTrue(max >= 0, "max must be >= 0");

        // Enforce our min/max boundaries.
        final long boundedMin = Math.max(0, Math.min(MAX_LONG, min));
        final long boundedMax = Math.min(MAX_LONG, Math.max(0, max));

        Validate.isTrue(boundedMin <= boundedMax, "min must be <= max");

        // Add 1 to max to make the bound inclusive rather than exclusive.
        return RandomUtils.nextLong(boundedMin, boundedMax + 1);
    }
}
