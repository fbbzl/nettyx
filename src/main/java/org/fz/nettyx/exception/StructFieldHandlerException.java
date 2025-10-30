package org.fz.nettyx.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/10/3 23:35
 */
public class StructFieldHandlerException extends RuntimeException {
    public StructFieldHandlerException() {
        super();
    }

    public StructFieldHandlerException(String message) {
        super(message);
    }

    public StructFieldHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public StructFieldHandlerException(Throwable cause) {
        super(cause);
    }

    protected StructFieldHandlerException(String message, Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
