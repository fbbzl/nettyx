package org.fz.nettyx.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:42
 */
public class TooLessBytesException extends RuntimeException {
    public TooLessBytesException() {
        super();
    }
    public TooLessBytesException(String message) {
        super(message);
    }
    public TooLessBytesException(String message, Throwable cause) {
        super(message, cause);
    }
    public TooLessBytesException(Throwable cause) {
        super(cause);
    }
    protected TooLessBytesException(String message, Throwable cause,
                                    boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
