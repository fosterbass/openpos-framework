package org.jumpmind.pos.wrapper.config;

public interface IExpressionTextStream {
    int getCurrentPosition();
    boolean isAtEnd();
    char peekChar();
    void advanceChar();
}
