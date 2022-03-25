package org.jumpmind.pos.util;

import static lombok.AccessLevel.PRIVATE;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * A set of utilities extending the mathematical operations provided by Java and the Apache Commons Math libraries.
 *
 * @author Jason Weiss
 */
@RequiredArgsConstructor(access = PRIVATE)
@Slf4j
public class MathUtils {
    // TODO Complete overloads for methods accepting collections instead of arrays.

    /**
     * Computes the absolute value of a {@code BigDecimal}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code BigDecimal} containing the absolute value of {@code val}; {@link BigDecimal#ZERO} if {@code
     * val} is {@code null}
     */
    public static BigDecimal abs(BigDecimal val) {
        return (val == null) ? BigDecimal.ZERO : val.abs();
    }

    /**
     * Computes the absolute value of a {@code BigInteger}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code BigInteger} containing the absolute value of {@code val}; {@link BigDecimal#ZERO} if {@code
     * val} is {@code null}
     */
    public static BigInteger abs(BigInteger val) {
        return (val == null) ? BigInteger.ZERO : val.abs();
    }

    /**
     * Computes the absolute value of a {@code Double}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Double} containing the absolute value of {@code val}; {@link NumberUtils#DOUBLE_ZERO} if
     * {@code val} is {@code null}
     */
    public static Double abs(Double val) {
        return (val == null) ? NumberUtils.DOUBLE_ZERO : ((val < 0d) ? val * -1d : val);
    }

    /**
     * Computes the absolute value of a {@code Float}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Float} containing the absolute value of {@code val}; {@link NumberUtils#FLOAT_ZERO} if
     * {@code val} is {@code null}
     */
    public static Float abs(Float val) {
        return (val == null) ? NumberUtils.FLOAT_ZERO : ((val < 0f) ? val * -1f : val);
    }

    /**
     * Computes the absolute value of a {@code Long}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Long} containing the absolute value of {@code val}; {@link NumberUtils#LONG_ZERO} if {@code
     * val} is {@code null}
     */
    public static Long abs(Long val) {
        return (val == null) ? NumberUtils.LONG_ZERO : (val < 0L) ? val * -1L : val;
    }

    /**
     * Computes the absolute value of a {@code Integer}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Integer} containing the absolute value of {@code val}; {@link NumberUtils#INTEGER_ZERO} if
     * {@code val} is {@code null}
     */
    public static Integer abs(Integer val) {
        return (val == null) ? NumberUtils.INTEGER_ZERO : ((val < 0) ? val * -1 : val);
    }

    /**
     * Computes the absolute value of a {@code Short}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Short} containing the absolute value of {@code val}; {@link NumberUtils#SHORT_ZERO} if
     * {@code val} is {@code null}
     */
    public static Short abs(Short val) {
        return (val == null) ? NumberUtils.SHORT_ZERO : (short) ((val < 0) ? val * -1 : val);
    }

    /**
     * Computes the absolute value of a {@code Byte}.
     *
     * @param val the number whose absolute value to compute
     * @return a new {@code Byte} containing the absolute value of {@code val}; {@link NumberUtils#BYTE_ZERO} if {@code
     * val} is {@code null}
     */
    public static Byte abs(Byte val) {
        return (val == null) ? NumberUtils.BYTE_ZERO : (byte) ((val < 0) ? val * -1 : val);
    }

    /**
     * Computes the sum of two {@code BigDecimals}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link BigDecimal#ZERO} is returned.
     */
    public static BigDecimal add(BigDecimal val1, BigDecimal val2) {
        return ofNullable(addImpl(val1, val2)).orElse(BigDecimal.ZERO);
    }

    /**
     * Computes the sum of two {@code BigIntegers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link BigInteger#ZERO} is returned.
     */
    public static BigInteger add(BigInteger val1, BigInteger val2) {
        return ofNullable(addImpl(val1, val2)).orElse(BigInteger.ZERO);
    }

    /**
     * Computes the sum of two {@code Doubles}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#DOUBLE_ZERO} is returned.
     */
    public static Double add(Double val1, Double val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Computes the sum of two {@code Floats}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#FLOAT_ZERO} is returned.
     */
    public static Float add(Float val1, Float val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.FLOAT_ZERO);
    }

