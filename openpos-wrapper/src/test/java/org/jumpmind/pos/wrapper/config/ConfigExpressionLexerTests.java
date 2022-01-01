package org.jumpmind.pos.wrapper.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfigExpressionLexerTests extends ConfigExpressionEngineTests {

    @Test
    void stringLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("'test'");

        assertEquals(1, tokens.size());
        assertStringLiteralToken(1, "test", tokens.get(0));
    }

    @Test
    void wholeNumberLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("123");

        assertEquals(1, tokens.size());
        assertNumberToken(0, new BigDecimal("123"), tokens.get(0));
    }

    @Test
    void fractionalNumberLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("321.23");

        assertEquals(1, tokens.size());
        assertNumberToken(0, new BigDecimal("321.23"), tokens.get(0));
    }

    @Test
    void simpleAddition() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("1+2");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("1"), tokens.get(0));
        assertPlusToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("2"), tokens.get(2));
    }

    @Test
    void simpleMultiplication() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2*3");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("2"), tokens.get(0));
        assertAsteriskToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("3"), tokens.get(2));
    }

    @Test
    void simpleSubtraction() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("9-4");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("9"), tokens.get(0));
        assertMinusToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("4"), tokens.get(2));
    }

    @Test
    void simpleDivision() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("1/6");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("1"), tokens.get(0));
        assertSlashToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("6"), tokens.get(2));
    }

    @Test
    void longMath() {

        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2+3*4/5-6");

        assertEquals(9, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertPlusToken(1, tokens.get(1));
        assertNumberToken(2, 3, tokens.get(2));
        assertAsteriskToken(3, tokens.get(3));
        assertNumberToken(4, 4, tokens.get(4));
        assertSlashToken(5, tokens.get(5));
        assertNumberToken(6, 5, tokens.get(6));
        assertMinusToken(7, tokens.get(7));
        assertNumberToken(8, 6, tokens.get(8));

    }

    @Test
    void simpleEquality() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2==3");

        assertEquals(3, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertEqualityToken(1, tokens.get(1));
        assertNumberToken(3, 3, tokens.get(2));
    }

    @Test
    void simpleInequality() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("4!=5");

        assertEquals(3, tokens.size());
        assertNumberToken(0, 4, tokens.get(0));
        assertInequalityToken(1, tokens.get(1));
        assertNumberToken(3, 5, tokens.get(2));
    }

    @Test
    void expressionWithSpaces() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2 + 3 * 4");

        assertEquals(5, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertPlusToken(2, tokens.get(1));
        assertNumberToken(4, 3, tokens.get(2));
        assertAsteriskToken(6, tokens.get(3));
        assertNumberToken(8, 4, tokens.get(4));
    }

    @Test
    void expressionWithTabs() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2\t+\t3\t*\t4");

        assertEquals(5, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertPlusToken(2, tokens.get(1));
        assertNumberToken(4, 3, tokens.get(2));
        assertAsteriskToken(6, tokens.get(3));
        assertNumberToken(8, 4, tokens.get(4));
    }

    @Test
    void simpleIdentifier() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("test.identIfieR_thing42");

        assertEquals(1, tokens.size());
        assertIdentifierToken(0, "test.identIfieR_thing42", tokens.get(0));
    }

    @Test
    void identifierInMathExpression() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("test.value + 12");

        assertEquals(3, tokens.size());
        assertIdentifierToken(0, "test.value", tokens.get(0));
        assertPlusToken(11, tokens.get(1));
        assertNumberToken(13, 12, tokens.get(2));
    }
}
