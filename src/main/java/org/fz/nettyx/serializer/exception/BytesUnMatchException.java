package org.fz.nettyx.serializer.exception;

/**
 * @author fengbinbin
 * @since 2021-10-20 21:56
 **/
public class BytesUnMatchException extends RuntimeException {

    public BytesUnMatchException() {
        super();
    }

    public BytesUnMatchException(String message) {
        super(message);
    }

    public BytesUnMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public BytesUnMatchException(Throwable cause) {
        super(cause);
    }

    protected BytesUnMatchException(String message, Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
