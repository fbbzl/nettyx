package org.fz.nettyx.exception;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/10/3 23:41
 */
public class StructDefinitionException extends RuntimeException {
    public StructDefinitionException() {
        super();
    }

    public StructDefinitionException(String message) {
        super(message);
    }

    public StructDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StructDefinitionException(Throwable cause) {
        super(cause);
    }

    protected StructDefinitionException(String message, Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
