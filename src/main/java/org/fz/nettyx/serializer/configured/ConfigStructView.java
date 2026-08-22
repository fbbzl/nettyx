package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.SerializeException;

/**
 * A reusable, zero-copy view over one fixed-length configured struct.
 * The view is overwritten by the next {@link ConfiguredSerializer#viewInto(ByteBuf, ConfigStructView)} call.
 */
public final class ConfigStructView {

    private final ConfiguredSerializer serializer;
    private final ConfigStruct struct;
    private final int byteLength;

    private ByteBuf source;
    private int startIndex;

    ConfigStructView(ConfiguredSerializer serializer, ConfigStruct struct, int byteLength)
    {
        this.serializer = serializer;
        this.struct = struct;
        this.byteLength = byteLength;
    }

    public int byteLength()
    {
        return byteLength;
    }

    public Object get(String fieldName)
    {
        if (source == null) throw new IllegalStateException("view has not been initialized");

        ByteBuf reading = source.duplicate();
        reading.readerIndex(startIndex);
        for (ConfigField field : struct.fields()) {
            Object value = serializer.readField(field, struct.byteOrder(), reading);
            if (field.name().equals(fieldName)) return value;
        }
        throw new SerializeException("field [" + fieldName + "] does not exist in struct [" + struct.fqName() + "]");
    }

    void reset(ByteBuf source, int startIndex)
    {
        this.source = source;
        this.startIndex = startIndex;
    }

    boolean belongsTo(ConfiguredSerializer serializer)
    {
        return this.serializer == serializer;
    }
}
