package org.fz.nettyx.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author fengbinbin
 * @since 2022-01-16 20:01
 **/
public class TypeJudgmentException extends RuntimeException {

    public TypeJudgmentException() {
        super();
    }

    public TypeJudgmentException(String message) {
        super(message);
    }

    public TypeJudgmentException(Field field) {
        super("can not determine type of field [" + field + "]");
    }

    public TypeJudgmentException(Type type) {
        super("can not determine type [" + type + "]");
    }

    public TypeJudgmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeJudgmentException(Throwable cause) {
        super(cause);
    }

    protected TypeJudgmentException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