    /**
     * Computes the sum of two {@code Longs}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#LONG_ZERO} is returned.
     */
    public static Long add(Long val1, Long val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.LONG_ZERO);
    }

    /**
     * Computes the sum of two {@code Integers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#INTEGER_ZERO} is returned.
     */
    public static Integer add(Integer val1, Integer val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.INTEGER_ZERO);
    }

    /**
     * Computes the sum of two {@code Shorts}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#SHORT_ZERO} is returned.
     */
    public static Short add(Short val1, Short val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.SHORT_ZERO);
    }

    /**
     * Computes the sum of two {@code Bytes}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of adding {@code val1} and {@code val2}.  If either operand is {@code null}, the other operand
     * is returned unmodified.  If both operands are {@code null}, {@link NumberUtils#BYTE_ZERO} is returned.
     */
    public static Byte add(Byte val1, Byte val2) {
        return ofNullable(addImpl(val1, val2)).orElse(NumberUtils.BYTE_ZERO);
    }

    /**
     * @see #addAll(Collection)
     */
    public static BigDecimal addAll(BigDecimal... vals) {
        return (ArrayUtils.isEmpty(vals)) ? BigDecimal.ZERO : addAll(asList(vals));
    }

    /**
     * Computes the sum of a sequence of {@code BigDecimals}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link BigDecimal#ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static BigDecimal addAll(Collection<BigDecimal> vals) {
        // TODO Rename to addAllBigDecimals so we can overload for other data types without erasure collisions.

        return (CollectionUtils.isEmpty(vals))
                ? BigDecimal.ZERO
                : vals.stream().filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(BigDecimal.ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code BigIntegers}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link BigInteger#ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static BigInteger addAll(BigInteger... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? BigInteger.ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(BigInteger.ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Doubles}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#DOUBLE_ZERO} if {@code vals}
     * is {@code null}, empty, or contains only {@code null} elements
     */
    public static Double addAll(Double... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.DOUBLE_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Floats}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#FLOAT_ZERO} if {@code vals}
     * is {@code null}, empty, or contains only {@code null} elements
     */
    public static Float addAll(Float... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.FLOAT_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.FLOAT_ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Longs}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#LONG_ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static Long addAll(Long... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.LONG_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.LONG_ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Integers}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#INTEGER_ZERO} if {@code vals}
     * is {@code null}, empty, or contains only {@code null} elements
     */
    public static Integer addAll(Integer... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.INTEGER_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.INTEGER_ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Shorts}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#SHORT_ZERO} if {@code vals}
     * is {@code null}, empty, or contains only {@code null} elements
     */
    public static Short addAll(Short... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.SHORT_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.SHORT_ZERO);
    }

    /**
     * Computes the sum of a sequence of {@code Bytes}.
     *
     * @param vals the numbers to sum; {@code null} elements will be ignored
     * @return the sum of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#BYTE_ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static Byte addAll(Byte... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.BYTE_ZERO
                : stream(vals).filter(Objects::nonNull).reduce(MathUtils::addImpl).orElse(NumberUtils.BYTE_ZERO);
    }

    /**
     * Computes the division of two {@code BigDecimals}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * BigDecimal#ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, RoundingMode roundingMode) {
        return divideImpl(dividend, divisor, BigDecimal.ZERO, roundingMode);
    }

    /**
     * Computes the division of two {@code BigIntegers}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * BigInteger#ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static BigInteger divide(BigInteger dividend, BigInteger divisor) {
        return divideImpl(dividend, divisor, BigInteger.ZERO, null);
    }

    /**
     * Computes the division of two {@code Doubles}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#DOUBLE_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Double divide(Double dividend, Double divisor) {
        return divideImpl(dividend, divisor, NumberUtils.DOUBLE_ZERO, null);
    }

    /**
     * Computes the division of two {@code Floats}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#FLOAT_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Float divide(Float dividend, Float divisor) {
        return divideImpl(dividend, divisor, NumberUtils.FLOAT_ZERO, null);
    }

    /**
     * Computes the division of two {@code Longs}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#LONG_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Long divide(Long dividend, Long divisor) {
        return divideImpl(dividend, divisor, NumberUtils.LONG_ZERO, null);
    }

    /**
     * Computes the division of two {@code Integers}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#INTEGER_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Integer divide(Integer dividend, Integer divisor) {
        return divideImpl(dividend, divisor, NumberUtils.INTEGER_ZERO, null);
    }

    /**
     * Computes the division of two {@code Shorts}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#SHORT_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Short divide(Short dividend, Short divisor) {
        return divideImpl(dividend, divisor, NumberUtils.SHORT_ZERO, null);
    }

    /**
     * Computes the division of two {@code Bytes}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the result of dividing {@code dividend} by {@code divisor}.  If {@code dividend} is {@code null}, {@link
     * NumberUtils#BYTE_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Byte divide(Byte dividend, Byte divisor) {
        return divideImpl(dividend, divisor, NumberUtils.BYTE_ZERO, null);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code BigDecimals}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link BigDecimal#ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static BigDecimal modulus(BigDecimal dividend, BigDecimal divisor) {
        return modulusImpl(dividend, divisor, BigDecimal.ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code BigIntegers}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link BigInteger#ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static BigInteger modulus(BigInteger dividend, BigInteger divisor) {
        return modulusImpl(dividend, divisor, BigInteger.ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Doubles}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#DOUBLE_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Double modulus(Double dividend, Double divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Floats}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#FLOAT_ZERO} is returned.
     * @throws ArithmeticException if {@code dividend} is non-{@code null} and {@code divisor} is {@code null}
     */
    public static Float modulus(Float dividend, Float divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.FLOAT_ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Longs}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#LONG_ZERO} is returned.
     */
    public static Long modulus(Long dividend, Long divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.LONG_ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Integers}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#INTEGER_ZERO} is returned.
     */
    public static Integer modulus(Integer dividend, Integer divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.INTEGER_ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Shorts}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#SHORT_ZERO} is returned.
     */
    public static Short modulus(Short dividend, Short divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.SHORT_ZERO);
    }

    /**
     * Computes the (always-positive) remainder resulting from the division of two {@code Bytes}.
     *
     * @param dividend the "divide into" operand
     * @param divisor the "divide by" operand
     * @return the remainder resulting from the division of {@code dividend} by {@code divisor}.  If {@code dividend} is
     * {@code null}, {@link NumberUtils#BYTE_ZERO} is returned.
     */
    public static Byte modulus(Byte dividend, Byte divisor) {
        return modulusImpl(dividend, divisor, NumberUtils.BYTE_ZERO);
    }

    /**
     * Computes the product of two {@code BigDecimals}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * BigDecimal#ZERO} is returned.
     */
    public static BigDecimal multiply(BigDecimal val1, BigDecimal val2) {
        return multiplyImpl(val1, val2, BigDecimal.ZERO);
    }

    /**
     * Computes the product of two {@code BigIntegers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * BigInteger#ZERO} is returned.
     */
    public static BigInteger multiply(BigInteger val1, BigInteger val2) {
        return multiplyImpl(val1, val2, BigInteger.ZERO);
    }

    /**
     * Computes the product of two {@code Doubles}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#DOUBLE_ZERO} is returned.
     */
    public static Double multiply(Double val1, Double val2) {
        return multiplyImpl(val1, val2, NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Computes the product of two {@code Floats}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#FLOAT_ZERO} is returned.
     */
    public static Float multiply(Float val1, Float val2) {
        return multiplyImpl(val1, val2, NumberUtils.FLOAT_ZERO);
    }

    /**
     * Computes the product of two {@code Longs}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#LONG_ZERO} is returned.
     */
    public static Long multiply(Long val1, Long val2) {
        return multiplyImpl(val1, val2, NumberUtils.LONG_ZERO);
    }

    /**
     * Computes the product of two {@code Integers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#INTEGER_ZERO} is returned.
     */
    public static Integer multiply(Integer val1, Integer val2) {
        return multiplyImpl(val1, val2, NumberUtils.INTEGER_ZERO);
    }

    /**
     * Computes the product of two {@code Shorts}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#SHORT_ZERO} is returned.
     */
    public static Short multiply(Short val1, Short val2) {
        return multiplyImpl(val1, val2, NumberUtils.SHORT_ZERO);
    }

    /**
     * Computes the product of two {@code Bytes}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of multiplying {@code val1} and {@code val2}.  If either operand is {@code null}, {@link
     * NumberUtils#BYTE_ZERO} is returned.
     */
    public static Byte multiply(Byte val1, Byte val2) {
        return multiplyImpl(val1, val2, NumberUtils.BYTE_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code BigDecimals}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link BigDecimal#ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static BigDecimal multiplyAll(BigDecimal... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? BigDecimal.ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, BigDecimal.ZERO))
                        .orElse(BigDecimal.ZERO);
    }

    /**
     * Computes the product of a sequence of {@code BigIntegers}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link BigInteger#ZERO} if {@code vals} is
     * {@code null}, empty, or contains only {@code null} elements
     */
    public static BigInteger multiplyAll(BigInteger... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? BigInteger.ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, BigInteger.ZERO))
                        .orElse(BigInteger.ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Doubles}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#DOUBLE_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Double multiplyAll(Double... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.DOUBLE_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.DOUBLE_ZERO))
                        .orElse(NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Floats}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#FLOAT_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Float multiplyAll(Float... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.FLOAT_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.FLOAT_ZERO))
                        .orElse(NumberUtils.FLOAT_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Longs}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#LONG_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Long multiplyAll(Long... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.LONG_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.LONG_ZERO))
                        .orElse(NumberUtils.LONG_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Integers}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#INTEGER_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Integer multiplyAll(Integer... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.INTEGER_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.INTEGER_ZERO))
                        .orElse(NumberUtils.INTEGER_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Shorts}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#SHORT_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Short multiplyAll(Short... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.SHORT_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.SHORT_ZERO))
                        .orElse(NumberUtils.SHORT_ZERO);
    }

    /**
     * Computes the product of a sequence of {@code Bytes}.
     *
     * @param vals the numbers to multiply together; {@code null} elements will be ignored
     * @return the product of all non-{@code null} numbers in {@code vals}; {@link NumberUtils#BYTE_ZERO} if {@code
     * vals} is {@code null}, empty, or contains only {@code null} elements
     */
    public static Byte multiplyAll(Byte... vals) {
        return (ArrayUtils.isEmpty(vals))
                ? NumberUtils.BYTE_ZERO
                : stream(vals)
                        .filter(Objects::nonNull)
                        .reduce((v1, v2) -> multiplyImpl(v1, v2, NumberUtils.BYTE_ZERO))
                        .orElse(NumberUtils.BYTE_ZERO);
    }

    /**
     * Computes the negation of a {@code BigDecimal}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link BigDecimal#ZERO} if {@code val} is {@code null}
     */
    public static BigDecimal negate(BigDecimal val) {
        return (val == null) ? BigDecimal.ZERO : val.negate();
    }

    /**
     * Computes the negation of a {@code BigInteger}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link BigInteger#ZERO} if {@code val} is {@code null}
     */
    public static BigInteger negate(BigInteger val) {
        return (val == null) ? BigInteger.ZERO : val.negate();
    }

    /**
     * Computes the negation of a {@code Double}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#DOUBLE_ZERO} if {@code val} is {@code null}
     */
    public static Double negate(Double val) {
        return (val == null) ? NumberUtils.DOUBLE_ZERO : val * -1d;
    }

    /**
     * Computes the negation of a {@code Float}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#FLOAT_ZERO} if {@code val} is {@code null}
     */
    public static Float negate(Float val) {
        return (val == null) ? NumberUtils.FLOAT_ZERO : val * -1f;
    }

    /**
     * Computes the negation of a {@code Long}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#LONG_ZERO} if {@code val} is {@code null}
     */
    public static Long negate(Long val) {
        return (val == null) ? NumberUtils.LONG_ZERO : val * -1L;
    }

    /**
     * Computes the negation of a {@code Integer}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#INTEGER_ZERO} if {@code val} is {@code null}
     */
    public static Integer negate(Integer val) {
        return (val == null) ? NumberUtils.INTEGER_ZERO : val * -1;
    }

    /**
     * Computes the negation of a {@code Short}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#SHORT_ZERO} if {@code val} is {@code null}
     */
    public static Short negate(Short val) {
        return (val == null) ? NumberUtils.SHORT_ZERO : (short) (val * -1);
    }

    /**
     * Computes the negation of a {@code Byte}.
     *
     * @param val the number to negate
     * @return the negation of {@code val}; {@link NumberUtils#BYTE_ZERO} if {@code val} is {@code null}
     */
    public static Byte negate(Byte val) {
        return (val == null) ? NumberUtils.BYTE_ZERO : (byte) (val * -1);
    }

    /**
     * Returns the negative of a {@code BigDecimal}.  This differs from {@link #negate(BigDecimal)} in that the latter
     * toggles the value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link BigDecimal#ZERO} if {@code val} is {@code null}
     * @see #negate(BigDecimal)
     */
    public static BigDecimal negative(BigDecimal val) {
        return negate(abs(val));
    }

    /**
     * Returns the negative of a {@code BigInteger}.  This differs from {@link #negate(BigInteger)} in that the latter
     * toggles the value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link BigInteger#ZERO} if {@code val} is {@code null}
     * @see #negate(BigInteger)
     */
    public static BigInteger negative(BigInteger val) {
        return negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Double}.  This differs from {@link #negate(Double)} in that the latter toggles
     * the value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#DOUBLE_ZERO} if {@code val} is {@code null}
     * @see #negate(Double)
     */
    public static Double negative(Double val) {
        return (val == null) ? NumberUtils.DOUBLE_ZERO : negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Float}.  This differs from {@link #negate(Float)} in that the latter toggles the
     * value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#FLOAT_ZERO} if {@code val} is {@code null}
     * @see #negate(Float)
     */
    public static Float negative(Float val) {
        return (val == null) ? NumberUtils.FLOAT_ZERO : negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Long}.  This differs from {@link #negate(Long)} in that the latter toggles the
     * value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#LONG_ZERO} if {@code val} is {@code null}
     * @see #negate(Long)
     */
    public static Long negative(Long val) {
        return negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Integer}.  This differs from {@link #negate(Integer)} in that the latter toggles
     * the value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#INTEGER_ZERO} if {@code val} is {@code null}
     * @see #negate(Integer)
     */
    public static Integer negative(Integer val) {
        return negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Short}.  This differs from {@link #negate(Short)} in that the latter toggles the
     * value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#SHORT_ZERO} if {@code val} is {@code null}
     * @see #negate(Short)
     */
    public static Short negative(Short val) {
        return negate(abs(val));
    }

    /**
     * Returns the negative of a {@code Byte}.  This differs from {@link #negate(Byte)} in that the latter toggles the
     * value's sign, whereas this method always produces a negative one for non-zero values.
     *
     * @param val the number to force to be negative
     * @return the result of calling {@code negate(abs(val))}; {@link NumberUtils#BYTE_ZERO} if {@code val} is {@code null}
     * @see #negate(Byte)
     */
    public static Byte negative(Byte val) {
        return negate(abs(val));
    }

    /**
     * Reports the difference between two {@code BigDecimals}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link BigDecimal#ZERO} is returned.
     */
    public static BigDecimal subtract(BigDecimal val1, BigDecimal val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(BigDecimal.ZERO);
    }

    /**
     * Reports the difference between two {@code BigIntegers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link BigInteger#ZERO} is returned.
     */
    public static BigInteger subtract(BigInteger val1, BigInteger val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(BigInteger.ZERO);
    }

    /**
     * Reports the difference between two {@code Doubles}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null},
     * {@code val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#DOUBLE_ZERO} is
     * returned.
     */
    public static Double subtract(Double val1, Double val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.DOUBLE_ZERO);
    }

    /**
     * Reports the difference between two {@code Floats}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null},
     * {@code val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#FLOAT_ZERO} is
     * returned.
     */
    public static Float subtract(Float val1, Float val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.FLOAT_ZERO);
    }

    /**
     * Reports the difference between two {@code Longs}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#LONG_ZERO} is
     * returned.
     */
    public static Long subtract(Long val1, Long val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.LONG_ZERO);
    }

    /**
     * Reports the difference between two {@code Integers}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#INTEGER_ZERO} is
     * returned.
     */
    public static Integer subtract(Integer val1, Integer val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.INTEGER_ZERO);
    }

    /**
     * Reports the difference between two {@code Shorts}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#SHORT_ZERO} is
     * returned.
     */
    public static Short subtract(Short val1, Short val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.SHORT_ZERO);
    }

    /**
     * Reports the difference between two {@code Bytes}.
     *
     * @param val1 the first operand
     * @param val2 the second operand
     * @return the result of subtracting {@code val2} from {@code val1}.  If {@code val1} is {@code null}, the negation
     * of {@code val2} is returned (i.e. {@code val1} will be treated as zero).  If {@code val2} is {@code null}, {@code
     * val1} will be returned unmodified.  If both operands are {@code null}, {@link NumberUtils#BYTE_ZERO} is
     * returned.
     */
    public static Byte subtract(Byte val1, Byte val2) {
        return ofNullable(subtractImpl(val1, val2)).orElse(NumberUtils.BYTE_ZERO);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T addImpl(T val1, T val2) {
        T result;

        if (val1 == null) {
            result = val2;
        }
        else if (val2 == null) {
            result = val1;
        }
        else if (val1 instanceof BigDecimal) {
            result = (T) ((BigDecimal) val1).add((BigDecimal) val2);
        }
        else if (val1 instanceof BigInteger) {
            result = (T) ((BigInteger) val1).add((BigInteger) val2);
        }
        else if (val1 instanceof Double) {
            result = (T) Double.valueOf(val1.doubleValue() + val2.doubleValue());
        }
        else if (val1 instanceof Float) {
            result = (T) Float.valueOf(val1.floatValue() + val2.floatValue());
        }
        else if (val1 instanceof Long) {
            result = (T) Long.valueOf(val1.longValue() + val2.longValue());
        }
        else if (val1 instanceof Integer) {
            result = (T) Integer.valueOf(val1.intValue() + val2.intValue());
        }
        else if (val1 instanceof Short) {
            result = (T) Short.valueOf((short) (val1.shortValue() + val2.shortValue()));
        }
        else if (val1 instanceof Byte) {
            result = (T) Byte.valueOf((byte) (val1.byteValue() + val2.byteValue()));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot add values of unsupported type [" + val1.getClass().getName() + "]!");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T divideImpl(T dividend, T divisor, T zero, RoundingMode roundingMode) {
        T result;

        if (dividend == null) {
            // Treat the dividend as zero.  Division into zero is always zero.
            result = zero;
        }
        else if ((divisor == null) || divisor.equals(zero)) {
            // Division by zero is an illegal operation.
            throw new ArithmeticException("Cannot divide by zero!");
        }
        else if (dividend instanceof BigDecimal) {
            result = (T) ((BigDecimal) dividend).divide((BigDecimal) divisor,
                    ((roundingMode == null) ? HALF_EVEN : roundingMode));
        }
        else if (dividend instanceof BigInteger) {
            result = (T) ((BigInteger) dividend).divide((BigInteger) divisor);
        }
        else if (dividend instanceof Double) {
            result = (T) Double.valueOf(dividend.doubleValue() / divisor.doubleValue());
        }
        else if (dividend instanceof Float) {
            result = (T) Float.valueOf(dividend.floatValue() / divisor.floatValue());
        }
        else if (dividend instanceof Long) {
            result = (T) Long.valueOf(dividend.longValue() / divisor.longValue());
        }
        else if (dividend instanceof Integer) {
            result = (T) Integer.valueOf(dividend.intValue() / divisor.intValue());
        }
        else if (dividend instanceof Short) {
            result = (T) Short.valueOf((short) (dividend.shortValue() / divisor.shortValue()));
        }
        else if (dividend instanceof Byte) {
            result = (T) Byte.valueOf((byte) (dividend.byteValue() / divisor.byteValue()));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot divide values of unsupported type [" + dividend.getClass().getName() + "]!");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T modulusImpl(T dividend, T divisor, T zero) {
        T result;

        if (dividend == null) {
            // Treat the dividend as zero.  Division into zero is always zero.
            result = zero;
        }
        else if ((divisor == null) || divisor.equals(zero)) {
            // Division by zero is an illegal operation.
            throw new ArithmeticException("Cannot divide by zero!");
        }
        else if (dividend instanceof BigDecimal) {
            result = (T) ((BigDecimal) dividend).remainder((BigDecimal) divisor).abs();
        }
        else if (dividend instanceof BigInteger) {
            result = (T) ((BigInteger) dividend).remainder((BigInteger) divisor).abs();
        }
        else if (dividend instanceof Double) {
            result = (T) Double.valueOf(dividend.doubleValue() % divisor.doubleValue());
        }
        else if (dividend instanceof Float) {
            result = (T) Float.valueOf(dividend.floatValue() % divisor.floatValue());
        }
        else if (dividend instanceof Long) {
            result = (T) Long.valueOf(dividend.longValue() % divisor.longValue());
        }
        else if (dividend instanceof Integer) {
            result = (T) Integer.valueOf(dividend.intValue() % divisor.intValue());
        }
        else if (dividend instanceof Short) {
            result = (T) Short.valueOf((short) (dividend.shortValue() % divisor.shortValue()));
        }
        else if (dividend instanceof Byte) {
            result = (T) Byte.valueOf((byte) (dividend.byteValue() % divisor.byteValue()));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot compute modulus for values of unsupported type [" + dividend.getClass().getName() + "]!");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T multiplyImpl(T val1, T val2, T zero) {
        T result;

        if ((val1 == null) || (val2 == null)) {
            result = zero;
        }
        else if (val1 instanceof BigDecimal) {
            result = (T) ((BigDecimal) val1).multiply((BigDecimal) val2);
        }
        else if (val1 instanceof BigInteger) {
            result = (T) ((BigInteger) val1).multiply((BigInteger) val2);
        }
        else if (val1 instanceof Double) {
            result = (T) Double.valueOf(val1.doubleValue() * val2.doubleValue());
        }
        else if (val1 instanceof Float) {
            result = (T) Float.valueOf(val1.floatValue() * val2.floatValue());
        }
        else if (val1 instanceof Long) {
            result = (T) Long.valueOf(val1.longValue() * val2.longValue());
        }
        else if (val1 instanceof Integer) {
            result = (T) Integer.valueOf(val1.intValue() * val2.intValue());
        }
        else if (val1 instanceof Short) {
            result = (T) Short.valueOf((short) (val1.shortValue() * val2.shortValue()));
        }
        else if (val1 instanceof Byte) {
            result = (T) Byte.valueOf((byte) (val1.byteValue() * val2.byteValue()));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot multiply values of unsupported type [" + val1.getClass().getName() + "]!");
        }
        return result;
    }

    /**
     * Wraps the negation of the most-precise quantity exposed by a number in a new instance of the same data type.
     *
     * @param <T> the type of number being evaluated and returned
     * @param val the number to negate
     * @return a new {@code Number} containing the negation of the most-precise value exposed by {@code val}; {@link
     * Optional#empty()} if {@code val}
     * is {@code null}
     * @throws IllegalArgumentException if {@code val} is of an unknown type or is of a type which does not support this
     * operation
     */
    @SuppressWarnings("unchecked")
    private static <T extends Number> T negateImpl(T val) {
        T result;

        if (val == null) {
            result = null;
        }
        else if (val instanceof BigDecimal) {
            result = (T) ((BigDecimal) val).negate();
        }
        else if (val instanceof BigInteger) {
            result = (T) ((BigInteger) val).negate();
        }
        else if (val instanceof Double) {
            result = (T) Double.valueOf(val.doubleValue() * -1d);
        }
        else if (val instanceof Float) {
            result = (T) Float.valueOf(val.floatValue() * -1f);
        }
        else if (val instanceof Long) {
            result = (T) Long.valueOf(val.longValue() * -1L);
        }
        else if (val instanceof Integer) {
            result = (T) Integer.valueOf(val.intValue() * -1);
        }
        else if (val instanceof Short) {
            result = (T) Short.valueOf((short) (val.shortValue() * -1));
        }
        else if (val instanceof Byte) {
            result = (T) Byte.valueOf((byte) (val.byteValue() * -1));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot negate value of unsupported type [" + val.getClass().getName() + "]!");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T subtractImpl(T val1, T val2) {
        T result;

        if (val1 == null) {
            // Negate val2 as if we were subtracting from zero.
            result = negateImpl(val2);
        }
        else if (val2 == null) {
            result = val1;
        }
        else if (val1 instanceof BigDecimal) {
            result = (T) ((BigDecimal) val1).subtract((BigDecimal) val2);
        }
        else if (val1 instanceof BigInteger) {
            result = (T) ((BigInteger) val1).subtract((BigInteger) val2);
        }
        else if (val1 instanceof Double) {
            result = (T) Double.valueOf(val1.doubleValue() - val2.doubleValue());
        }
        else if (val1 instanceof Float) {
            result = (T) Float.valueOf(val1.floatValue() - val2.floatValue());
        }
        else if (val1 instanceof Long) {
            result = (T) Long.valueOf(val1.longValue() - val2.longValue());
        }
        else if (val1 instanceof Integer) {
            result = (T) Integer.valueOf(val1.intValue() - val2.intValue());
        }
        else if (val1 instanceof Short) {
            result = (T) Short.valueOf((short) (val1.shortValue() - val2.shortValue()));
        }
        else if (val1 instanceof Byte) {
            result = (T) Byte.valueOf((byte) (val1.byteValue() - val2.byteValue()));
        }
        else {
            throw new IllegalArgumentException(
                    "Cannot subtract values of unsupported type [" + val1.getClass().getName() + "]!");
        }
        return result;
    }
}
