package org.fz.nettyx.exception;

import java.lang.reflect.Field;
import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/11 16:02
 */
@Getter
public class HandlerException extends RuntimeException {

    private final Field field;

    private final Class<?> handlerClass;

    public HandlerException(Field field, Class<?> handlerClass) {
        super("handler cause exception please check [" + handlerClass + "], this handler is on field [" + field + "]");
        this.field = field;
        this.handlerClass = handlerClass;
    }
}
