package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * struct object target annotation,
 * An error will be reported when the domain class corresponding to the struct does not have this annotation
 *
 * @Struct
 * public class User {
 *     private Clong id;
 *     private Cint age;
 * }
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/4 10:17
 */

@Target(TYPE)
@Retention(RUNTIME)
public @interface Struct {

}
