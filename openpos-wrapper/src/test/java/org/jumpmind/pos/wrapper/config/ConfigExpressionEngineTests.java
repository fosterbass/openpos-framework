package org.jumpmind.pos.wrapper.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class ConfigExpressionEngineTests {
    protected ConfigExpressionLexer makeLexer(String expression) {
        return new ConfigExpressionLexer(new StringExpressionTextStream(expression));
    }

    protected String runExpression(String expression) {
        return runExpression(expression, FunctionCollection.empty(), IdentifierCollection.empty());
    }

    protected <T> String runExpression(String expression, ExpressionFunction<T> function) {
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(function);

        return runExpression(expression, new FunctionCollection(functions), IdentifierCollection.empty());
    }

    protected <T> String runExpression(String expression, IdentifierValue<T> identifierValue) {
        final List<IdentifierValue<?>> identifiers = new ArrayList<>();
        identifiers.add(identifierValue);

        return runExpression(expression, FunctionCollection.empty(), new IdentifierCollection(identifiers));
    }

    protected String runExpression(String expression, FunctionCollection functions, IdentifierCollection identifiers) {
        final ConfigExpressionLexer lexer = makeLexer(expression);
        final ConfigExpression expr = ConfigExpression.parse(lexer, functions, identifiers);

        return expr.process();
    }

    protected List<ConfigExpressionLexer.Token> tokenStream(String expression) {
        final ConfigExpressionLexer lexer = makeLexer(expression);

        final List<ConfigExpressionLexer.Token> tokens = new ArrayList<>();

        Optional<ConfigExpressionLexer.Token> nextToken = lexer.getNextToken();
        while (nextToken.isPresent()) {
            tokens.add(nextToken.get());
            nextToken = lexer.getNextToken();
        }

        return tokens;
    }

    protected void assertToken(
            int expectedPosition,
            String expectedRawText,
            ConfigExpressionLexer.TokenKind expectedKind,
            ConfigExpressionLexer.Token token
    ) {
        assertEquals(expectedRawText, token.getRawText());
        assertEquals(expectedPosition, token.getPosition());
        assertEquals(expectedKind, token.getKind());
    }

    protected void assertIdentifierToken(
            int expectedPosition,
            String expectedIdentifier,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedIdentifier, ConfigExpressionLexer.TokenKind.IDENTIFIER, token);
    }

    protected void assertStringLiteralToken(
            int expectedPosition,
            String expectedText,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedText, ConfigExpressionLexer.TokenKind.STRING_LITERAL, token);
    }

    protected void assertNumberToken(
            int expectedPosition,
            int expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertNumberToken(expectedPosition, new BigDecimal(expectedNumber), token);
    }

    protected void assertNumberToken(
            int expectedPosition,
            double expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertNumberToken(expectedPosition, new BigDecimal(expectedNumber), token);
    }

    protected void assertNumberToken(
            int expectedPosition,
            BigDecimal expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedNumber.toPlainString(), ConfigExpressionLexer.TokenKind.NUMBER, token);
        assertEquals(expectedNumber, token.getDecimalValue());
    }

    protected void assertPlusToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "+", ConfigExpressionLexer.TokenKind.PLUS, token);
    }

    protected void assertAsteriskToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "*", ConfigExpressionLexer.TokenKind.ASTERISK, token);
    }

    protected void assertMinusToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "-", ConfigExpressionLexer.TokenKind.MINUS, token);
    }

    protected void assertSlashToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "/", ConfigExpressionLexer.TokenKind.SLASH, token);
    }

    protected void assertEqualityToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "==", ConfigExpressionLexer.TokenKind.EQUALITY, token);
    }

    protected void assertInequalityToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "!=", ConfigExpressionLexer.TokenKind.INEQUALITY, token);
    }
}
