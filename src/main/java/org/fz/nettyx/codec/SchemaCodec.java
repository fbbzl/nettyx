package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import org.fz.nettyx.serializer.schema.Schema;
import org.fz.nettyx.serializer.schema.SchemaContext;
import org.fz.nettyx.serializer.schema.SchemaSerializer;

import java.util.List;
import java.util.Map;

/**
 * Netty codec for maps encoded using a schema from a {@link SchemaContext}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-18
 */
public class SchemaCodec extends ByteToMessageCodec<Map<String, Object>>
{

    private static final InternalLogger log = InternalLoggerFactory.getInstance(SchemaCodec.class);

    private static final boolean DEFAULT_SKIP_LEFT_BYTES = true;

    @Getter
    private final Schema           schema;
    private final SchemaSerializer serializer;
    @Getter
    private final boolean          skipLeftBytes;

    public SchemaCodec(SchemaContext context, String schemaName)
    {
        this(context, schemaName, DEFAULT_SKIP_LEFT_BYTES);
    }

    public SchemaCodec(SchemaContext context, String schemaName, boolean skipLeftBytes)
    {
        this.schema        = context.require(schemaName);
        this.serializer    = context.serializer(schema.fqName());
        this.skipLeftBytes = skipLeftBytes;
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
    protected void encode(ChannelHandlerContext ctx, Map<String, Object> values, ByteBuf out)
    {
        serializer.doSerialize(values, out);
    }
}
