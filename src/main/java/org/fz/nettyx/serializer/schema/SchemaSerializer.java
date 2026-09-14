package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.schema.codec.SchemaCodec;

import java.util.Map;

/**
 * the configured struct serializer, parses binary data into {@link Map} by struct definitions
 * loaded from config files, no POJO and no annotation required
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SchemaSerializer implements Serializer
{

    Schema          root;
    SchemaCodec codec;

    public SchemaSerializer(SchemaRegistry registry, String structName)
    {
        this.root  = registry.require(structName);
        this.codec = new SchemaCodec(registry);
    }

    public Schema getStruct()
    {
        return root;
    }

    public static Map<String, Object> toStruct(SchemaRegistry registry, String structName, ByteBuf byteBuf)
    {
        return registry.serializer(structName).doDeserialize(byteBuf);
    }

    public static void toByteBuf(SchemaRegistry registry, String structName, Map<String, Object> structMap, ByteBuf writing)
    {
        registry.serializer(structName).doSerialize(structMap, writing);
    }

    /**
     * Creates a result map that can be reused by {@link #deserializeInto(ByteBuf, Map)}.
     */
    public static Map<String, Object> newReusableStruct(SchemaRegistry registry, String structName)
    {
        return registry.serializer(structName).newReusableStruct();
    }

    /**
     * Deserializes into a reusable target created by {@link #newReusableStruct(SchemaRegistry, String)}.
     * Values such as byte arrays, nested maps and lists are updated in place and must not be retained across calls.
     */
    public static void deserializeInto(SchemaRegistry registry, String structName, ByteBuf reading, Map<String, Object> target)
    {
        registry.serializer(structName).deserializeInto(reading, target);
    }

    public static SchemaView newView(SchemaRegistry registry, String structName)
    {
        return registry.serializer(structName).newView();
    }

    public static void viewInto(SchemaRegistry registry, String structName, ByteBuf reading, SchemaView target)
    {
        registry.serializer(structName).viewInto(reading, target);
    }

    @Override
    public <S> S doDeserialize(ByteBuf reading)
    {
        return (S) codec.readStruct(root, reading);
    }

    public Map<String, Object> newReusableStruct()
    {
        return codec.newReusableStruct(root);
    }

    public void deserializeInto(ByteBuf reading, Map<String, Object> target)
    {
        codec.readStructInto(root, reading, target);
    }

    public SchemaView newView()
    {
        int byteLength = codec.fixedSizeOf(root);
        if (byteLength < 0)
            throw new SerializeException("zero-copy view only supports fixed-length struct: [" + root.fqName() + "]");
        return new SchemaView(this, codec, root, byteLength);
    }

    public void viewInto(ByteBuf reading, SchemaView target)
    {
        if (!target.belongsTo(this))
            throw new SerializeException("view belongs to a different configured serializer");
        if (reading.readableBytes() < target.byteLength())
            throw new TooLessBytesException(target.byteLength(), reading.readableBytes());

        viewIntoUnchecked(reading, target);
    }

    /**
     * Binds a reusable view without validating ownership or available bytes.
     * Call this only after the framing layer has verified a complete fixed-length message.
     */
    void viewIntoUnchecked(ByteBuf reading, SchemaView target)
    {
        target.reset(reading, reading.readerIndex());
        reading.skipBytes(target.byteLength());
    }

    @Override
    public <T> void doSerialize(T struct, ByteBuf writing)
    {
        if (struct != null && !(struct instanceof Map))
            throw new SerializeException("configured serializer only accepts java.util.Map, but got [" + struct.getClass().getName() + "]");

        codec.writeStruct(root, (Map<String, Object>) struct, writing);
    }
}