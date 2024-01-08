package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/7 23:36
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface ToBoolean {

    int trueValue() default 1;

    int falseValue() default 0;



}
