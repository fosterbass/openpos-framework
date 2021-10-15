package org.jumpmind.pos.wrapper.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigExpressionExecutionTests extends ConfigExpressionEngineTests {
    @Test
    void testExpressionParserMathOrderOfOperations() {
        assertEquals("8", runExpression("1 + 2 * 3 / 3 + 1 * 2 + 5 - 2"));
    }

    @Test
    void testExpressionParserParen() {
        assertEquals("27", runExpression("(1 + (2 * 4)) * (3)"));
    }

    @Test
    void testExpressionParserParen2() {
        assertEquals("2", runExpression("6 / (3 * 2) * 2"));
    }

    @Test
    void testExpressionParserFuncRight() {
        final ExpressionFunction<BigDecimal> func = ExpressionFunction.make("test", BigDecimal.class, () -> new BigDecimal(42));
        assertEquals("47", runExpression("5 + test()", func));
    }

    @Test
    void testExpressionParserFuncLeft() {
        final ExpressionFunction<BigDecimal> func = ExpressionFunction.make("test", BigDecimal.class, () -> new BigDecimal(42));
        assertEquals("47", runExpression("test() + 5", func));
    }

    @Test
    void testExpressionParserWithSingleArg() {
        final ExpressionFunction<String> func = ExpressionFunction.make("echo", String.class, String.class, (arg) -> arg);
        assertEquals("Hello, World!", runExpression("echo('Hello, World!')", func));
    }

    @Test
    void testExpressionParserWithMultipleArg() {
        assertEquals("Hello, Billy!", runExpression(
                "concat('Hello, ', 'Billy', '!')",
                BasicFunctionLibrary.collection(),
                IdentifierCollection.empty()
        ));
    }

    @Test
    void testExpressionParserWithConstant() {
        final IdentifierValue<BigDecimal> ident = IdentifierValue.fromConstant("some.constant", new BigDecimal("42"));
        assertEquals("47", runExpression("5 + some.constant", ident));
    }
}
