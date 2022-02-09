package org.jumpmind.pos.wrapper.config;

public class StringExpressionTextStream implements IExpressionTextStream {
    private final String expression;
    private int currentPosition = 0;

    public StringExpressionTextStream(String expression) {
        this.expression = expression;
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public boolean isAtEnd() {
        return currentPosition == expression.length();
    }

    @Override
    public char peekChar() {
        return expression.charAt(currentPosition);
    }

    @Override
    public void advanceChar() {
        currentPosition++;
    }
}
