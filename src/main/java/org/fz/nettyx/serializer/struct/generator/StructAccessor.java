package org.fz.nettyx.serializer.struct.generator;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.StructSerializer;

import java.lang.reflect.Type;

/**
 * Generated constructor, reader and writer for a struct type.
 */
public interface StructAccessor {
    Object newInstance();

    Object read(StructSerializer serializer, Type root, Type structType, ByteBuf buf);

    void write(StructSerializer serializer, Type root, Type structType, Object struct, ByteBuf buf);
}
