package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.serializer.schema.Schema;
import org.fz.nettyx.serializer.schema.SchemaRegistry;
import org.fz.nettyx.serializer.schema.SchemaSerializer;

import java.util.List;
import java.util.Map;

/**
 * Netty codec for structs defined by a {@link SchemaRegistry}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-18
 */
public class SchemaCodec extends ByteToMessageCodec<Map<String, Object>>
{

    private static final InternalLogger log = InternalLoggerFactory.getInstance(SchemaCodec.class);

    private static final boolean DEFAULT_SKIP_LEFT_BYTES = true;

    private final Schema           schema;
    private final SchemaSerializer serializer;
    private final boolean          skipLeftBytes;

    public SchemaCodec(SchemaRegistry registry, String schemaName)
    {
        this(registry, schemaName, DEFAULT_SKIP_LEFT_BYTES);
    }

    public SchemaCodec(SchemaRegistry registry, String schemaName, boolean skipLeftBytes)
    {
        this.schema        = registry.require(schemaName);
        this.serializer    = registry.serializer(schema.fqName());
        this.skipLeftBytes = skipLeftBytes;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public boolean isSkipLeftBytes()
    {
        return skipLeftBytes;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
    {
        out.add(serializer.doDeserialize(msg));
        // A schema represents one message. Extra bytes belong to a following frame only when retained explicitly.
        if (skipLeftBytes && msg.isReadable()) {
            int readableLength = msg.readableBytes();
            log.debug("There is still readable bytes in the buffer after schema serialization, it will be skipped, "
                      + "length is [{}]", readableLength);
            msg.skipBytes(readableLength);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Map<String, Object> struct, ByteBuf out)
    {
        serializer.doSerialize(struct, out);
    }
}
