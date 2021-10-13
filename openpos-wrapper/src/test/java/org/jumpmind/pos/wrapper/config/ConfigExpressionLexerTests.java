package org.jumpmind.pos.wrapper.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigExpressionLexerTests {

    @Test
    public void stringLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("'test'");

        assertEquals(1, tokens.size());
        assertStringLiteralToken(1, "test", tokens.get(0));
    }

    @Test
    public void wholeNumberLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("123");

        assertEquals(1, tokens.size());
        assertNumberToken(0, new BigDecimal("123"), tokens.get(0));
    }

    @Test
    public void fractionalNumberLiteral() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("321.23");

        assertEquals(1, tokens.size());
        assertNumberToken(0, new BigDecimal("321.23"), tokens.get(0));
    }

    @Test
    public void simpleAddition() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("1+2");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("1"), tokens.get(0));
        assertPlusToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("2"), tokens.get(2));
    }

    @Test
    public void simpleMultiplication() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2*3");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("2"), tokens.get(0));
        assertAsteriskToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("3"), tokens.get(2));
    }

    @Test
    public void simpleSubtraction() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("9-4");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("9"), tokens.get(0));
        assertMinusToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("4"), tokens.get(2));
    }

    @Test
    public void simpleDivision() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("1/6");

        assertEquals(3, tokens.size());
        assertNumberToken(0, new BigDecimal("1"), tokens.get(0));
        assertSlashToken(1, tokens.get(1));
        assertNumberToken(2, new BigDecimal("6"), tokens.get(2));
    }

    @Test
    public void longMath() {

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
    public void simpleEquality() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2==3");

        assertEquals(3, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertEqualityToken(1, tokens.get(1));
        assertNumberToken(3, 3, tokens.get(2));
    }

    @Test
    public void simpleInequality() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("4!=5");

        assertEquals(3, tokens.size());
        assertNumberToken(0, 4, tokens.get(0));
        assertInequalityToken(1, tokens.get(1));
        assertNumberToken(3, 5, tokens.get(2));
    }

    @Test
    public void expressionWithSpaces() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2 + 3 * 4");

        assertEquals(5, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertPlusToken(2, tokens.get(1));
        assertNumberToken(4, 3, tokens.get(2));
        assertAsteriskToken(6, tokens.get(3));
        assertNumberToken(8, 4, tokens.get(4));
    }

    @Test
    public void expressionWithTabs() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("2\t+\t3\t*\t4");

        assertEquals(5, tokens.size());
        assertNumberToken(0, 2, tokens.get(0));
        assertPlusToken(2, tokens.get(1));
        assertNumberToken(4, 3, tokens.get(2));
        assertAsteriskToken(6, tokens.get(3));
        assertNumberToken(8, 4, tokens.get(4));
    }

    @Test
    public void simpleIdentifier() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("test.identIfieR_thing42");

        assertEquals(1, tokens.size());
        assertIdentifierToken(0, "test.identIfieR_thing42", tokens.get(0));
    }

    @Test
    public void identifierInMathExpression() {
        final List<ConfigExpressionLexer.Token> tokens = tokenStream("test.value + 12");

        assertEquals(3, tokens.size());
        assertIdentifierToken(0, "test.value", tokens.get(0));
        assertPlusToken(11, tokens.get(1));
        assertNumberToken(13, 12, tokens.get(2));
    }

    @Test
    public void testExpressionParser() {
        final ConfigExpressionLexer lexar = makeLexer("1 + 2 * 3 / 3 + 1 * 2 + 5 - 2");
        final ConfigExpression expr = ConfigExpression.parse(lexar, FunctionCollection.empty());

        assertEquals("8", expr.process());
    }

    @Test
    public void testExpressionParserParen() {
        final ConfigExpressionLexer lexar = makeLexer("(1 + (2 * 4)) * (3)");
        final ConfigExpression expr = ConfigExpression.parse(lexar, FunctionCollection.empty());

        assertEquals("27", expr.process());
    }

    @Test
    public void testExpressionParserFuncRight() {
        final ConfigExpressionLexer lexar = makeLexer("5 + test()");
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(ExpressionFunction.make("test", BigDecimal.class, () -> new BigDecimal(42)));

        final ConfigExpression expr = ConfigExpression.parse(lexar, new FunctionCollection(functions));

        assertEquals("47", expr.process());
    }

    @Test
    public void testExpressionParserFuncLeft() {
        final ConfigExpressionLexer lexar = makeLexer(" test() + 5");
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(ExpressionFunction.make("test", BigDecimal.class, () -> new BigDecimal(42)));

        final ConfigExpression expr = ConfigExpression.parse(lexar, new FunctionCollection(functions));

        assertEquals("47", expr.process());
    }

    private ConfigExpressionLexer makeLexer(String expression) {
        return new ConfigExpressionLexer(new StringExpressionTextStream(expression));
    }

    private List<ConfigExpressionLexer.Token> tokenStream(String expression) {
        final ConfigExpressionLexer lexer = makeLexer(expression);

        final List<ConfigExpressionLexer.Token> tokens = new ArrayList<>();

        Optional<ConfigExpressionLexer.Token> nextToken = lexer.getNextToken();
        while (nextToken.isPresent()) {
            tokens.add(nextToken.get());
            nextToken = lexer.getNextToken();
        }

        return tokens;
    }

    private void assertToken(
            int expectedPosition,
            String expectedRawText,
            ConfigExpressionLexer.TokenKind expectedKind,
            ConfigExpressionLexer.Token token
    ) {
        assertEquals(expectedRawText, token.getRawText());
        assertEquals(expectedPosition, token.getPosition());
        assertEquals(expectedKind, token.getKind());
    }

    private void assertIdentifierToken(
            int expectedPosition,
            String expectedIdentifier,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedIdentifier, ConfigExpressionLexer.TokenKind.IDENTIFIER, token);
    }

    private void assertStringLiteralToken(
            int expectedPosition,
            String expectedText,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedText, ConfigExpressionLexer.TokenKind.STRING_LITERAL, token);
    }

    private void assertNumberToken(
            int expectedPosition,
            int expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertNumberToken(expectedPosition, new BigDecimal(Integer.toString(expectedNumber)), token);
    }

    private void assertNumberToken(
            int expectedPosition,
            double expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertNumberToken(expectedPosition, new BigDecimal(Double.toString(expectedNumber)), token);
    }

    private void assertNumberToken(
            int expectedPosition,
            BigDecimal expectedNumber,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, expectedNumber.toPlainString(), ConfigExpressionLexer.TokenKind.NUMBER, token);
        assertEquals(expectedNumber, token.getDecimalValue());
    }

    private void assertPlusToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "+", ConfigExpressionLexer.TokenKind.PLUS, token);
    }

    private void assertAsteriskToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "*", ConfigExpressionLexer.TokenKind.ASTERISK, token);
    }

    private void assertMinusToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "-", ConfigExpressionLexer.TokenKind.MINUS, token);
    }

    private void assertSlashToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "/", ConfigExpressionLexer.TokenKind.SLASH, token);
    }

    private void assertEqualityToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "==", ConfigExpressionLexer.TokenKind.EQUALITY, token);
    }

    private void assertInequalityToken(
            int expectedPosition,
            ConfigExpressionLexer.Token token
    ) {
        assertToken(expectedPosition, "!=", ConfigExpressionLexer.TokenKind.INEQUALITY, token);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> T assertIsPresent(Optional<T> option) {
        assertNotNull(option);
        assertTrue(option.isPresent());
        return option.get();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertIsNotPresent(Optional<?> option) {
        assertNotNull(option);
        assertFalse(option.isPresent());
    }
}
