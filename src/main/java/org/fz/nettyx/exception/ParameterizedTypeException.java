package org.fz.nettyx.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/11 17:13
 */
public class ParameterizedTypeException extends RuntimeException {

    public ParameterizedTypeException(Field field) {
        super("can not determine field [" + field + "] parameterized type");
    }

    public ParameterizedTypeException(Method method) {
        super("can not determine method [" + method + "] parameterized type");
    }

    public ParameterizedTypeException(Class<?> clazz) {
        super("can not determine clazz [" + clazz + "] parameterized type");
    }


}
