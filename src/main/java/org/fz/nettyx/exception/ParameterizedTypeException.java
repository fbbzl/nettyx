package org.fz.nettyx.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/11 17:13
 */
public class ParameterizedTypeException extends RuntimeException {

    static String toExceptionMessage(Object entry) {
        return "can not determine field [" + entry + "] parameterized type";
    }

    public ParameterizedTypeException(Field field) {
        super(toExceptionMessage(field));
    }

    public ParameterizedTypeException(Method method) {
        super(toExceptionMessage(method));
    }

    public ParameterizedTypeException(Class<?> clazz) {
        super(toExceptionMessage(clazz));
    }

}
