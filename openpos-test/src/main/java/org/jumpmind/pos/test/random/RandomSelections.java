package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.IterableUtils.toList;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import static java.util.stream.Collectors.toList;

import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.jumpmind.pos.util.StreamUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A set of utilities for selecting a random element from among a bounded set of elements.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
public class RandomSelections {
    /**
     * Selects a random member from an enumerated type's full membership.
     *
     * @param <T> the enumerated type
     * @param enumType the enumerated type
     * @return a randomly-selected member of {@code enumType}; {@code null} if {@code enumType} is {@code null}
     */
    public static <T extends Enum<T>> T randomEnum(Class<T> enumType) {
        return (enumType == null) ? null : selectRandomFrom(EnumSet.allOf(enumType));
    }

    /**
     * Selects a random member from a qualified subset of an enumerated type's full membership.
     *
     * @param <T> the enumerated type
     * @param exclusion1 a member of {@code enumType} ineligible for selection
     * @param exclusionsN any additional members of {@code enumType} ineligible for selection
     * @return a randomly-selected member of {@code enumType} which matches none among {@code exclusion1...exclusionsN}; {@code null} if
     * {@code exclusion1} is {@code null} and either {@code exclusionsN} is {@code null} or contains only {@code null} elements, as the type of
     * enumeration will be indeterminate
     */
    @SafeVarargs
    public static <T extends Enum<T>> T randomEnumExcluding(T exclusion1, T... exclusionsN) {
        return ((exclusion1 == null) || (exclusionsN == null))
                ? null
                : selectRandomFrom(EnumSet.complementOf(EnumSet.of(exclusion1, exclusionsN)));
    }

    /**
     * Selects a random member from a qualified subset of an enumerated type's full membership.
     *
     * @param <T> the enumerated type
     * @param exclusions all members of {@code enumType} ineligible for selection
     * @return a randomly-selected member of {@code enumType} which matches none among {@code exclusions}; {@code null} if {@code exclusions} is
     * empty or {@code null}, as the type of enumeration will be indeterminate
     */
    public static <T extends Enum<T>> T randomEnumExcluding(Iterable<T> exclusions) {
        return (IterableUtils.isEmpty(exclusions)) ? null : selectRandomFrom(EnumSet.complementOf(EnumSet.copyOf(toList(exclusions))));
    }

    /**
     * Selects a random member from a qualified subset of an enumerated type's full membership.
     *
     * @param <T> the enumerated type
     * @param inclusion1 a member of {@code enumType} eligible for selection
     * @param inclusionsN any additional members of {@code enumType} eligible for selection
     * @return a randomly-selected member of {@code enumType} which matches one among {@code inclusion1...inclusionsN}; {@code null} if
     * {@code inclusion1} is {@code null} and either {@code inclusionsN} is {@code null} or contains only {@code null} elements
     */
    @SafeVarargs
    public static <T extends Enum<T>> T randomEnumIncluding(T inclusion1, T... inclusionsN) {
        return ((inclusion1 == null) || (inclusionsN == null)) ? null : selectRandomFrom(EnumSet.of(inclusion1, inclusionsN));
    }

    /**
     * Selects a random member from a qualified subset of an enumerated type's full membership.
     *
     * @param <T> the enumerated type
     * @param inclusions all members of {@code enumType} eligible for selection
     * @return a randomly-selected member of {@code enumType} which matches one among {@code inclusions}; {@code null} if {@code inclusions} is
     * empty or {@code null}
     */
    public static <T extends Enum<T>> T randomEnumIncluding(Iterable<T> inclusions) {
        return (IterableUtils.isEmpty(inclusions)) ? null : selectRandomFrom(EnumSet.copyOf(toList(inclusions)));
    }

    /**
     * Selects a random element from among the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param candidate1 the first element eligible for selection
     * @param candidatesN any additional elements eligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidate1...candidatesN}; {@code null} if {@code candidate1}
     * is {@code null} and either {@code candidatesN} is {@code null} or contains only {@code null} elements
     */
    @SafeVarargs
    public static <T> T randomFrom(T candidate1, T... candidatesN) {
        return selectRandomFrom(candidate1, candidatesN);
    }

    /**
     * Selects a random element from among the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param candidates all elements eligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidates}; {@code null} if {@code candidates} is empty,
     * {@code null}, or includes only {@code null} members
     */
    public static <T> T randomFrom(Iterable<T> candidates) {
        return selectRandomFrom(candidates);
    }

