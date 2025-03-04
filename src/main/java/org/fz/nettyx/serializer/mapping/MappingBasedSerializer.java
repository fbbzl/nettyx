package org.fz.nettyx.serializer.mapping;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.Serializer;

import java.lang.reflect.Type;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/3/11 23:08
 */

public class MappingBasedSerializer implements Serializer {

    /**
     * rootType of struct
     */
    private final Type rootType;

    /**
     * byteBuf ready for serialization/deserialization
     */
    private final ByteBuf byteBuf;

    /**
     * an object ready for serialization/deserialization
     */
    private final Object struct;

    MappingBasedSerializer(Type rootType, ByteBuf byteBuf, Object struct) {
        this.rootType = rootType;
        this.byteBuf  = byteBuf;
        this.struct   = struct;
    }








    @Override
    public Type getType() {
        return rootType;
    }

    @Override
    public ByteBuf getByteBuf() {
        return byteBuf;
    }

}
