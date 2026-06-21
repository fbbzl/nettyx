package org.fz.nettyx.serializer.struct.generator;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.StructSerializer;

import java.lang.reflect.Type;

/**
 * Generated struct reader and writer.
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
public interface StructReaderWriter {
    Object read(StructSerializer serializer, Type root, Type structType, ByteBuf buf);

    void write(StructSerializer serializer, Type root, Type structType, Object struct, ByteBuf buf);
}
