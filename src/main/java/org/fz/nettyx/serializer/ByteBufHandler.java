package org.fz.nettyx.serializer;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
/**
 * target handler
 *
 * @author fengbinbin
 * @since 2022-01-16 16:39
 **/
public interface ByteBufHandler {

    static boolean isReadHandler(Class<?> clazz) {
        return ReadHandler.class.isAssignableFrom(clazz);
    }

    static boolean isWriteHandler(Class<?> clazz) {
        return WriteHandler.class.isAssignableFrom(clazz);
    }


    /**
     * @author fengbinbin
     * @since 2022-01-16 13:37
     **/
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
     * @author fengbinbin
     * @since 2022-01-20 19:46
     **/
    interface ReadWriteHandler<S extends Serializer> extends ReadHandler<S>, WriteHandler<S> {

    }

    /**
     * @author fengbinbin
     * @since 2022-01-16 13:37
     **/
    interface WriteHandler<S extends Serializer> extends ByteBufHandler {

        /**
         * Do write.
         *
         * @param serializer the serializer
         * @param field the field
         * @param value the value
         * @param add the add
         */
        void doWrite(S serializer, Field field, Object value, ByteBuf add);
    }
}
