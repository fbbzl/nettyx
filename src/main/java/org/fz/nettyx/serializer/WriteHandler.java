package org.fz.nettyx.serializer;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.handler.ByteBufHandler;

import java.lang.reflect.Field;

/**
 * @author fengbinbin
 * @since 2022-01-16 13:37
 **/
public interface WriteHandler<S extends Serializer> extends ByteBufHandler {

    /**
     * Do write.
     *
     * @param serializer the serializer
     * @param field      the field
     * @param value      the value
     * @param add        the add
     */
    void doWrite(S serializer, Field field, Object value, ByteBuf add);
}
