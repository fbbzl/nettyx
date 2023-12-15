package org.fz.nettyx.serializer.typed.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * array field must use this to assign array length!!!
 *
 * @author fengbinbin
 * @since 2021-10-20 08:18
 **/

@Target(FIELD)
@Retention(RUNTIME)
public @interface Length {

    int value();
}
