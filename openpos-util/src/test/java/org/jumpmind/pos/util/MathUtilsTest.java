package org.jumpmind.pos.util;

import static org.apache.commons.lang3.math.NumberUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A set of unit tests for the {@link MathUtils} class.
 */
class MathUtilsTest {
    // TODO Complete coverage
    // TODO Randomize inputs and inject into @ParameterizedTests
    // TODO Break up multiple assertions into individual test methods

    @Test
    void abs_shouldReturnEmpty_givenNull() {
        assertThat(MathUtils.abs((BigDecimal) null)).describedAs("abs(BigDecimal[null]) should return 0").isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(MathUtils.abs((BigInteger) null)).describedAs("abs(BigInteger[null]) should return 0").isEqualByComparingTo(BigInteger.ZERO);
        assertThat(MathUtils.abs((Double) null)).describedAs("abs(Double[null]) should return 0").isEqualByComparingTo(DOUBLE_ZERO);
        assertThat(MathUtils.abs((Float) null)).describedAs("abs(Float[null]) should return 0").isEqualByComparingTo(FLOAT_ZERO);
        assertThat(MathUtils.abs((Long) null)).describedAs("abs(Long[null]) should return 0").isEqualByComparingTo(LONG_ZERO);
        assertThat(MathUtils.abs((Integer) null)).describedAs("abs(Integer[null]) should return 0").isEqualByComparingTo(INTEGER_ZERO);
        assertThat(MathUtils.abs((Short) null)).describedAs("abs(Short[null]) should return 0").isEqualByComparingTo(SHORT_ZERO);
        assertThat(MathUtils.abs((Byte) null)).describedAs("abs(Byte[null]) should return 0").isEqualByComparingTo(BYTE_ZERO);
    }

    @Test
    void abs_shouldReturnPositive_givenNegative() {
        final double expected = 1.2345;
        final double negative = expected * -1d;

        assertThat(MathUtils.abs(BigDecimal.valueOf(negative)).doubleValue())
                .describedAs("abs(BigDecimal[negative]) should be positive")
                .isEqualTo(expected);

        assertThat(MathUtils.abs(BigInteger.valueOf((long) negative)).longValue())
                .describedAs("abs(BigInteger[negative]) should be positive")
                .isEqualTo((long) expected);

        assertThat(MathUtils.abs(negative).doubleValue())
                .describedAs("abs(Double[negative]) should be positive")
                .isEqualTo(expected);

        assertThat(MathUtils.abs((float) negative).floatValue())
                .describedAs("abs(Float[negative]) should be positive")
                .isEqualTo((float) expected);

        assertThat(MathUtils.abs((long) negative).longValue())
                .describedAs("abs(Long[negative]) should be positive")
                .isEqualTo((long) expected);

        assertThat(MathUtils.abs((int) negative).intValue())
                .describedAs("abs(Integer[negative]) should be positive")
                .isEqualTo((int) expected);

        assertThat(MathUtils.abs((short) negative).shortValue())
                .describedAs("abs(Short[negative]) should be positive")
                .isEqualTo((short) expected);

        assertThat(MathUtils.abs((byte) negative).byteValue())
                .describedAs("abs(Byte[negative]) should be positive")
                .isEqualTo((byte) expected);
    }

    @Test
    void abs_shouldReturnPositive_givenPositive() {
        final double expected = 1.2345;

        assertThat(MathUtils.abs(BigDecimal.valueOf(expected)).doubleValue())
                .describedAs("abs(BigDecimal[positive]) should be positive")
                .isEqualTo(expected);

        assertThat(MathUtils.abs(BigInteger.valueOf((long) expected)).longValue())
                .describedAs("abs(BigInteger[positive]) should be positive")
                .isEqualTo((long) expected);

        assertThat(MathUtils.abs(expected).doubleValue())
                .describedAs("abs(Double[positive]) should be positive")
                .isEqualTo(expected);

        assertThat(MathUtils.abs((float) expected).floatValue())
                .describedAs("abs(Float[positive]) should be positive")
                .isEqualTo((float) expected);

        assertThat(MathUtils.abs((long) expected).longValue())
                .describedAs("abs(Long[positive]) should be positive")
                .isEqualTo((long) expected);

        assertThat(MathUtils.abs((int) expected).intValue())
                .describedAs("abs(Integer[positive]) should be positive")
                .isEqualTo((int) expected);

        assertThat(MathUtils.abs((short) expected).shortValue())
                .describedAs("abs(Short[positive]) should be positive")
                .isEqualTo((short) expected);

        assertThat(MathUtils.abs((byte) expected).byteValue())
                .describedAs("abs(Byte[positive]) should be positive")
                .isEqualTo((byte) expected);
    }

    @Test
    void abs_shouldReturnZero_givenZero() {
        assertThat(MathUtils.abs(BigDecimal.valueOf(0d)).doubleValue())
                .describedAs("abs(BigDecimal(0) should be 0")
                .isEqualTo(0d);

        assertThat(MathUtils.abs(BigInteger.valueOf(0L)).longValue())
                .describedAs("abs(BigInteger(0) should be 0")
                .isEqualTo(0L);

        assertThat(MathUtils.abs(0d).doubleValue())
                .describedAs("abs(Double(0) should be 0")
                .isEqualTo(0d);

        assertThat(MathUtils.abs(0f).floatValue())
                .describedAs("abs(Float(0)) should be 0")
                .isEqualTo(0f);

        assertThat(MathUtils.abs(0L).longValue())
                .describedAs("abs(Long(0)) should be 0")
                .isEqualTo(0L);

        assertThat(MathUtils.abs(0).intValue())
                .describedAs("abs(Integer(0)) should be 0")
                .isEqualTo(0);

        assertThat(MathUtils.abs((short) 0).shortValue())
                .describedAs("abs(Short(0)) should be 0")
                .isEqualTo((short) 0);

        assertThat(MathUtils.abs((byte) 0).byteValue())
                .describedAs("abs(Byte(0)) should be 0")
                .isEqualTo((byte) 0);
    }
}
