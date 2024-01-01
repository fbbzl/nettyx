package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;

/**
 * The top-level parent class of all custom serialization processors
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
public interface SerializerHandler {

    /**
     * Is read handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends SerializerHandler> boolean isReadHandler(Class<S> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is write handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends SerializerHandler> boolean isWriteHandler(Class<S> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is read handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends SerializerHandler> boolean isReadHandler(S handler) {
        return handler instanceof ReadHandler;
    }

    /**
     * Is write handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends SerializerHandler> boolean isWriteHandler(S handler) {
        return handler instanceof WriteHandler;
    }

    /**
     * The interface Read handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface ReadHandler extends SerializerHandler {

        /**
         * Do read object. if not override, this method will return null
         *
         * @param serializer the serializer
         * @param field the field
         * @return the final returned field value
         */
        Object doRead(StructSerializer serializer, Field field);

    }

    /**
     * The interface Write handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface WriteHandler extends SerializerHandler {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param field the field
         * @param value the value
         * @param writingBuffer the writingBuffer
         */
        void doWrite(StructSerializer serializer, Field field, Object value, ByteBuf writingBuffer);
    }

    /**
     * The interface Read write handler. support read and write
     *
     * @author fengbinbin
     * @since 2022 -01-20 19:46
     */
    interface ReadWriteHandler extends ReadHandler, WriteHandler {

    }

}
