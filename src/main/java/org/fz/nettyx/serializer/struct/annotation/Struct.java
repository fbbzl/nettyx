package org.fz.nettyx.serializer.struct.annotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.ByteOrder;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static lombok.AccessLevel.PRIVATE;

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
    // default is little endian
    Endian order() default Endian.LITTLE;

    @Getter
    @FieldDefaults(makeFinal = true, level = PRIVATE)
    @RequiredArgsConstructor
    enum Endian {
        BIG(ByteOrder.BIG_ENDIAN),
        LITTLE(ByteOrder.LITTLE_ENDIAN);

        ByteOrder order;

    }

}
