package org.fz.nettyx.codec;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.fz.nettyx.codec.DelimiterBasedFrameCodec.DelimiterBasedFrameEncoder;

/**
 * Extensions to netty's Delimiter Based Frame Decoder
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /9/2 9:17
 */
public class DelimiterBasedFrameCodec extends CombinedChannelDuplexHandler<DelimiterBasedFrameDecoder,
        DelimiterBasedFrameEncoder> {

    public DelimiterBasedFrameCodec(DelimiterBasedFrameDecoder decoder, DelimiterBasedFrameEncoder encoder)
    {
        super(decoder, encoder);
    }

    public DelimiterBasedFrameCodec(int maxFrameLength, ByteBuf delimiter)
    {
        this(new DelimiterBasedFrameDecoder(maxFrameLength, delimiter), new DelimiterBasedFrameEncoder(delimiter));
    }

    public DelimiterBasedFrameCodec(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter)
    {
        this(new DelimiterBasedFrameDecoder(maxFrameLength, stripDelimiter, true, delimiter), new DelimiterBasedFrameEncoder(delimiter));
    }

    public DelimiterBasedFrameCodec(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf delimiter)
    {
        this(new DelimiterBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast, delimiter.slice(delimiter.readerIndex(), delimiter.readableBytes())), new DelimiterBasedFrameEncoder(delimiter));
    }

    public static class DelimiterBasedFrameEncoder extends MessageToByteEncoder<ByteBuf> {

        private final ByteBuf delimiter;

        public DelimiterBasedFrameEncoder(ByteBuf delimiter)
        {
            this.delimiter = delimiter;
        }

        @Override
        public void encode(
                ChannelHandlerContext ctx,
                ByteBuf               msg,
                ByteBuf               out)
        {
            out.writeBytes(Unpooled.wrappedBuffer(msg, delimiter.duplicate()));
        }

    }
}
