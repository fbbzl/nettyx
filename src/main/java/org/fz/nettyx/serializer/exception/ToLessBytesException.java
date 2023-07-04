package org.fz.nettyx.serializer.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:42
 */
public class ToLessBytesException extends RuntimeException {
    public ToLessBytesException() {
        super();
    }
    public ToLessBytesException(String message) {
        super(message);
    }
    public ToLessBytesException(String message, Throwable cause) {
        super(message, cause);
    }
    public ToLessBytesException(Throwable cause) {
        super(cause);
    }
    protected ToLessBytesException(String message, Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
