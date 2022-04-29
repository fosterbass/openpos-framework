package org.jumpmind.pos.util;

import static lombok.AccessLevel.PRIVATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A set of utilities for working with streams.
 *
 * @author Jason Weiss
 */
@RequiredArgsConstructor(access = PRIVATE)
@Slf4j
public class StreamUtils {
    /**
     * Returns a stream concatenating the first argument and any additional arguments.
     * <p>
     * This utility is useful for dealing with methods attempting to work around Java's not-always-desirable "I'll accept no argument for a
     * variable-length parameter" accommodation by requiring that at least one argument be supplied for that parameter, even if that single argument
     * is an explicit {@code null}.
     * <p>
     * Note that all {@code null} elements among {@code first} and {@code others} will be preserved in the result stream.
     *
     * @param <T> the type of objects to be streamed
     * @param first the first argument
     * @param others any additional arguments
     * @return a stream concatenating all elements among {@code first} and {@code others}
     */
    @SafeVarargs
    public static <T> Stream<T> stream(T first, T... others) {
        // TODO Java 9 has Optional.stream() we can make use of here.

        final Stream<T> otherStream = (ArrayUtils.isEmpty(others)) ? Stream.empty() : Arrays.stream(others);
        return Stream.concat(Stream.of(first), otherStream);
    }

    /**
     * Returns a stream concatenating the first {@code int} argument and any additional {@code int} arguments.
     * <p>
     * This utility is useful for dealing with methods attempting to work around Java's not-always-desirable "I'll accept no argument for a
     * variable-length parameter" accommodation by requiring that at least one argument be supplied for that parameter.
     *
     * @param first the first argument
     * @param others any additional arguments
     * @return an {@link IntStream} concatenating all {@code ints} among {@code first} and {@code others}
     */
    public static IntStream stream(int first, int... others) {
        final IntStream otherStream = (ArrayUtils.isEmpty(others)) ? IntStream.empty() : Arrays.stream(others);
        return IntStream.concat(IntStream.of(first), otherStream);
    }

    /**
     * Returns a stream concatenating the first {@code long} argument and any additional {@code long} arguments.
     * <p>
     * This utility is useful for dealing with methods attempting to work around Java's not-always-desirable "I'll accept no argument for a
     * variable-length parameter" accommodation by requiring that at least one argument be supplied for that parameter.
     *
     * @param first the first argument
     * @param others any additional arguments
     * @return a {@link LongStream} concatenating all {@code longs} among {@code first} and {@code others}
     */
    public static LongStream stream(long first, long... others) {
        final LongStream otherStream = (ArrayUtils.isEmpty(others)) ? LongStream.empty() : Arrays.stream(others);
        return LongStream.concat(LongStream.of(first), otherStream);
    }

    /**
     * Returns a stream concatenating the first {@code double} argument and any additional {@code double} arguments.
     * <p>
     * This utility is useful for dealing with methods attempting to work around Java's not-always-desirable "I'll accept no argument for a
     * variable-length parameter" accommodation by requiring that at least one argument be supplied for that parameter.
     *
     * @param first the first argument
     * @param others any additional arguments
     * @return a {@link DoubleStream} concatenating all {@code doubles} among {@code first} and {@code others}
     */
    public static DoubleStream stream(double first, double... others) {
        final DoubleStream otherStream = (ArrayUtils.isEmpty(others)) ? DoubleStream.empty() : Arrays.stream(others);
        return DoubleStream.concat(DoubleStream.of(first), otherStream);
    }
}
