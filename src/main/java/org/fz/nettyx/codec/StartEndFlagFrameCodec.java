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

    /**
     * the default frame size is 2048kb
     */
    private static final int DEFAULT_MAX_FRAME_LENGTH = 2 * 1024;

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param maxFrameLength the max frame length
     * @param strip          the strip
     * @param startFlag      the start flag
     * @param endFlag        the end flag
     */
    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, strip, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param maxFrameLength   the max frame length
     * @param strip            the strip
     * @param startEndSameFlag the start end same flag
     */
    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, strip, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param strip            the strip
     * @param startEndSameFlag the start end same flag
     */
    public StartEndFlagFrameCodec(boolean strip, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(strip, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param strip     the strip
     * @param startFlag the start flag
     * @param endFlag   the end flag
     */
    public StartEndFlagFrameCodec(boolean strip, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(strip, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param decoder the decoder
     * @param encoder the encoder
     */
    public StartEndFlagFrameCodec(StartEndFlagFrameDecoder decoder, StartEndFlagFrameEncoder encoder) {
        super(decoder, encoder);
    }

    /**
     * The type Start end flag frame decoder.
     */
    public static class StartEndFlagFrameDecoder extends DelimiterBasedFrameDecoder {

        private final ByteBuf startFlag, endFlag;
        private final boolean stripStartEndDelimiter;

        @Override
        public final boolean isSharable() {
            return false;
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param maxFrameLength   the max frame length
         * @param stripDelimiter   the strip delimiter
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startEndSameFlag) {
            super(maxFrameLength, true, startEndSameFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param maxFrameLength the max frame length
         * @param stripDelimiter the strip delimiter
         * @param startFlag      the start flag
         * @param endFlag        the end flag
         */
        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
            super(maxFrameLength, true, startFlag, endFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag = startFlag;
            this.endFlag = endFlag;
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param stripDelimiter   the strip delimiter
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameDecoder(boolean stripDelimiter, ByteBuf startEndSameFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startEndSameFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param stripDelimiter the strip delimiter
         * @param startFlag      the start flag
         * @param endFlag        the end flag
         */
        public StartEndFlagFrameDecoder(boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startFlag, endFlag);

            this.stripStartEndDelimiter = stripDelimiter;
            this.startFlag = startFlag;
            this.endFlag = endFlag;
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

        @Override
        public final boolean isSharable() {
            return false;
        }

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
            this.endFlag = endFlag;
        }

        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf applicationDataBytes, ByteBuf byteBuf) {
            byteBuf.writeBytes(Unpooled.wrappedBuffer(startFlag.duplicate(), applicationDataBytes, endFlag.duplicate()));
        }
    }

}
