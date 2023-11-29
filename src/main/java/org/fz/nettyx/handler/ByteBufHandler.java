package org.fz.nettyx.handler;

import org.fz.nettyx.serializer.ReadHandler;
import org.fz.nettyx.serializer.WriteHandler;

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
