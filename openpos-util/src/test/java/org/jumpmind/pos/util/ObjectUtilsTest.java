package org.jumpmind.pos.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A set of unit tests for the {@link ObjectUtils} class.
 */
class ObjectUtilsTest {
    // TODO Complete coverage

    @ParameterizedTest
    @NullSource
    void defaultIfNull_shouldReturnDefault_whenObjIsNull(Object nullObj) {
        final Object expected = new Object();

        assertThat(ObjectUtils.defaultIfNull(nullObj, Function.identity(), expected))
                .describedAs("defaultIfNull(null, *, def) == def")
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"animal", "vegetable", "mineral"})
    void defaultIfNull_shouldReturnMapping_whenFunctionReturnsNonNull(String mapping) {
        assertThat(ObjectUtils.defaultIfNull(new Object(), obj -> mapping, new Object()))
                .describedAs("defaultIfNull(*, * -> mapping, *) == mapping")
                .isEqualTo(mapping);
    }

    @ParameterizedTest
    @NullSource
    void defaultIfNull_shouldReturnNull_whenFunctionReturnsNull(Object nullMapping) {
        final Object expected = new Object();

        assertThat(ObjectUtils.defaultIfNull(new Object(), obj -> nullMapping, expected))
                .describedAs("defaultIfNull(*, * -> null, def) == def")
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    void defaultIfNull_shouldThrow_whenMapperIsNull(Function<Object, Object> nullMapper) {
        assertThatThrownBy(() -> ObjectUtils.defaultIfNull(new Object(), nullMapper, new Object()))
                .isInstanceOf(NullPointerException.class);
    }
}
