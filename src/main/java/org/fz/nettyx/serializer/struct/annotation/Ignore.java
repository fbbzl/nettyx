package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * use this annotation, will skip the bytes-allocation.
 * If the field is transient decorated, it will not participate in serialization either
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/1/20 11:04
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Ignore {}
