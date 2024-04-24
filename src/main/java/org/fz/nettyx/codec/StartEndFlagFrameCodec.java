package org.fz.nettyx.codec;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameDecoder;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameEncoder;

/**
 * Protocols based on start and end characters can be used
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /1/20 15:13
 */
@Slf4j
public class StartEndFlagFrameCodec extends CombinedChannelDuplexHandler<StartEndFlagFrameDecoder, StartEndFlagFrameEncoder> {

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, strip, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, strip, startEndSameFlag),
              new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    public StartEndFlagFrameCodec(StartEndFlagFrameDecoder decoder, StartEndFlagFrameEncoder encoder) {
        super(decoder, encoder);
    }

    /**
     * The type Start end flag frame decoder.
     */
    public static class StartEndFlagFrameDecoder extends DelimiterBasedFrameDecoder {

        private final ByteBuf startFlag, endFlag;
        private final boolean stripStartEndDelimiter;

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startEndSameFlag) {
            super(maxFrameLength, true, startEndSameFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag              = this.endFlag = startEndSameFlag;
        }

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
            super(maxFrameLength, true, startFlag, endFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag              = startFlag;
            this.endFlag                = endFlag;
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
            ByteBuf decodedByteBuf = (ByteBuf) super.decode(ctx, buf);
            if (decodedByteBuf != null) {
                if (decodedByteBuf.readableBytes() > 0) {
                    if (stripStartEndDelimiter) return decodedByteBuf;
                    // must use ByteBuf.retainedDuplicate, if you use ByteBuf.duplicate(), it will also be release, and will never be usable
                    return Unpooled.wrappedBuffer(startFlag.retainedDuplicate(), decodedByteBuf, endFlag.retainedDuplicate());
                } else {
                    ReferenceCountUtil.release(decodedByteBuf);
                }
            }

            return null;
        }
    }

    /**
     * The type Start end flag frame encoder.
     */
    public static class StartEndFlagFrameEncoder extends MessageToByteEncoder<ByteBuf> {

        private final ByteBuf startFlag;
        private final ByteBuf endFlag;

        /**
         * Instantiates a new Start end flag frame encoder.
         *
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameEncoder(ByteBuf startEndSameFlag) {
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        /**
         * Instantiates a new Start end flag frame encoder.
         *
         * @param startFlag the start flag
         * @param endFlag   the end flag
         */
        public StartEndFlagFrameEncoder(ByteBuf startFlag, ByteBuf endFlag) {
            this.startFlag = startFlag;
            this.endFlag   = endFlag;
        }

        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf applicationDataBytes, ByteBuf byteBuf) {
            byteBuf.writeBytes(Unpooled.wrappedBuffer(startFlag.duplicate(), applicationDataBytes, endFlag.duplicate()));
        }
    }

}
