package org.jumpmind.pos.test.params;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.jumpmind.pos.test.random.RandomBigDecimals.randomBigDecimalBetween;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * An {@link ArgumentsProvider} which supplies {@link BigDecimalArgumentsSource}-annotated unit test methods with
 * randomly-generated {@link BigDecimal} arguments configured per constraints expressed on the annotation.
 *
 * @author Jason Weiss
 */
public class BigDecimalArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<BigDecimalArgumentsSource> {
    private BigDecimalArgumentsSource args;

    @Override
    public void accept(BigDecimalArgumentsSource argumentsSource) {
        args = argumentsSource;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        double[] values = args.value();

        return (ArrayUtils.isNotEmpty(values))
                ? provideArgumentsImpl(values)
                : provideRandomArguments();
    }

    private Stream<Arguments> provideArgumentsImpl(double[] values) {
        return Arrays.stream(values).mapToObj(BigDecimal::valueOf).map(Arguments::of);
    }

    private Stream<Arguments> provideRandomArguments() {
        /* The random decimal generator doesn't support negative values (blame Apache), so we've got to get cute here.
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
                    .generate(() -> arguments(randomBigDecimalBetween(negativeMin, negativeMax, args.scale()).negate()))
                    .limit(negativeCount);
        }

        final int positiveCount = args.count() - negativeCount;
        if (positiveCount > 0) {
            final long positiveMin = (args.min() >= 0) ? args.min() : 0;

            positiveArgs = Stream
                    .generate(() -> arguments(randomBigDecimalBetween(positiveMin, args.max(), args.scale())))
                    .limit(positiveCount);
        }
        return Stream.concat(negativeArgs, positiveArgs);
    }
}
