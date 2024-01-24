package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * top serializer meta annotation, every property serializer annotation must add with this annotation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/24 15:11
 */

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface PropertyHandlerAnnotation {
}
