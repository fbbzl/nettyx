package org.fz.nettyx.serializer.typed.annotation.collection;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.typed.ByteBufHandler;
import org.fz.nettyx.serializer.typed.TypedSerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * The interface Set.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:31
 */
public @interface Set {

    /**
     * Type class.
     *
     * @return the class
     */
    Class<? extends java.util.List> type() default ArrayList.class;

    /**
     * Size int.
     *
     * @return the int
     */
    int size() default 0;

    /**
     * Buffer size int.
     *
     * @return the buffer occupied by this char set
     */
    int bufferSize() default 0;

    /**
     * The type Byte buf set handler.
     */
    class ByteBufSetHandler implements ByteBufHandler.ReadWriteHandler<TypedSerializer> {

        @Override
        public Object doRead(TypedSerializer serializer, Field field) {
            // 校验是否是set子类

            return null;
        }

        @Override
        public void doWrite(TypedSerializer serializer, Field field, Object value, ByteBuf add) {

        }

    }

}
