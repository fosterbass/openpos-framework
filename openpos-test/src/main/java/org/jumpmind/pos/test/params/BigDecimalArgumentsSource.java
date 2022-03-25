package org.jumpmind.pos.test.params;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;
import java.math.BigDecimal;

/**
 * An {@link ArgumentsSource} which dispenses randomly-generated {@link BigDecimal} arguments for unit test methods
 * bearing this annotation.
 *
 * @author Jason Weiss
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(BigDecimalArgumentsProvider.class)
public @interface BigDecimalArgumentsSource {
    /**
     * @return the number of arguments to generate
     */
    int count() default 4;

    /**
     * @return the maximum value for any argument
     */
    long max() default 100;

    /**
     * @return the minimum value for any argument
     */
    long min() default 1;

    /**
     * @return the scale of all arguments
     */
    int scale() default 2;

    /**
     * @return the explicit values to inject as arguments; if specified overrides all other parameters
     */
    double[] value() default {};
}
