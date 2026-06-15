package org.fz.nettyx.serializer.struct.annotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.ByteOrder;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * struct annotation
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/4 10:17
 */

@Target(TYPE)
@Retention(RUNTIME)
public @interface Struct {

    Endian endian() default Endian.NATIVE;

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    enum Endian {
        /**
         * Big endian - most significant byte first
         */
        BE(ByteOrder.BIG_ENDIAN),

        /**
         * Little endian - least significant byte first
         */
        LE(ByteOrder.LITTLE_ENDIAN),

        /**
         * Native endian - follows the byte order of the underlying platform
         */
        NATIVE(ByteOrder.nativeOrder()),
        ;

        ByteOrder byteOrder;

    }
}
