package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;

import static java.util.stream.Collectors.toList;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.stream.Stream;

/**
 * A set of utilities generating random {@link String} values for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
public class RandomStrings {
    /**
     * @see RandomStringUtils#randomAlphabetic(int)
     */
    public static String randomAlphabetic(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    /**
     * @see RandomStringUtils#randomAlphabetic(int, int)
     */
    public static String randomAlphabetic(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.randomAlphabetic(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * @see RandomStringUtils#randomAlphanumeric(int)
     */
    public static String randomAlphanumeric(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * @see RandomStringUtils#randomAlphanumeric(int, int)
     */
    public static String randomAlphanumeric(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.randomAlphanumeric(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * @see RandomStringUtils#randomNumeric(int)
     */
    public static String randomNumeric(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    /**
     * @see RandomStringUtils#randomNumeric(int, int)
     */
    public static String randomNumeric(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.randomNumeric(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Selects a random string from among the non-{@code null} members of a specified collection.
     *
     * @param candidate1 the first string eligible for selection
     * @param candidatesN any additional strings eligible for selection
     * @return a randomly-selected string among the non-{@code null} members of {@code candidate1...candidatesN}; {@code null} if {@code candidate1}
     * is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code null} elements
     */
    public static String randomStringFrom(String candidate1, String... candidatesN) {
        return RandomSelections.randomFrom(candidate1, candidatesN);
    }

    /**
     * Selects a random string from among the non-{@code null} members of a specified collection.
     *
     * @param candidates all strings eligible for selection
     * @return a randomly-selected string among the non-{@code null} members of {@code candidates}; {@code null} if {@code candidates} is empty,
     * {@code null}, or includes only {@code null} members
     */
    public static String randomStringFrom(Iterable<String> candidates) {
        return RandomSelections.randomFrom(candidates);
    }

    /**
     * Generates a list of random strings.  This method can be referenced as a {@link @MethodSource} of qualifying unit tests.
     *
     * @return a list of random strings
     */
    public static List<String> randomStrings() {
        // TODO Relocate to TestRandomProviders when junit-jupiter-params dependency is added.
        return Stream.generate(() -> RandomStringUtils.randomAlphabetic(8)).limit(5).collect(toList());
    }
}
