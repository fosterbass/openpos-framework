package org.jumpmind.pos.test.random;

import static lombok.AccessLevel.PRIVATE;
import static org.joda.money.CurrencyUnit.*;
import static org.jumpmind.pos.test.random.RandomBigDecimals.randomBigDecimalBetween;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Arrays.asList;

import lombok.NoArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * A set of utilities generating random {@link Money} and {@link CurrencyUnit} values for seeding unit test data.
 *
 * @author Jason Weiss
 */
@NoArgsConstructor(access = PRIVATE)
public class RandomMoneys {
    private static final Iterable<CurrencyUnit> possibleCurrencies = asList(AUD, CAD, EUR, GBP, JPY, USD);
    private static final long MAX_VALUE = Long.MAX_VALUE - 1;

    /**
     * Selects a random {@link CurrencyUnit} from among a subset of all possible currencies.
     *
     * @return a randomly-selected {@code CurrencyUnit}
     */
    public static CurrencyUnit randomCurrency() {
        return RandomSelections.selectRandomFrom(possibleCurrencies);
    }

    /**
     * Selects a random {@link CurrencyUnit} from among a qualified subset of supported currencies.
     *
     * @param inclusion1 a currency eligible for selection
     * @param inclusionsN any additional currencies eligible for selection
     * @return a randomly-selected {@code CurrencyUnit} among the supported set of currencies which matches any among
     * {@code inclusion1...inclusionsN}; {@code null} if the supported set of currencies includes no members among {@code inclusion1...inclusionsN}
     */
    public static CurrencyUnit randomCurrencyIn(CurrencyUnit inclusion1, CurrencyUnit... inclusionsN) {
        return RandomSelections.selectRandomFromIncluding(possibleCurrencies, inclusion1, inclusionsN);
    }

    /**
     * Selects a random {@link CurrencyUnit} from among a qualified subset of supported currencies.
     *
     * @param inclusions all currencies eligible for selection
     * @return a randomly-selected {@code CurrencyUnit} among the supported set of currencies which matches any among {@code inclusions}; {@code null}
     * if the supported set of currencies includes no members among {@code inclusions}
     */
    public static CurrencyUnit randomCurrencyIn(Iterable<CurrencyUnit> inclusions) {
        return RandomSelections.selectRandomFromIncluding(possibleCurrencies, inclusions);
    }

    /**
     * Selects a random {@link CurrencyUnit} from among a qualified subset of supported currencies.
     *
     * @param exclusion1 a currency ineligible for selection
     * @param exclusionsN any additional currencies ineligible for selection
     * @return a randomly-selected {@code CurrencyUnit} among the supported set of currencies which matches none among
     * {@code exclusion1...exclusionsN}; {@code null} if the supported set of currencies includes only members among {@code exclusion1...exclusionsN}
     */
    public static CurrencyUnit randomCurrencyNotIn(CurrencyUnit exclusion1, CurrencyUnit... exclusionsN) {
        return RandomSelections.selectRandomFromExcluding(possibleCurrencies, exclusion1, exclusionsN);
    }

    /**
     * Selects a random {@link CurrencyUnit} from among a qualified subset of supported currencies.
     *
     * @param exclusions all currencies ineligible for selection
     * @return a randomly-selected {@code CurrencyUnit} among the supported set of currencies which matches none among {@code exclusions};
     * {@code null} if the supported set of currencies includes only members among {@code exclusions}
     */
    public static CurrencyUnit randomCurrencyNotIn(Iterable<CurrencyUnit> exclusions) {
        return RandomSelections.selectRandomFromExcluding(possibleCurrencies, exclusions);
    }

    /**
     * Generates a random {@link Money} expressed in a randomly-selected currency.
     *
     * @return a {@code Money} having an amount between {@code 0} and {@link Long#MAX_VALUE}, scaled to the number of decimal places dictated by the
     * assigned currency
     */
    public static Money randomMoney() {
        return randomMoneyImpl(0, MAX_VALUE, randomCurrency());
    }

