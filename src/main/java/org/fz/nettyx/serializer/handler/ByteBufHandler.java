package org.fz.nettyx.serializer.handler;

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
}
