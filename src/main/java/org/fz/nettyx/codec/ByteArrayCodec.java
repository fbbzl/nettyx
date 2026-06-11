package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * byte array codec
 * @author fengbinbin
 * @version 1.0
 * @since 2024/8/4 21:53
 */
public class ByteArrayCodec extends ByteToMessageCodec<byte[]> {

    @Override
    protected void encode(
            ChannelHandlerContext ctx,
            byte[]                msg,
            ByteBuf               out)
    {
        out.writeBytes(msg);
    }

    @Override
    protected void decode(
            ChannelHandlerContext ctx,
            ByteBuf               in,
            List<Object>          out)
    {
        int readable = in.readableBytes();
        byte[] bytes = new byte[readable];
        in.readBytes(bytes);
        out.add(bytes);
    }

}
