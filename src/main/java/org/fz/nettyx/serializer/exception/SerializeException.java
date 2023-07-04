package org.fz.nettyx.serializer.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 9:52
 */
public class SerializeException extends RuntimeException {

    public SerializeException() {
        super();
    }
    public SerializeException(String message) {
        super(message);
    }
    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
    public SerializeException(Throwable cause) {
        super(cause);
    }
    protected SerializeException(String message, Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
