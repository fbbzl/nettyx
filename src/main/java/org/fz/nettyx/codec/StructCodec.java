package org.fz.nettyx.codec;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.Getter;
import org.fz.nettyx.serializer.struct.StructSerializer;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/7/26 18:58
 */

@Getter
public abstract class StructCodec<T> extends ByteToMessageCodec<T> {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        out.add(StructSerializer.read(type, msg));
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, T msg, ByteBuf out) {
        out.writeBytes(StructSerializer.write(type, msg));
    }
}