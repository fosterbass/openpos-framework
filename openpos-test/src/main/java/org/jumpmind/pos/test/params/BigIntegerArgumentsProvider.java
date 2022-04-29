package org.jumpmind.pos.test.params;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.jumpmind.pos.test.random.RandomBigIntegers.randomBigIntegerBetween;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * An {@link ArgumentsProvider} which supplies {@link BigIntegerArgumentsSource}-annotated unit test methods with
 * randomly-generated {@link BigInteger} arguments configured per constraints expressed on the annotation.
 *
 * @author Jason Weiss
 */
public class BigIntegerArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<BigIntegerArgumentsSource> {
    private BigIntegerArgumentsSource args;

    @Override
    public void accept(BigIntegerArgumentsSource argumentsSource) {
        args = argumentsSource;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        int[] values = args.value();

        return (ArrayUtils.isNotEmpty(values))
                ? provideArgumentsImpl(values)
                : provideRandomArguments();
    }

    private Stream<Arguments> provideArgumentsImpl(int[] values) {
        return Arrays.stream(values).mapToObj(BigInteger::valueOf).map(Arguments::of);
    }

    private Stream<Arguments> provideRandomArguments() {
        /* The random integer generator doesn't support negative values (blame Apache), so we've got to get cute here.
         * If the min and max are both negative, all of our generated values will be negative.  If the minimum is
         * negative but the maximum is not, we'll split the arguments: half-negative, half-positive. */

        Stream<Arguments> negativeArgs = Stream.empty();
        Stream<Arguments> positiveArgs = Stream.empty();
        int negativeCount = 0;

        if (args.min() < 0) {
            negativeCount = (args.max() <= 0) ? args.count() : args.count() / 2;
            final long negativeMin = (args.max() <= 0) ? args.max() * -1 : 0;
            final long negativeMax = args.min() * -1;

            negativeArgs = Stream
                    .generate(() -> arguments(randomBigIntegerBetween(negativeMin, negativeMax).negate()))
                    .limit(negativeCount);
        }

        final int positiveCount = args.count() - negativeCount;
        if (positiveCount > 0) {
            final long positiveMin = (args.min() >= 0) ? args.min() : 0;

            positiveArgs = Stream
                    .generate(() -> arguments(randomBigIntegerBetween(positiveMin, args.max())))
                    .limit(positiveCount);
        }
        return Stream.concat(negativeArgs, positiveArgs);
    }
}
