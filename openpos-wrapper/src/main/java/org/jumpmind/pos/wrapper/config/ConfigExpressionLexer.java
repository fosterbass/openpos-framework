package org.jumpmind.pos.wrapper.config;

import java.math.BigDecimal;
import java.util.Optional;

public class ConfigExpressionLexer {
    public static final class Token {
        private final int position;
        private final String rawText;
        private final TokenKind kind;

        private BigDecimal decimalValue;

        Token(int position, String rawText, TokenKind kind) {
            this.position = position;
            this.rawText = rawText;
            this.kind = kind;
        }

        public int getPosition() {
            return this.position;
        }

        public String getRawText() {
            return this.rawText;
        }

        public TokenKind getKind() {
            return this.kind;
        }

        public BigDecimal getDecimalValue() {
            return this.decimalValue;
        }

        public void setDecimalValue(BigDecimal value) {
            this.decimalValue = value;
        }
    }

    public enum TokenKind {
        ERROR,
        IDENTIFIER,
        NUMBER,
        PIPE,
        STRING_LITERAL,
        ASTERISK,
        SLASH,
        PLUS,
        MINUS,
        OPEN_PAREN,
        CLOSE_PAREN,
        COMMA,
        EQUALITY,
        INEQUALITY
    }

    private final IExpressionTextStream stream;
    private final StringBuilder builder = new StringBuilder();

    ConfigExpressionLexer(IExpressionTextStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("source stream must be specified");
        }

        this.stream = stream;
    }

    public Optional<Token> getNextToken() {
        if (stream.isAtEnd()) {
            return Optional.empty();
        }

        // naively skip whitespace
        char character = stream.peekChar();
        while (Character.isWhitespace(character)) {
            stream.advanceChar();
            character = stream.peekChar();
        }

        if (isDigit(character)) {
            return Optional.of(scanNumber());
        }

        if (isValidIdentifierChar(character, false)) {
            return Optional.of(scanIdentifier());
        }

        switch (character) {
            case '\'':
            case '\"':
                return Optional.of(scanStringLiteral());

            case '*':
                return Optional.of(tokenAtPosition("*", TokenKind.ASTERISK));

            case '/':
                return Optional.of(tokenAtPosition("/", TokenKind.SLASH));

            case '+':
                return Optional.of(tokenAtPosition("+", TokenKind.PLUS));

            case '-':
                return Optional.of(tokenAtPosition("-", TokenKind.MINUS));

            case '(':
                return Optional.of(tokenAtPosition("(", TokenKind.OPEN_PAREN));

            case ')':
                return Optional.of(tokenAtPosition(")", TokenKind.CLOSE_PAREN));

            case ',':
                return Optional.of(tokenAtPosition(",", TokenKind.COMMA));

            // todo: greater/less than
            // assignments aren't allowed, expect (in)equality.
            case '!':
            case '=':
                builder.setLength(0);
                builder.append(character);

                final int startedPosition = stream.getCurrentPosition();

                stream.advanceChar();

                if (stream.isAtEnd()) {
                    return Optional.of(error(stream.getCurrentPosition(), "unexpected end of stream"));
                }

                final char equalsChar = stream.peekChar();

                if (equalsChar != '=') {
                    return Optional.of(error(stream.getCurrentPosition(), "expected `=` for equality check"));
                }

                builder.append(equalsChar);

                stream.advanceChar();

                final String rawText = builder.toString();

                return Optional.of(
                        new Token(
                            startedPosition,
                            rawText,
                            rawText.equals("==") ? TokenKind.EQUALITY : TokenKind.INEQUALITY
                        )
                );

            default:
                stream.advanceChar();
                return Optional.of(error(stream.getCurrentPosition() - 1, "invalid token"));
        }
    }

    private Token tokenAtPosition(String rawText, TokenKind kind) {
        final Token token = new Token(stream.getCurrentPosition(), rawText, kind);

        stream.advanceChar();

        return token;
    }

    private Token scanStringLiteral() {
        final char quote = stream.peekChar();

        stream.advanceChar();
        builder.setLength(0);

        int startingAt = stream.getCurrentPosition();

        while (true) {
            if (stream.isAtEnd()) {
                return error(stream.getCurrentPosition(), "string literal not closed");
            }

            final char character = stream.peekChar();

            if (character == '\\') {
                // todo: escape sequences
            } else if (character == quote) {
                stream.advanceChar();
                break;
            } else {
                builder.append(character);
                stream.advanceChar();
            }
        }

        return new Token(
                startingAt,
                builder.toString(),
                TokenKind.STRING_LITERAL
        );
    }

    private Token scanNumber() {
        builder.setLength(0);

        int startingAt = stream.getCurrentPosition();

        boolean isDecimal = false;
        boolean requireNumberNext = false;

        while (!stream.isAtEnd()) {
            final char digit = stream.peekChar();

            if (Character.isDigit(digit)) {
                requireNumberNext = false;

                builder.append(digit);
                stream.advanceChar();
            } else if (digit == '.') {
                if (isDecimal) {
                    return error(stream.getCurrentPosition(), "invalid decimal literal");
                }

                isDecimal = true;
                requireNumberNext = true;

                builder.append(digit);
                stream.advanceChar();
            } else {
                break;
            }
        }

        if (requireNumberNext) {
            return error(stream.getCurrentPosition(), "expected a fractional value after `.`");
        }

        final Token token = new Token(
                startingAt,
                builder.toString(),
                TokenKind.NUMBER
        );

        token.setDecimalValue(new BigDecimal(builder.toString()));

        return token;
    }

    private Token scanIdentifier() {
        final int startAt = stream.getCurrentPosition();

        builder.setLength(0);

        char character = stream.peekChar();
        builder.append(character);

        stream.advanceChar();
        character = stream.peekChar();

        while (isValidIdentifierChar(character, true)) {
            builder.append(character);

            stream.advanceChar();

            if (stream.isAtEnd()) {
                break;
            }

            character = stream.peekChar();
        }

        return new Token(
                startAt,
                builder.toString(),
                TokenKind.IDENTIFIER
        );
    }

    private boolean isDigit(char character) {
        return character >= 0x30 && character <= 0x39;
    }

    private boolean isValidIdentifierChar(char character, boolean digitAllowedInPosition) {
        boolean isUppercaseLetter = character >= 0x41 && character <= 0x5A;
        boolean isLowercaseLetter = character >= 0x61 && character <= 0x7A;

        if (isUppercaseLetter || isLowercaseLetter) {
            return true;
        }

        if (digitAllowedInPosition && isDigit(character)) {
            return true;
        }

        // we're going to allow config entries to be directly referenced, which looks like `something.value.thing`
        if (character == '.' || character == '_') {
            return true;
        }

        return false;
    }

    private Token error(int position, String message) {
        return new Token(position, message, TokenKind.ERROR);
    }
}
