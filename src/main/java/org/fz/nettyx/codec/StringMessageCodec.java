package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

/**
 * basic string codec
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/9 16:35
 */
@Slf4j
@RequiredArgsConstructor
public class StringMessageCodec extends ByteToMessageCodec<String> {

    private final Charset charset;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) {
        list.add(in.readCharSequence(in.readableBytes(), charset).toString());
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String msg, ByteBuf out) {
        out.writeBytes(msg.getBytes(charset));
    }

}