    /**
     * Generates a random {@link Money} expressed in a randomly-selected currency.
     *
     * @param minInclusive the minimum amount of the generated {@code Money} (inclusive); must be >= 0
     * @return a {@code Money} having an amount between {@code minInclusive} and {@link Long#MAX_VALUE}, scaled to the number of decimal places
     * dictated by the assigned currency
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Money randomMoneyAtLeast(long minInclusive) {
        return randomMoneyAtLeast(minInclusive, randomCurrency());
    }

    /**
     * Generates a random {@link Money}.
     *
     * @param minInclusive the minimum amount of the generated {@code Money} (inclusive); must be >= 0
     * @param currency the currency for the generated {@code Money} and which dictates the amount's scale; cannot be {@code null}
     * @return a {@code Money} having an amount between {@code minInclusive} and {@link Long#MAX_VALUE}, scaled to the number of decimal places
     * dictated by {@code currency}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code currency} is {@code null}
     */
    public static Money randomMoneyAtLeast(long minInclusive, CurrencyUnit currency) {
        return randomMoneyImpl(minInclusive, MAX_VALUE, currency);
    }

    /**
     * Generates a random {@link Money} expressed in a randomly-selected currency.
     *
     * @param maxInclusive the maximum value of the generated {@code Money} (inclusive); must be >= 0
     * @return a {@code Money} having an amount between {@code 0} and {@code maxInclusive}, scaled to the number of decimal places dictated by the
     * assigned currency
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Money randomMoneyAtMost(long maxInclusive) {
        return randomMoneyAtMost(maxInclusive, randomCurrency());
    }

    /**
     * Generates a random {@link Money}.
     *
     * @param maxInclusive the maximum value of the generated {@code Money} (inclusive); must be >= 0
     * @param currency the currency for the generated {@code Money} and which dictates the amount's scale; cannot be {@code null}
     * @return a {@code Money} having an amount between {@code 0} and {@code maxInclusive}, scaled to the number of decimal places dictated by {@code
     * currency}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code currency} is {@code null}
     */
    public static Money randomMoneyAtMost(long maxInclusive, CurrencyUnit currency) {
        return randomMoneyImpl(0, maxInclusive, currency);
    }

    /**
     * Generates a random {@link Money} expressed in a randomly-selected currency.
     *
     * @param minInclusive the minimum amount of the generated {@code Money} (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated {@code Money} (inclusive); must be >= 0 and >= {@code minInclusive}
     * @return a {@code Money} having an amount between {@code minInclusive} and {@code maxInclusive}, scaled to the number of decimal places dictated
     * by the assigned currency
     * @throws IllegalArgumentException if any argument violates its documented constraints
     */
    public static Money randomMoneyBetween(long minInclusive, long maxInclusive) {
        return randomMoneyBetween(minInclusive, maxInclusive, randomCurrency());
    }

    /**
     * Generates a random {@link Money}.
     *
     * @param minInclusive the minimum amount of the generated {@code Money} (inclusive); must be >= 0 and <= {@code maxInclusive}
     * @param maxInclusive the maximum value of the generated {@code Money} (inclusive); must be >= 0 and >= {@code minInclusive}
     * @param currency the currency for the generated {@code Money} and which dictates the amount's scale; cannot be {@code null}
     * @return a {@code Money} having an amount between {@code minInclusive} and {@code maxInclusive}, scaled to the number of decimal places dictated
     * by {@code currency}
     * @throws IllegalArgumentException if any argument violates its documented constraints
     * @throws NullPointerException if {@code currency} is {@code null}
     */
    public static Money randomMoneyBetween(long minInclusive, long maxInclusive, CurrencyUnit currency) {
        return randomMoneyImpl(minInclusive, maxInclusive, currency);
    }

    private static Money randomMoneyImpl(long minInclusive, long maxInclusive, CurrencyUnit currency) {
        return Money.of(currency, randomBigDecimalBetween(minInclusive, maxInclusive, currency.getDecimalPlaces()), HALF_EVEN);
    }
}
