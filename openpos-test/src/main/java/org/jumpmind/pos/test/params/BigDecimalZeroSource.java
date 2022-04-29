package org.jumpmind.pos.test.params;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;
import java.math.BigDecimal;

/**
 * An {@link ArgumentsSource} which dispenses a {@link BigDecimal#ZERO} argument for unit test methods bearing this
 * annotation.
 *
 * @author Jason Weiss
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(BigDecimalZeroProvider.class)
public @interface BigDecimalZeroSource {
}