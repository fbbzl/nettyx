package org.fz.nettyx.codec;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameDecoder;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameEncoder;

/**
 * Protocols based on start and end characters can be used
 *
 * @author fengbinbin
 * @version 1.0
 * @apiNote The [strip] option is not provided and always strip the head and tail flag
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
     * @param startFlag      the start flag
     * @param endFlag        the end flag
     */
    public StartEndFlagFrameCodec(int maxFrameLength, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param maxFrameLength   the max frame length
     * @param startEndSameFlag the start end same flag
     */
    public StartEndFlagFrameCodec(int maxFrameLength, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param startEndSameFlag the start end same flag
     */
    public StartEndFlagFrameCodec(ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    /**
     * Instantiates a new Start end flag frame codec.
     *
     * @param startFlag the start flag
     * @param endFlag   the end flag
     */
    public StartEndFlagFrameCodec(ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
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

        @Override
        public final boolean isSharable() {
            return false;
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param maxFrameLength   the max frame length
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameDecoder(int maxFrameLength, ByteBuf startEndSameFlag) {
            super(maxFrameLength, true, startEndSameFlag);
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param maxFrameLength the max frame length
         * @param startFlag      the start flag
         * @param endFlag        the end flag
         */
        public StartEndFlagFrameDecoder(int maxFrameLength, ByteBuf startFlag, ByteBuf endFlag) {
            super(maxFrameLength, true, startFlag, endFlag);
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameDecoder(ByteBuf startEndSameFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startEndSameFlag);
        }

        /**
         * Instantiates a new Start end flag frame decoder.
         *
         * @param startFlag the start flag
         * @param endFlag   the end flag
         */
        public StartEndFlagFrameDecoder(ByteBuf startFlag, ByteBuf endFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startFlag, endFlag);
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
            ByteBuf decodedByteBuf = (ByteBuf) super.decode(ctx, buf);
            if (decodedByteBuf != null && decodedByteBuf.readableBytes() > 0) {
                return decodedByteBuf;
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
            byteBuf.writeBytes(Unpooled.wrappedBuffer(startFlag.duplicate(), byteBuf, endFlag.duplicate()));
        }
    }

}
