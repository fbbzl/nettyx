package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * byte array codec
 * @author fengbinbin
 * @version 1.0
 * @since 2024/8/4 21:53
 */
public class ByteArrayCodec extends ByteToMessageCodec<byte[]> {

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) {
        out.writeBytes(Unpooled.wrappedBuffer(msg));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            out.add(ByteBufUtil.getBytes(in));
        } finally {
            ReferenceCountUtil.safeRelease(in);
        }
    }

}
