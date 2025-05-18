package org.fz.nettyx.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/6/10 20:40
 */
public class StopRedoException extends RuntimeException {

    public StopRedoException()
    {
        super();
    }

    public StopRedoException(String message)
    {
        super(message);
    }

    public StopRedoException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public StopRedoException(Throwable cause)
    {
        super(cause);
    }

    protected StopRedoException(
            String    message,
            Throwable cause,
            boolean   enableSuppression,
            boolean   writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
