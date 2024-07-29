package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/28 23:41
 */

@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToList {


}
