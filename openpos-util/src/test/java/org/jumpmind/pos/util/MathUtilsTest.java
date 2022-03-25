package org.jumpmind.pos.util;

import static org.jumpmind.pos.test.random.RandomBigDecimals.randomBigDecimalAtMost;
import static org.jumpmind.pos.test.random.RandomBigDecimals.randomBigDecimalBetween;
import static org.jumpmind.pos.test.random.RandomBigIntegers.randomBigIntegerAtMost;
import static org.jumpmind.pos.test.random.RandomBigIntegers.randomBigIntegerBetween;
import static org.jumpmind.pos.test.random.RandomPrimitives.*;
import static org.jumpmind.pos.test.random.RandomSelections.randomEnumExcluding;

import static org.apache.commons.lang3.math.NumberUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static java.math.RoundingMode.UNNECESSARY;
import static java.util.Arrays.stream;

import org.jumpmind.pos.test.params.BigDecimalArgumentsSource;
import org.jumpmind.pos.test.params.BigIntegerArgumentsSource;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.stream.Stream;

/**
 * A set of unit tests for the {@link MathUtils} class.
 */
class MathUtilsTest {
    @ParameterizedTest
    @BigDecimalArgumentsSource({-1.5, -2.25, -3})
    void abs_shouldReturnAbs_whenNegative(BigDecimal negativeVal) {
        final BigDecimal expected = BigDecimal.valueOf(negativeVal.doubleValue() * -1);

        assertThat(MathUtils.abs(expected)).describedAs("abs(val < 0) = -val").isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-1, -2, -3})
    void abs_shouldReturnAbs_whenNegative(BigInteger negativeVal) {
        final BigInteger expected = BigInteger.valueOf(negativeVal.intValue() * -1);

        assertThat(MathUtils.abs(expected)).describedAs("abs(val < 0) = -val").isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.5, -2.25, -3})
    void abs_shouldReturnAbs_whenNegative(double negativeVal) {
        assertThat(MathUtils.abs(negativeVal)).describedAs("abs(val < 0) = -val").isEqualTo(negativeVal * -1);
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.5f, -2.25f, -3})
    void abs_shouldReturnAbs_whenNegative(float negativeVal) {
        assertThat(MathUtils.abs(negativeVal)).describedAs("abs(val < 0) = -val").isEqualTo(negativeVal * -1);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -2, -3})
    void abs_shouldReturnAbs_whenNegative(long negativeVal) {
        assertThat(MathUtils.abs(negativeVal)).describedAs("abs(val < 0) = -val").isEqualTo(negativeVal * -1);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -3})
    void abs_shouldReturnAbs_whenNegative(int negativeVal) {
        assertThat(MathUtils.abs(negativeVal)).describedAs("abs(val < 0) = -val").isEqualTo(negativeVal * -1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-1, -2, -3})
    void abs_shouldReturnAbs_whenNegative(short negativeVal) {
        assertThat(MathUtils.abs(negativeVal))
                .describedAs("abs(val < 0) = -val")
                .isEqualTo((short) (negativeVal * -1));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-1, -2, -3})
    void abs_shouldReturnAbs_whenNegative(byte negativeVal) {
        assertThat(MathUtils.abs(negativeVal))
                .describedAs("abs(val < 0) = -val")
                .isEqualTo((byte) (negativeVal * -1));
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({0, 1.5, 2.25, 3})
    void abs_shouldReturnArg_whenNonNegative(BigDecimal nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({0, 1, 2, 3})
    void abs_shouldReturnArg_whenNonNegative(BigInteger nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 1.5, 2.25, 3})
    void abs_shouldReturnArg_whenNonNegative(double nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(floats = {0, 1.5f, 2.25f, 3})
    void abs_shouldReturnArg_whenNonNegative(float nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3})
    void abs_shouldReturnArg_whenNonNegative(long nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void abs_shouldReturnArg_whenNonNegative(int nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, 1, 2, 3})
    void abs_shouldReturnArg_whenNonNegative(short nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, 2, 3})
    void abs_shouldReturnArg_whenNonNegative(byte nonNegativeVal) {
        assertThat(MathUtils.abs(nonNegativeVal)).describedAs("abs(val >= 0) = val").isEqualTo(nonNegativeVal);
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(BigDecimal nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(BigInteger nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Double nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Float nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Long nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Integer nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Short nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void abs_shouldReturnZero_whenNull(Byte nullVal) {
        assertThat(MathUtils.abs(nullVal)).describedAs("abs(null) = 0").isZero();
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenBigDecimalsAreNonNull() {
        final BigDecimal[] values = Stream.generate(() -> randomBigDecimalAtMost(100))
                .limit(5)
                .toArray(BigDecimal[]::new);

        final BigDecimal expected = stream(values).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        assertThat(MathUtils.addAll(values))
                .describedAs("addAll(vals...) == sum(vals)")
                .isEqualByComparingTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenBigIntegersAreNonNull() {
        final BigInteger[] values = Stream.generate(() -> randomBigIntegerAtMost(100))
                .limit(5)
                .toArray(BigInteger[]::new);

        final BigInteger expected = stream(values).reduce(BigInteger::add).orElse(BigInteger.ZERO);

        assertThat(MathUtils.addAll(values))
                .describedAs("addAll(vals...) == sum(vals)")
                .isEqualByComparingTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenBytesAreNonNull() {
        final Byte[] values = Stream.generate(() -> (byte) randomIntAtMost(5)).limit(5).toArray(Byte[]::new);
        final Byte expected = stream(values).reduce((v1, v2) -> (byte) (v1 + v2)).orElse(BYTE_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenDoublesAreNonNull() {
        final Double[] values = Stream.generate(() -> randomDoubleAtMost(100)).limit(5).toArray(Double[]::new);
        final Double expected = stream(values).reduce(Double::sum).orElse(DOUBLE_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenFloatsAreNonNull() {
        final Float[] values = Stream.generate(() -> randomFloatAtMost(100)).limit(5).toArray(Float[]::new);
        final Float expected = stream(values).reduce(Float::sum).orElse(FLOAT_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenIntsAreNonNull() {
        final Integer[] values = Stream.generate(() -> randomIntAtMost(100)).limit(5).toArray(Integer[]::new);
        final Integer expected = stream(values).reduce(Integer::sum).orElse(INTEGER_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenLongsAreNonNull() {
        final Long[] values = Stream.generate(() -> randomLongAtMost(100)).limit(5).toArray(Long[]::new);
        final Long expected = stream(values).reduce(Long::sum).orElse(LONG_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @RepeatedTest(5)
    void addAll_shouldAddAll_whenShortsAreNonNull() {
        final Short[] values = Stream.generate(() -> (short) randomIntAtMost(5)).limit(5).toArray(Short[]::new);
        final Short expected = stream(values).reduce((v1, v2) -> (short) (v1 + v2)).orElse(SHORT_ZERO);

        assertThat(MathUtils.addAll(values)).describedAs("addAll(vals...) == sum(vals)").isEqualTo(expected);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(BigDecimal single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(BigInteger single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(double single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(float single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(long single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(int single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(short single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void addAll_shouldReturnArg_whenOnlyOneArg(byte single) {
        assertThat(MathUtils.addAll(single)).describedAs("addAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(BigDecimal nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(BigInteger nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Double nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Float nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Long nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Integer nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Short nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenArrayOfNulls(Byte nullVal) {
        assertThat(MathUtils.addAll(nullVal, nullVal, nullVal)).describedAs("addAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(BigDecimal[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(BigInteger[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Double[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Float[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Long[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Integer[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Short[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void addAll_shouldReturnZero_whenNull(Byte[] nullVals) {
        assertThat(MathUtils.addAll(nullVals)).describedAs("addAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldAdd_whenArgsAreNonNull(BigDecimal arg1) {
        final BigDecimal arg2 = arg1.multiply(randomBigDecimalAtMost(5, 3));
        final BigDecimal expected = arg1.add(arg2);

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void add_shouldAdd_whenArgsAreNonNull(BigInteger arg1) {
        final BigInteger arg2 = arg1.multiply(randomBigIntegerAtMost(5));
        final BigInteger expected = BigInteger.valueOf(arg1.intValue() + arg2.intValue());

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldAdd_whenArgsAreNonNull(double arg1) {
        final double arg2 = arg1 * randomDoubleAtMost(5);

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo(arg1 + arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void add_shouldAdd_whenArgsAreNonNull(float arg1) {
        final float arg2 = arg1 * randomFloatAtMost(5);

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo(arg1 + arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldAdd_whenArgsAreNonNull(long arg1) {
        final long arg2 = arg1 * randomLongAtMost(5);

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo(arg1 + arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldAdd_whenArgsAreNonNull(int arg1) {
        final int arg2 = arg1 * randomIntAtMost(5);

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo(arg1 + arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldAdd_whenArgsAreNonNull(short arg1) {
        final short arg2 = (short) (arg1 * randomIntAtMost(5));

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo((short) (arg1 + arg2));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldAdd_whenArgsAreNonNull(byte arg1) {
        final byte arg2 = (byte) (arg1 * randomIntAtMost(5));

        assertThat(MathUtils.add(arg1, arg2))
                .describedAs("add(arg1, arg2) = arg1 + arg2")
                .isEqualTo((byte) (arg1 + arg2));
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldReturnArg1_whenArg2IsNull(BigDecimal arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg1_whenArg2IsNull(BigInteger arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldReturnArg1_whenArg2IsNull(double arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void add_shouldReturnArg1_whenArg2IsNull(float arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg1_whenArg2IsNull(long arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg1_whenArg2IsNull(int arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg1_whenArg2IsNull(short arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg1_whenArg2IsNull(byte arg1) {
        assertThat(MathUtils.add(arg1, null)).describedAs("add(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldReturnArg2_whenArg1IsNull(BigDecimal arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg2_whenArg1IsNull(BigInteger arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void add_shouldReturnArg2_whenArg1IsNull(double arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void add_shouldReturnArg2_whenArg1IsNull(float arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg2_whenArg1IsNull(long arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg2_whenArg1IsNull(int arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg2_whenArg1IsNull(short arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void add_shouldReturnArg2_whenArg1IsNull(byte arg2) {
        assertThat(MathUtils.add(null, arg2)).describedAs("add(null, *) = *").isEqualTo(arg2);
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(BigDecimal nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(BigInteger nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Double nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Float nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Long nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Integer nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Short nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void add_shouldReturnZero_whenBothArgsAreNull(Byte nullVal) {
        assertThat(MathUtils.add(nullVal, nullVal)).describedAs("add(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void divide_shouldDivide_whenArgsAreNonNull(BigDecimal arg1) {
        final BigDecimal arg2 = randomBigDecimalBetween(1, 10);
        final RoundingMode roundingMode = randomEnumExcluding(UNNECESSARY);
        final BigDecimal expected = arg1.divide(arg2, roundingMode);

        assertThat(MathUtils.divide(arg1, arg2, roundingMode))
                .describedAs("divide(arg1, arg2, *) = arg1 / arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldDivide_whenArgsAreNonNull(BigInteger arg1) {
        final BigInteger arg2 = randomBigIntegerBetween(1, 10);
        final BigInteger expected = arg1.divide(arg2);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void divide_shouldDivide_whenArgsAreNonNull(double arg1) {
        final double arg2 = randomDoubleBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 / arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void divide_shouldDivide_whenArgsAreNonNull(float arg1) {
        final float arg2 = randomFloatBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 / arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldDivide_whenArgsAreNonNull(long arg1) {
        final long arg2 = randomLongBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 / arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldDivide_whenArgsAreNonNull(int arg1) {
        final int arg2 = randomIntBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 / arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldDivide_whenArgsAreNonNull(short arg1) {
        final short arg2 = (short) randomIntBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo((short) (arg1 / arg2));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldDivide_whenArgsAreNonNull(byte arg1) {
        final byte arg2 = (byte) randomIntBetween(1, 10);

        assertThat(MathUtils.divide(arg1, arg2))
                .describedAs("divide(arg1, arg2) = arg1 / arg2")
                .isEqualTo((byte) (arg1 / arg2));
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(BigDecimal nullVal) {
        assertThat(MathUtils.divide(nullVal, randomBigDecimalAtMost(30), randomEnumExcluding(UNNECESSARY)))
                .describedAs("divide(null, *, *) = 0")
                .isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(BigInteger nullVal) {
        assertThat(MathUtils.divide(nullVal, randomBigIntegerAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Double nullVal) {
        assertThat(MathUtils.divide(nullVal, randomDoubleAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Float nullVal) {
        assertThat(MathUtils.divide(nullVal, randomFloatAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Long nullVal) {
        assertThat(MathUtils.divide(nullVal, randomLongAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Integer nullVal) {
        assertThat(MathUtils.divide(nullVal, randomIntAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Short nullVal) {
        assertThat(MathUtils.divide(nullVal, (short) randomIntAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldReturnZero_whenArg1IsNull(Byte nullVal) {
        assertThat(MathUtils.divide(nullVal, (byte) randomIntAtMost(30))).describedAs("divide(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(BigDecimal nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomBigDecimalAtMost(30), nullVal,
                randomEnumExcluding(RoundingMode.UNNECESSARY)), "divide(*, null, *) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(BigInteger nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomBigIntegerAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Double nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomDoubleAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Float nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomFloatAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Long nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomLongAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Integer nullVal) {
        assertThatThrownBy(() -> MathUtils.divide(randomIntAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Short nullVal) {
        assertThatThrownBy(() -> MathUtils.divide((short) randomIntAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void divide_shouldThrow_whenArg2IsNull(Byte nullVal) {
        assertThatThrownBy(() -> MathUtils.divide((byte) randomIntAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void divide_shouldThrow_whenArg2IsZero(BigDecimal arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, BigDecimal.ZERO, randomEnumExcluding(RoundingMode.UNNECESSARY)),
                "divide(*, 0, *) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldThrow_whenArg2IsZero(BigInteger arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, BigInteger.ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void divide_shouldThrow_whenArg2IsZero(double arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, DOUBLE_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void divide_shouldThrow_whenArg2IsZero(float arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, FLOAT_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldThrow_whenArg2IsZero(long arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, LONG_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldThrow_whenArg2IsZero(int arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, INTEGER_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldThrow_whenArg2IsZero(short arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, SHORT_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void divide_shouldThrow_whenArg2IsZero(byte arg1) {
        assertThatThrownBy(() -> MathUtils.divide(arg1, BYTE_ZERO), "divide(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(BigDecimal arg1) {
        final BigDecimal arg2 = randomBigDecimalBetween(1, 10);
        final BigDecimal expected = arg1.remainder(arg2).abs();

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 % arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(BigInteger arg1) {
        final BigInteger arg2 = randomBigIntegerBetween(1, 10);
        final BigInteger expected = arg1.remainder(arg2).abs();

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 % arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(double arg1) {
        final double arg2 = randomDoubleBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 % arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(float arg1) {
        final float arg2 = randomFloatBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 % arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(long arg1) {
        final long arg2 = randomLongBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 % arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(int arg1) {
        final int arg2 = randomIntBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo(arg1 % arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(short arg1) {
        final short arg2 = (short) randomIntBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo((short) (arg1 % arg2));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldModulus_whenArgsAreNonNull(byte arg1) {
        final byte arg2 = (byte) randomIntBetween(1, 10);

        assertThat(MathUtils.modulus(arg1, arg2))
                .describedAs("modulus(arg1, arg2) = arg1 / arg2")
                .isEqualTo((byte) (arg1 % arg2));
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(BigDecimal nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomBigDecimalAtMost(30)))
                .describedAs("modulus(null, *) = 0")
                .isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(BigInteger nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomBigIntegerAtMost(30))).describedAs("modulus(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Double nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomDoubleAtMost(30))).describedAs("modulus(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Float nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomFloatAtMost(30))).describedAs("modulus(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Long nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomLongAtMost(30))).describedAs("modulus(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Integer nullVal) {
        assertThat(MathUtils.modulus(nullVal, randomIntAtMost(30))).describedAs("modulus(null, *) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Short nullVal) {
        assertThat(MathUtils.modulus(nullVal, (short) randomIntAtMost(30)))
                .describedAs("modulus(null, *) = 0")
                .isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldReturnZero_whenArg1IsNull(Byte nullVal) {
        assertThat(MathUtils.modulus(nullVal, (byte) randomIntAtMost(30)))
                .describedAs("modulus(null, *) = 0")
                .isZero();
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(BigDecimal nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomBigDecimalAtMost(30), nullVal), "divide(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(BigInteger nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomBigIntegerAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Double nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomDoubleAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Float nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomFloatAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Long nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomLongAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Integer nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus(randomIntAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Short nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus((short) randomIntAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @NullSource
    void modulus_shouldThrow_whenArg2IsNull(Byte nullVal) {
        assertThatThrownBy(() -> MathUtils.modulus((byte) randomIntAtMost(30), nullVal), "modulus(*, null) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void modulus_shouldThrow_whenArg2IsZero(BigDecimal arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, BigDecimal.ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldThrow_whenArg2IsZero(BigInteger arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, BigInteger.ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void modulus_shouldThrow_whenArg2IsZero(double arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, DOUBLE_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void modulus_shouldThrow_whenArg2IsZero(float arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, FLOAT_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldThrow_whenArg2IsZero(long arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, LONG_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldThrow_whenArg2IsZero(int arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, INTEGER_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldThrow_whenArg2IsZero(short arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, SHORT_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void modulus_shouldThrow_whenArg2IsZero(byte arg1) {
        assertThatThrownBy(() -> MathUtils.modulus(arg1, BYTE_ZERO), "modulus(*, 0) -> ex")
                .isInstanceOf(ArithmeticException.class);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenBigDecimalsAreNonNull() {
        final BigDecimal[] values = Stream.generate(() -> randomBigDecimalAtMost(100))
                .limit(5)
                .toArray(BigDecimal[]::new);

        final BigDecimal expected = stream(values).reduce(BigDecimal::multiply).orElse(BigDecimal.ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualByComparingTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenBigIntegersAreNonNull() {
        final BigInteger[] values = Stream.generate(() -> randomBigIntegerAtMost(100))
                .limit(5)
                .toArray(BigInteger[]::new);

        final BigInteger expected = stream(values).reduce(BigInteger::multiply).orElse(BigInteger.ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualByComparingTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenBytesAreNonNull() {
        final Byte[] values = Stream.generate(() -> (byte) randomIntAtMost(5)).limit(3).toArray(Byte[]::new);
        final Byte expected = stream(values).reduce((v1, v2) -> (byte) (v1 * v2)).orElse(BYTE_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenDoublesAreNonNull() {
        final Double[] values = Stream.generate(() -> randomDoubleAtMost(100)).limit(5).toArray(Double[]::new);
        final Double expected = stream(values).reduce((v1, v2) -> v1 * v2).orElse(DOUBLE_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenFloatsAreNonNull() {
        final Float[] values = Stream.generate(() -> randomFloatAtMost(20)).limit(5).toArray(Float[]::new);
        final Float expected = stream(values).reduce((v1, v2) -> v1 * v2).orElse(FLOAT_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenIntsAreNonNull() {
        final Integer[] values = Stream.generate(() -> randomIntAtMost(20)).limit(5).toArray(Integer[]::new);
        final Integer expected = stream(values).reduce((v1, v2) -> v1 * v2).orElse(INTEGER_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenLongsAreNonNull() {
        final Long[] values = Stream.generate(() -> randomLongAtMost(100)).limit(5).toArray(Long[]::new);
        final Long expected = stream(values).reduce((v1, v2) -> v1 * v2).orElse(LONG_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @RepeatedTest(5)
    void multiplyAll_shouldMultiplyAll_whenShortsAreNonNull() {
        final Short[] values = Stream.generate(() -> (short) randomIntAtMost(5)).limit(5).toArray(Short[]::new);
        final Short expected = stream(values).reduce((v1, v2) -> (short) (v1 * v2)).orElse(SHORT_ZERO);

        assertThat(MathUtils.multiplyAll(values))
                .describedAs("multiplyAll(vals...) == product(vals)")
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(BigDecimal single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(BigInteger single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(double single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(float single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(long single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(int single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(short single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void multiplyAll_shouldReturnArg_whenOnlyOneArg(byte single) {
        assertThat(MathUtils.multiplyAll(single)).describedAs("multiplyAll(single) == single").isEqualTo(single);
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(BigDecimal nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(BigInteger nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Double nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Float nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Long nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Integer nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Short nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenArrayOfNulls(Byte nullVal) {
        assertThat(MathUtils.multiplyAll(nullVal, nullVal, nullVal)).describedAs("multiplyAll(null...) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(BigDecimal[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(BigInteger[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Double[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Float[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Long[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Integer[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Short[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiplyAll_shouldReturnZero_whenNull(Byte[] nullVals) {
        assertThat(MathUtils.multiplyAll(nullVals)).describedAs("multiplyAll(null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(BigDecimal arg1) {
        final BigDecimal arg2 = arg1.add(randomBigDecimalAtMost(5, 3));
        final BigDecimal expected = arg1.multiply(arg2);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(BigInteger arg1) {
        final BigInteger arg2 = arg1.add(randomBigIntegerAtMost(5));
        final BigInteger expected = arg1.multiply(arg2);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(double arg1) {
        final double arg2 = arg1 + randomDoubleAtMost(5);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo(arg1 * arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(float arg1) {
        final float arg2 = arg1 + randomFloatAtMost(5);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo(arg1 * arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(long arg1) {
        final long arg2 = arg1 + randomLongAtMost(5);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo(arg1 * arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(int arg1) {
        final int arg2 = arg1 + randomIntAtMost(5);

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo(arg1 * arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(short arg1) {
        final short arg2 = (short) (arg1 + randomIntAtMost(5));

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo((short) (arg1 * arg2));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void multiply_shouldMultiply_whenArgsAreNonNull(byte arg1) {
        final byte arg2 = (byte) (arg1 + randomIntAtMost(5));

        assertThat(MathUtils.multiply(arg1, arg2))
                .describedAs("multiply(arg1, arg2) = arg1 * arg2")
                .isEqualTo((byte) (arg1 * arg2));
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(BigDecimal nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomBigDecimalAtMost(30)))
                .describedAs("multiply(null, *) = 0")
                .isZero();

        assertThat(MathUtils.multiply(randomBigDecimalAtMost(30), nullVal))
                .describedAs("multiply(*, null) = 0")
                .isZero();

        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(BigInteger nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomBigIntegerAtMost(30)))
                .describedAs("multiply(null, *) = 0")
                .isZero();

        assertThat(MathUtils.multiply(randomBigIntegerAtMost(30), nullVal))
                .describedAs("multiply(*, null) = 0")
                .isZero();

        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Double nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomDoubleAtMost(30))).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply(randomDoubleAtMost(30), nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Float nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomFloatAtMost(30))).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply(randomFloatAtMost(30), nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Long nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomLongAtMost(30))).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply(randomLongAtMost(30), nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Integer nullVal) {
        assertThat(MathUtils.multiply(nullVal, randomIntAtMost(30))).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply(randomIntAtMost(30), nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Short nullVal) {
        assertThat(MathUtils.multiply(nullVal, (short) 4)).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply((short) 4, nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void multiply_shouldReturnZero_whenEitherArgIsNull(Byte nullVal) {
        assertThat(MathUtils.multiply(nullVal, (byte) 4)).describedAs("multiply(null, *) = 0").isZero();
        assertThat(MathUtils.multiply((byte) 4, nullVal)).describedAs("multiply(*, null) = 0").isZero();
        assertThat(MathUtils.multiply(nullVal, nullVal)).describedAs("multiply(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void negate_shouldReturnNegation_whenNonNull(BigDecimal val) {
        final BigDecimal expected = BigDecimal.valueOf(val.doubleValue() * -1);

        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void negate_shouldReturnNegation_whenNonNull(BigInteger val) {
        final BigInteger expected = BigInteger.valueOf(val.intValue() * -1);

        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void negate_shouldReturnNegation_whenNonNull(Double val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo(val * -1d);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void negate_shouldReturnNegation_whenNonNull(Float val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo(val * -1f);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void negate_shouldReturnNegation_whenNonNull(Long val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo(val * -1L);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void negate_shouldReturnNegation_whenNonNull(Integer val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo(val * -1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void negate_shouldReturnNegation_whenNonNull(Short val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo((short) (val * -1));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void negate_shouldReturnNegation_whenNonNull(Byte val) {
        assertThat(MathUtils.negate(val)).describedAs("negate(val) = -val").isEqualTo((byte) (val * -1));
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(BigDecimal nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(BigInteger nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Double nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Float nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Long nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Integer nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Short nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negate_shouldReturnZero_whenNull(Byte nullVal) {
        assertThat(MathUtils.negate(nullVal)).describedAs("negate(null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({0, -1.5, -2.25, -3})
    void negative_shouldReturnArg_whenNonPositive(BigDecimal nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({0, -1, -2, -3})
    void negative_shouldReturnArg_whenNonPositive(BigInteger nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1.5, -2.25, -3})
    void negative_shouldReturnArg_whenNonPositive(double nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(floats = {0, -1.5f, -2.25f, -3})
    void negative_shouldReturnArg_whenNonPositive(float nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -2, -3})
    void negative_shouldReturnArg_whenNonPositive(long nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -2, -3})
    void negative_shouldReturnArg_whenNonPositive(int nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, -1, -2, -3})
    void negative_shouldReturnArg_whenNonPositive(short nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, -1, -2, -3})
    void negative_shouldReturnArg_whenNonPositive(byte nonPositiveVal) {
        assertThat(MathUtils.negative(nonPositiveVal))
                .describedAs("negative(val <= 0) = val")
                .isEqualTo(nonPositiveVal);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({1.5, 2.25, 3})
    void negative_shouldReturnNegation_whenPositive(BigDecimal positiveVal) {
        final BigDecimal expected = BigDecimal.valueOf(positiveVal.doubleValue() * -1);

        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({1, 2, 3})
    void negative_shouldReturnNegation_whenPositive(BigInteger positiveVal) {
        final BigInteger expected = BigInteger.valueOf(positiveVal.intValue() * -1);

        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.5, 2.25, 3})
    void negative_shouldReturnNegation_whenPositive(double positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo(positiveVal * -1d);
    }

    @ParameterizedTest
    @ValueSource(floats = {1.5f, 2.25f, 3})
    void negative_shouldReturnNegation_whenPositive(float positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo(positiveVal * -1f);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3})
    void negative_shouldReturnNegation_whenPositive(long positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo(positiveVal * -1L);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void negative_shouldReturnNegation_whenPositive(int positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo(positiveVal * -1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {1, 2, 3})
    void negative_shouldReturnNegation_whenPositive(short positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo((short) (positiveVal * -1));
    }

    @ParameterizedTest
    @ValueSource(bytes = {1, 2, 3})
    void negative_shouldReturnNegation_whenPositive(byte positiveVal) {
        assertThat(MathUtils.negative(positiveVal))
                .describedAs("negative(val > 0) = -val")
                .isEqualTo((byte) (positiveVal * -1));
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(BigDecimal nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(BigInteger nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Double nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Float nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Long nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Integer nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Short nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void negative_shouldReturnZero_whenNull(Byte nullVal) {
        assertThat(MathUtils.negative(nullVal)).describedAs("negative(null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(BigDecimal arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(BigInteger arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(double arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(float arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(long arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(int arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(short arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnArg1_whenArg2IsNull(byte arg1) {
        assertThat(MathUtils.subtract(arg1, null)).describedAs("subtract(*, null) = *").isEqualTo(arg1);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(BigDecimal arg2) {
        final BigDecimal expected = BigDecimal.valueOf(arg2.doubleValue() * -1);

        assertThat(MathUtils.subtract(null, arg2))
                .describedAs("subtract(null, *) = -*")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(BigInteger arg2) {
        final BigInteger expected = BigInteger.valueOf(arg2.intValue() * -1);

        assertThat(MathUtils.subtract(null, arg2))
                .describedAs("subtract(null, *) = -*")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(double arg2) {
        assertThat(MathUtils.subtract(null, arg2)).describedAs("subtract(null, *) = -*").isEqualTo(arg2 * -1d);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(float arg2) {
        assertThat(MathUtils.subtract(null, arg2)).describedAs("subtract(null, *) = -*").isEqualTo(arg2 * -1f);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(long arg2) {
        assertThat(MathUtils.subtract(null, arg2)).describedAs("subtract(null, *) = -*").isEqualTo(arg2 * -1L);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(int arg2) {
        assertThat(MathUtils.subtract(null, arg2)).describedAs("subtract(null, *) = -*").isEqualTo(arg2 * -1);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(short arg2) {
        assertThat(MathUtils.subtract(null, arg2))
                .describedAs("subtract(null, *) = -*")
                .isEqualTo((short) (arg2 * -1));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldReturnNegationOfArg2_whenArg1IsNull(byte arg2) {
        assertThat(MathUtils.subtract(null, arg2))
                .describedAs("subtract(null, *) = -*")
                .isEqualTo((byte) (arg2 * -1));
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(BigDecimal nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(BigInteger nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Double nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Float nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Long nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Integer nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Short nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void subtract_shouldReturnZero_whenBothArgsAreNull(Byte nullVal) {
        assertThat(MathUtils.subtract(nullVal, nullVal)).describedAs("subtract(null, null) = 0").isZero();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource({-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(BigDecimal arg1) {
        final BigDecimal arg2 = arg1.multiply(randomBigDecimalAtMost(5, 3));
        final BigDecimal expected = arg1.subtract(arg2);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource({-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(BigInteger arg1) {
        final BigInteger arg2 = arg1.multiply(randomBigIntegerAtMost(5));
        final BigInteger expected = BigInteger.valueOf(arg1.intValue() - arg2.intValue());

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-3, -2.25, -1.5, 0, 1.5, 2.25, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(double arg1) {
        final double arg2 = arg1 * randomDoubleAtMost(5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo(arg1 - arg2);
    }

    @ParameterizedTest
    @ValueSource(floats = {-3, -2.25f, -1.5f, 0, 1.5f, 2.25f, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(float arg1) {
        final float arg2 = arg1 * randomFloatAtMost(5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo(arg1 - arg2);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(long arg1) {
        final long arg2 = arg1 * randomLongAtMost(5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo(arg1 - arg2);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(int arg1) {
        final int arg2 = arg1 * randomIntAtMost(5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo(arg1 - arg2);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(short arg1) {
        final short arg2 = (short) (arg1 * 5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo((short) (arg1 - arg2));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-3, -2, -1, 0, 1, 2, 3})
    void subtract_shouldSubtract_whenArgsAreNonNull(byte arg1) {
        final byte arg2 = (byte) (arg1 * 5);

        assertThat(MathUtils.subtract(arg1, arg2))
                .describedAs("subtract(arg1, arg2) = arg1 - arg2")
                .isEqualTo((byte) (arg1 - arg2));
    }
}
