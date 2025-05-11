package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/5/11 13:05
 */

@Target(TYPE)
@Retention(RUNTIME)
public @interface Chunk {

    int size();

}
