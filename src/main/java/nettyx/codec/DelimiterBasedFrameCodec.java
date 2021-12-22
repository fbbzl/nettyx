package nettyx.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import nettyx.codec.DelimiterBasedFrameCodec.DelimiterBasedFrameEncoder;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/9/2 9:17
 */

public class DelimiterBasedFrameCodec extends CombinedChannelDuplexHandler<DelimiterBasedFrameDecoder, DelimiterBasedFrameEncoder> {

    public DelimiterBasedFrameCodec(DelimiterBasedFrameDecoder decoder, DelimiterBasedFrameEncoder encoder) {
        super(decoder, encoder);
    }

    public static class DelimiterBasedFrameEncoder extends MessageToByteEncoder<ByteBuf> {

        private final ByteBuf delimiter;

        public DelimiterBasedFrameEncoder(ByteBuf delimiter) {
            this.delimiter = delimiter;
        }

        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            out.writeBytes(appendDelimiter(msg));
        }

        private ByteBuf appendDelimiter(ByteBuf byteBuf) {
            return byteBuf.writeBytes(delimiter);
        }
    }
}
