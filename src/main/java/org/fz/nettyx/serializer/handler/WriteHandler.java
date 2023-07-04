package org.fz.nettyx.serializer.handler;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.serializer.type.TypedByteBufSerializer;

import java.lang.reflect.Field;

/**
 * @author fengbinbin
 * @since 2022-01-16 13:37
 **/
public interface WriteHandler extends ByteBufHandler {

    /**
     * Do write.
     *
     * @param serializer the serializer
     * @param field the field
     * @param value the value
     * @param add the add
     */
    void doWrite(TypedByteBufSerializer serializer, Field field, Object value, ByteBuf add);
}
