package org.fz.nettyx.beanmodel.primitive;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks Java primitive fields for the test serializer field handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface PrimitiveField {
}
