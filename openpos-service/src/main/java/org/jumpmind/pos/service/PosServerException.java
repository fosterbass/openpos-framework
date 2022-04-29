package org.jumpmind.pos.service;


import lombok.Getter;

public class PosServerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    protected boolean logMessageOnly;

    public PosServerException() {
        super();
    }

    public PosServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PosServerException(String message, Object... args) {
        super(String.format(message, args));
    }

    public PosServerException(Throwable cause) {
        super(cause);
    }
}
