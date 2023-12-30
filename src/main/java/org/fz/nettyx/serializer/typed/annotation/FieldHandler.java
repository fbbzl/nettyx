package org.fz.nettyx.serializer.typed.annotation;

import org.fz.nettyx.serializer.typed.ByteBufHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/4 9:43
 */
@Target({FIELD, TYPE})
@Retention(RUNTIME)
public @interface FieldHandler {

    Class<? extends ByteBufHandler> value();

}
