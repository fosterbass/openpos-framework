package org.jumpmind.pos.util;

import static org.assertj.core.api.Assertions.assertThat;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.UP;

import org.jumpmind.pos.test.params.BigDecimalArgumentsSource;
import org.jumpmind.pos.test.params.BigDecimalZeroSource;
import org.jumpmind.pos.test.params.BigIntegerArgumentsSource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A set of unit tests for the {@link NumberUtils} class.
 *
 * @noinspection ALL
 */
class NumberUtilsTest {
    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isEqual_shouldReturnFalse_whenOneArgIsNull(BigDecimal val) {
        assertThat(NumberUtils.isEqual(null, val)).describedAs("isEqual(null, *) = false").isFalse();
        assertThat(NumberUtils.isEqual(val, null)).describedAs("isEqual(*, null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isEqual_shouldReturnFalse_whenUnequalValues(BigDecimal val) {
        final BigDecimal unequalVal = val.negate().add(ONE);

        assertThat(NumberUtils.isEqual(val, unequalVal))
                .describedAs("isEqual(val1, val2 != val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isEqual_shouldReturnTrue_whenBothArgsAreNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isEqual(nullVal, nullVal)).describedAs("isEqual(null, null) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isEqual_shouldReturnTrue_whenEqualValuesAndScales(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue());

        assertThat(NumberUtils.isEqual(val, equalVal))
                .describedAs("isEqual(val1, val2 == val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isEqual_shouldReturnTrue_whenEqualValuesButUnequalScales(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue()).setScale(val.scale() + 3, UP);

        assertThat(NumberUtils.isEqual(val, equalVal))
                .describedAs("isEqual(val1, val2 == val1') = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThanOrEqual_shouldReturnFalse_whenAnyArgIsNull(BigDecimal val) {
        assertThat(NumberUtils.isGreaterThanOrEqual(null, val))
                .describedAs("isGreaterThanOrEqual(null, *) = false")
                .isFalse();

        assertThat(NumberUtils.isGreaterThanOrEqual(val, null))
                .describedAs("isGreaterThanOrEqual(*, null) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThanOrEqual_shouldReturnFalse_whenFirstIsLessThanSecond(BigDecimal val) {
        final BigDecimal greaterVal = val.add(ONE);

        assertThat(NumberUtils.isGreaterThanOrEqual(val, greaterVal))
                .describedAs("isGreaterThanOrEqual(val1, val2 > val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThanOrEqual_shouldReturnTrue_whenFirstIsEqualToSecond(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue());

        assertThat(NumberUtils.isGreaterThanOrEqual(val, equalVal))
                .describedAs("isGreaterThanOrEqual(val1, val2 == val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThanOrEqual_shouldReturnTrue_whenFirstIsGreaterThanSecond(BigDecimal val) {
        final BigDecimal lesserVal = val.subtract(ONE);

        assertThat(NumberUtils.isGreaterThanOrEqual(val, lesserVal))
                .describedAs("isGreaterThanOrEqual(val1, val2 < val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThan_shouldReturnFalse_whenAnyArgIsNull(BigDecimal val) {
        assertThat(NumberUtils.isGreaterThan(null, val))
                .describedAs("isGreaterThan(null, *) = false")
                .isFalse();

        assertThat(NumberUtils.isGreaterThan(val, null))
                .describedAs("isGreaterThan(*, null) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThan_shouldReturnFalse_whenFirstIsEqualToSecond(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue());

        assertThat(NumberUtils.isGreaterThan(val, equalVal))
                .describedAs("isGreaterThan(val1, val2 == val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThan_shouldReturnFalse_whenFirstIsLessThanSecond(BigDecimal val) {
        final BigDecimal greaterVal = val.add(ONE);

        assertThat(NumberUtils.isGreaterThan(val, greaterVal))
                .describedAs("isGreaterThan(val1, val2 > val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isGreaterThan_shouldReturnTrue_whenFirstIsGreaterThanSecond(BigDecimal val) {
        final BigDecimal lesserVal = val.subtract(ONE);

        assertThat(NumberUtils.isGreaterThan(val, lesserVal))
                .describedAs("isGreaterThan(val1, val2 < val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThanOrEqual_shouldReturnFalse_whenAnyArgIsNull(BigDecimal val) {
        assertThat(NumberUtils.isLessThanOrEqual(null, val))
                .describedAs("isLessThanOrEqual(null, *) = false")
                .isFalse();

        assertThat(NumberUtils.isLessThanOrEqual(val, null))
                .describedAs("isLessThanOrEqual(*, null) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThanOrEqual_shouldReturnFalse_whenFirstIsGreaterThanSecond(BigDecimal val) {
        final BigDecimal lesserVal = val.subtract(ONE);

        assertThat(NumberUtils.isLessThanOrEqual(val, lesserVal))
                .describedAs("isLessThanOrEqual(val1, val2 < val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThanOrEqual_shouldReturnTrue_whenFirstIsEqualToSecond(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue());

        assertThat(NumberUtils.isLessThanOrEqual(val, equalVal))
                .describedAs("isLessThanOrEqual(val1, val2 == val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThanOrEqual_shouldReturnTrue_whenFirstIsLessThanSecond(BigDecimal val) {
        final BigDecimal greaterVal = val.add(ONE);

        assertThat(NumberUtils.isLessThanOrEqual(val, greaterVal))
                .describedAs("isLessThanOrEqual(val1, val2 > val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThan_shouldReturnFalse_whenAnyArgIsNull(BigDecimal val) {
        assertThat(NumberUtils.isLessThan(null, val))
                .describedAs("isLessThan(null, *) = false")
                .isFalse();

        assertThat(NumberUtils.isLessThan(val, null))
                .describedAs("isLessThan(*, null) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThan_shouldReturnFalse_whenFirstIsEqualToSecond(BigDecimal val) {
        final BigDecimal equalVal = BigDecimal.valueOf(val.doubleValue());

        assertThat(NumberUtils.isLessThan(val, equalVal))
                .describedAs("isLessThan(val1, val2 == val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThan_shouldReturnFalse_whenFirstIsGreaterThanSecond(BigDecimal val) {
        final BigDecimal lesserVal = val.subtract(ONE);

        assertThat(NumberUtils.isLessThan(val, lesserVal))
                .describedAs("isLessThan(val1, val2 < val1) = false")
                .isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void isLessThan_shouldReturnTrue_whenFirstIsLessThanSecond(BigDecimal val) {
        final BigDecimal greaterVal = val.add(ONE);

        assertThat(NumberUtils.isLessThan(val, greaterVal))
                .describedAs("isLessThan(val1, val2 > val1) = true")
                .isTrue();
    }

    @ParameterizedTest
    @NullSource
    void isNegative_shouldReturnFalse_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isNegative(nullVal)).describedAs("isNegative(null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isNegative_shouldReturnFalse_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isNegative(positive)).describedAs("isNegative(val > 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isNegative_shouldReturnFalse_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isNegative(zero)).describedAs("isNegative(val == 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isNegative_shouldReturnTrue_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isNegative(negative)).describedAs("isNegative(val < 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isNonNegative_shouldReturnFalse_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isNonNegative(negative)).describedAs("isNonNegative(val < 0) = false").isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isNonNegative_shouldReturnFalse_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isNonNegative(nullVal)).describedAs("isNonNegative(null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isNonNegative_shouldReturnTrue_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isNonNegative(positive)).describedAs("isNonNegative(val > 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isNonNegative_shouldReturnTrue_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isNonNegative(zero)).describedAs("isNonNegative(val == 0) = true").isTrue();
    }

    @ParameterizedTest
    @NullSource
    void isNonPositive_shouldReturnFalse_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isNonPositive(nullVal)).describedAs("isNonPositive(null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isNonPositive_shouldReturnFalse_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isNonPositive(positive)).describedAs("isNonPositive(val > 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isNonPositive_shouldReturnTrue_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isNonPositive(negative)).describedAs("isNonPositive(val < 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isNonPositive_shouldReturnTrue_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isNonPositive(zero)).describedAs("isNonPositive(val == 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isPositive_shouldReturnFalse_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isPositive(negative)).describedAs("isPositive(val < 0) = false").isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isPositive_shouldReturnFalse_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isPositive(nullVal)).describedAs("isPositive(null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isPositive_shouldReturnFalse_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isPositive(zero)).describedAs("isPositive(val == 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isPositive_shouldReturnTrue_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isPositive(positive)).describedAs("isPositive(val > 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isZeroOrNull_shouldReturnFalse_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isZeroOrNull(negative)).describedAs("isZeroOrNull(val < 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isZeroOrNull_shouldReturnFalse_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isZeroOrNull(positive)).describedAs("isZeroOrNull(val > 0) = false").isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isZeroOrNull_shouldReturnTrue_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isZeroOrNull(nullVal)).describedAs("isZeroOrNull(null) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isZeroOrNull_shouldReturnTrue_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isZeroOrNull(zero.setScale(10, UP)))
                .describedAs("isZeroOrNull(val == 0) = true")
                .isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = -1)
    void isZero_shouldReturnFalse_whenNegative(BigDecimal negative) {
        assertThat(NumberUtils.isZero(negative)).describedAs("isZero(val < 0) = false").isFalse();
    }

    @ParameterizedTest
    @NullSource
    void isZero_shouldReturnFalse_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.isZero(nullVal)).describedAs("isZero(null) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = 1, max = 100)
    void isZero_shouldReturnFalse_whenPositive(BigDecimal positive) {
        assertThat(NumberUtils.isZero(positive)).describedAs("isZero(val > 0) = false").isFalse();
    }

    @ParameterizedTest
    @BigDecimalZeroSource
    void isZero_shouldReturnTrue_whenZero(BigDecimal zero) {
        assertThat(NumberUtils.isZero(zero.setScale(10, UP))).describedAs("isZero(val == 0) = true").isTrue();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void max_shouldReturnArg_whenAllOthersAreLess(BigDecimal maxVal) {
        assertThat(NumberUtils.max(maxVal, maxVal.subtract(ONE), maxVal.subtract(BigDecimal.valueOf(4.25))))
                .describedAs("max(maxVal, lesser...) == maxVal")
                .contains(maxVal);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void max_shouldReturnArg_whenOnlyOneArg(BigDecimal single) {
        assertThat(NumberUtils.max(single)).describedAs("max(single) == single").contains(single);
    }

    @ParameterizedTest
    @NullSource
    void max_shouldReturnEmpty_whenArrayOfNulls(BigDecimal nullVal) {
        assertThat(NumberUtils.max(nullVal, nullVal, nullVal)).describedAs("max(null...) is empty").isEmpty();
    }

    @ParameterizedTest
    @NullSource
    void max_shouldReturnEmpty_whenNull(BigDecimal[] nullVals) {
        assertThat(NumberUtils.max(nullVals)).describedAs("max(null) is empty").isEmpty();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void min_shouldReturnArg_whenAllOthersAreGreater(BigDecimal minVal) {
        assertThat(NumberUtils.min(minVal, minVal.add(ONE), minVal.add(BigDecimal.valueOf(4.25))))
                .describedAs("min(minVal, greater...) == minVal")
                .contains(minVal);
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void min_shouldReturnArg_whenOnlyOneArg(BigDecimal single) {
        assertThat(NumberUtils.min(single)).describedAs("min(single) == single").contains(single);
    }

    @ParameterizedTest
    @NullSource
    void min_shouldReturnEmpty_whenArrayOfNulls(BigDecimal nullVal) {
        assertThat(NumberUtils.min(nullVal, nullVal, nullVal)).describedAs("min(null...) is empty").isEmpty();
    }

    @ParameterizedTest
    @NullSource
    void min_shouldReturnEmpty_whenNull(BigDecimal[] nullVals) {
        assertThat(NumberUtils.min(nullVals)).describedAs("min(null) is empty").isEmpty();
    }

    @ParameterizedTest
    @BigDecimalArgumentsSource(min = -100, max = 100)
    void zeroIfNull_shouldReturnArg_whenNonNull(BigDecimal val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @BigIntegerArgumentsSource(min = -100, max = 100)
    void zeroIfNull_shouldReturnArg_whenNonNull(BigInteger val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-2.5, -1.0, 0, 1.0, 2.5})
    void zeroIfNull_shouldReturnArg_whenNonNull(Double val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(floats = {-2.5f, -1.0f, 0, 1.0f, 2.5f})
    void zeroIfNull_shouldReturnArg_whenNonNull(Float val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -1, 0, 1, 2})
    void zeroIfNull_shouldReturnArg_whenNonNull(Integer val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(longs = {-2, -1, 0, 1, 2})
    void zeroIfNull_shouldReturnArg_whenNonNull(Long val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(shorts = {-2, -1, 0, 1, 2})
    void zeroIfNull_shouldReturnArg_whenNonNull(Short val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @ValueSource(bytes = {-2, -1, 0, 1, 2})
    void zeroIfNull_shouldReturnArg_whenNonNull(Byte val) {
        assertThat(NumberUtils.zeroIfNull(val)).describedAs("zeroIfNull(val != null) == val").isEqualTo(val);
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(BigDecimal nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(BigInteger nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Double nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Float nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Integer nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Long nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Short nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }

    @ParameterizedTest
    @NullSource
    void zeroIfNull_shouldReturnZero_whenNull(Byte nullVal) {
        assertThat(NumberUtils.zeroIfNull(nullVal)).describedAs("zeroIfNull(null) == 0").isZero();
    }
}