    /**
     * Selects a random element from among a qualified subset of the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param exclusions all members of {@code candidates} ineligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidates} which matches none among
     * {@code exclusions}; {@code null} if {@code candidates} is empty, {@code null}, includes only {@code null} members, or includes only members
     * among {@code exclusions}
     */
    public static <T> T randomFromExcluding(Iterable<T> candidates, Iterable<T> exclusions) {
        return selectRandomFromExcluding(candidates, exclusions);
    }

    /**
     * Selects a random element from among a qualified subset of the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param exclusion1 a member of {@code candidates} ineligible for selection
     * @param exclusionsN any additional members of {@code candidates} ineligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidates} which matches none among
     * {@code exclusion1...exclusionsN}; {@code null} if {@code candidates} is empty, {@code null}, includes only {@code null} members, or includes
     * only members among {@code exclusion1...exclusionsN}
     */
    @SafeVarargs
    public static <T> T randomFromExcluding(Iterable<T> candidates, T exclusion1, T... exclusionsN) {
        return selectRandomFromExcluding(candidates, exclusion1, exclusionsN);
    }

    /**
     * Selects a random element from among a qualified subset of the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param inclusions all members of {@code candidates} eligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidates} which matches any among {@code inclusions};
     * {@code null} if {@code candidates} is empty, {@code null}, includes only {@code null} members, or includes no members among {@code inclusions}
     */
    public static <T> T randomFromIncluding(Iterable<T> candidates, Iterable<T> inclusions) {
        return selectRandomFromIncluding(candidates, inclusions);
    }

    /**
     * Selects a random element from among a qualified subset of the non-{@code null} members of a specified collection.
     *
     * @param <T> the type of elements to select from
     * @param inclusion1 a member of {@code candidates} eligible for selection
     * @param inclusionsN any additional members of {@code candidates} eligible for selection
     * @return a randomly-selected member among the non-{@code null} members of {@code candidates} which matches any among
     * {@code inclusion1...inclusionsN}; {@code null} if {@code candidates} is empty, {@code null}, includes only {@code null} members, or includes
     * no members among {@code inclusion1...inclusionsN}
     */
    @SafeVarargs
    public static <T> T randomFromIncluding(Iterable<T> candidates, T inclusion1, T... inclusionsN) {
        return selectRandomFromIncluding(candidates, inclusion1, inclusionsN);
    }

    static <T> T selectRandomFrom(Stream<T> candidates) {
        return (candidates == null) ? null : selectRandomFrom(candidates.collect(toList()));
    }

    @SafeVarargs
    static <T> T selectRandomFrom(T candidate1, T... candidatesN) {
        return selectRandomFrom(StreamUtils.stream(candidate1, candidatesN).collect(toList()));
    }

    static <T> T selectRandomFrom(Iterable<T> candidates) {
        if (IterableUtils.isEmpty(candidates)) return null;

        // Filter out  all null elements before we decide our selection range.
        final Collection<T> nonNullCandidates = IterableUtils.toList(IterableUtils.filteredIterable(candidates, Objects::nonNull));

        final int randomIdx = nextInt(0, nonNullCandidates.size());
        return nonNullCandidates.stream().skip(randomIdx).findFirst().orElse(null);
    }

    @SafeVarargs
    static <T> T selectRandomFromExcluding(Iterable<T> candidates, T exclusion1, T... exclusionsN) {
        return selectRandomFromExcluding(candidates, StreamUtils.stream(exclusion1, exclusionsN).collect(toList()));
    }

    static <T> T selectRandomFromExcluding(Iterable<T> candidates, Iterable<T> exclusions) {
        return selectRandomFrom(CollectionUtils.removeAll(IterableUtils.toList(candidates), IterableUtils.toList(exclusions)));
    }

    static <T> T selectRandomFromIncluding(Iterable<T> candidates, Iterable<T> inclusions) {
        return selectRandomFrom(CollectionUtils.intersection(IterableUtils.toList(candidates), IterableUtils.toList(inclusions)));
    }

    @SafeVarargs
    static <T> T selectRandomFromIncluding(Iterable<T> candidates, T inclusion1, T... inclusionsN) {
        return selectRandomFromExcluding(candidates, StreamUtils.stream(inclusion1, inclusionsN).collect(toList()));
    }
}
