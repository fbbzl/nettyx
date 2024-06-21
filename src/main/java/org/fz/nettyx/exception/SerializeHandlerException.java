package org.fz.nettyx.exception;

import lombok.Getter;

import java.lang.reflect.Field;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/11 16:02
 */
@Getter
public class SerializeHandlerException extends RuntimeException {

    private final transient Field field;

    private final Class<?> handlerClass;

    public SerializeHandlerException(Field field, Class<?> handlerClass, Throwable throwable) {
        super("handler cause exception please check [" + handlerClass + "], this handler is on field [" + field + "]", throwable);
        this.field        = field;
        this.handlerClass = handlerClass;
    }
}
