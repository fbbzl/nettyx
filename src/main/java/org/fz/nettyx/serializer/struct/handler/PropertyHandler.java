package org.fz.nettyx.serializer.struct.handler;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
import org.fz.nettyx.serializer.struct.StructSerializer;

/**
 * The top-level parent class of all custom serialization processors
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
public interface PropertyHandler<A> {

    /**
     * Is read handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isReadHandler(Class<S> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is write handler boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isWriteHandler(Class<S> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Is read handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isReadHandler(S handler) {
        return handler instanceof ReadHandler;
    }

    /**
     * Is write handler boolean.
     *
     * @param handler the handler
     * @return the boolean
     */
    static <S extends PropertyHandler<?>> boolean isWriteHandler(S handler) {
        return handler instanceof WriteHandler;
    }

    /**
     * The interface Read handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface ReadHandler<A> extends PropertyHandler<A> {

        /**
         * Do read object. if not override, this method will return null
         *
         * @param serializer the serializer
         * @param field the field
         * @return the final returned field value
         */
        Object doRead(StructSerializer serializer, Field field, A annotation);

    }

    /**
     * The interface Write handler.
     *
     * @author fengbinbin
     * @since 2022 -01-16 13:37
     */
    interface WriteHandler<A> extends PropertyHandler<A> {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param field the field
         * @param value the value
         * @param writingBuffer the writingBuffer
         */
        void doWrite(StructSerializer serializer, Field field, Object value, A annotation, ByteBuf writingBuffer);
    }

    /**
     * The interface Read write handler. support read and write
     *
     * @author fengbinbin
     * @since 2022 -01-20 19:46
     */
    interface ReadWriteHandler<A> extends ReadHandler<A>, WriteHandler<A> {

    }

}
