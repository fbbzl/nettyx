package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.schema.codec.SchemaCodec;

/**
 * A reusable, zero-copy view over one fixed-length configured struct.
 * The view is overwritten by the next {@link SchemaSerializer#viewInto(ByteBuf, SchemaView)} call.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SchemaView
{

    SchemaSerializer  serializer;
    SchemaCodec codec;
    Schema          struct;
    int                   byteLength;

    @NonFinal
    ByteBuf source;
    @NonFinal
    int     startIndex;

    SchemaView(SchemaSerializer serializer, SchemaCodec codec, Schema struct, int byteLength)
    {
        this.serializer = serializer;
        this.codec      = codec;
        this.struct     = struct;
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
        for (SchemaField field : struct.fields()) {
            Object value = codec.readField(field, struct.byteOrder(), reading);
            if (field.name().equals(fieldName)) return value;
        }
        throw new SerializeException("field [" + fieldName + "] does not exist in struct [" + struct.fqName() + "]");
    }

    void reset(ByteBuf source, int startIndex)
    {
        this.source     = source;
        this.startIndex = startIndex;
    }

    boolean belongsTo(SchemaSerializer serializer)
    {
        return this.serializer == serializer;
    }
}