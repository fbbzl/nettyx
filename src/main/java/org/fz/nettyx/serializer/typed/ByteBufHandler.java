package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;

/**
 * target handler
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
public interface ByteBufHandler {

    /**
     * Is read handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static boolean isReadHandler(Class<?> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is write handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static boolean isWriteHandler(Class<?> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }


    /**
     * The interface Read handler.
     *
     * @param <S> the type parameter
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface ReadHandler<S extends Serializer> extends ByteBufHandler {

        /**
         * Do read object. if not override, this method will return null
         *
         * @param serializer the serializer
         * @param field the field
         * @return the final returned field value
         */
        Object doRead(S serializer, Field field);

    }

    /**
     * The interface Write handler.
     *
     * @param <S> the type parameter
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface WriteHandler<S extends Serializer> extends ByteBufHandler {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param field the field
         * @param value the value
         * @param writingBuffer the writingBuffer
         */
        void doWrite(S serializer, Field field, Object value, ByteBuf writingBuffer);
    }

    /**
     * The interface Read write handler.
     * support read and write
     * @param <S> the type parameter
     * @author fengbinbin
     * @since 2022 -01-20 19:46
     */
    interface ReadWriteHandler<S extends Serializer> extends ReadHandler<S>, WriteHandler<S> {

    }

}
