package org.fz.nettyx.serializer.struct.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * chunk only support for [byte[]/ByteBuf] type field
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/5/11 13:05
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface Chunk {

    /**
     * the number of bytes that need to be occupied
     */
    int size();

}
