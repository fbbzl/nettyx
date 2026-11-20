package org.fz.nettyx.codec;

import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.struct.StructSerializer;

import java.lang.reflect.Type;
import java.util.List;

/**
 * struct codec
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/26 18:58
 */

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StructCodec<S> extends ByteToMessageCodec<S>
{

    private static final InternalLogger log = InternalLoggerFactory.getInstance(StructCodec.class);

    private static final boolean DEFAULT_SKIP_LEFT_BYTES = true;

    private final Type type = TypeToken.of(this.getClass()).resolveType(StructCodec.class.getTypeParameters()[0]).getType();

    private final boolean skipLeftBytes;

    protected StructCodec()
    {
        this.skipLeftBytes = DEFAULT_SKIP_LEFT_BYTES;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
    {
        out.add(StructSerializer.toStruct(type, msg));
        // If there is still readable data in the buffer after serialization, it will be skipped if skipLeftBytes
        // is true
        if (skipLeftBytes && msg.readableBytes() > 0) {
            int readableLength = msg.readableBytes();
            log.debug("There is still readable bytes in the buffer after serialization, it will be skipped, "
                      + "length is [{}]", readableLength);
            msg.skipBytes(readableLength);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, S struct, ByteBuf out)
    {
        StructSerializer.toByteBuf(type, struct, out);
    }
}
