package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.fz.nettyx.serializer.struct.SerializerHandler;

/**
 * If you use a custom field handler annotation, you can use it as a meta annotation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/4 9:43
 */
@Target({FIELD, TYPE})
@Retention(RUNTIME)
public @interface PropertyHandler {

    boolean isSingleton() default true;

    Class<? extends SerializerHandler<?>> value();

}
