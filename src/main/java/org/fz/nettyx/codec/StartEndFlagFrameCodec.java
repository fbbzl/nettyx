package org.fz.nettyx.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameDecoder;
import org.fz.nettyx.codec.StartEndFlagFrameCodec.StartEndFlagFrameEncoder;
import org.fz.nettyx.util.HexKit;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Protocols based on start and end characters can be used
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /1/20 15:13
 */
public class StartEndFlagFrameCodec extends CombinedChannelDuplexHandler<StartEndFlagFrameDecoder,
        StartEndFlagFrameEncoder> {

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, byte[] startFlag, byte[] endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, strip, startFlag, endFlag),
              new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, String startHex, String endHex) {
        this(maxFrameLength, strip, HexKit.decode(startHex), HexKit.decode(endHex));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, byte[] startEndSameFlag) {
        this(maxFrameLength, strip, startEndSameFlag, startEndSameFlag);
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean strip, String startEndSameHex) {
        this(maxFrameLength, strip, startEndSameHex, startEndSameHex);
    }

    /**
     * The type Start end flag frame decoder.
     */
    public static class StartEndFlagFrameDecoder extends DelimiterBasedFrameDecoder {

        private final byte[] startFlag, endFlag;
        private final boolean stripStartEndDelimiter;

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, byte[] startEndSameFlag) {
            super(maxFrameLength, true, wrappedBuffer(startEndSameFlag));

            this.stripStartEndDelimiter = stripDelimiter;

            this.startFlag = this.endFlag = startEndSameFlag;
        }

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, byte[] startFlag, byte[] endFlag) {
            super(maxFrameLength, true, wrappedBuffer(startFlag), wrappedBuffer(endFlag));

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
                    // must use ByteBuf.retainedDuplicate, if you use ByteBuf.duplicate(), it will also be release,
                    // and will never be usable
                    return wrappedBuffer(wrappedBuffer(startFlag), decodedByteBuf, wrappedBuffer(endFlag));
                }
                else {
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

        private final byte[] startFlag, endFlag;

        /**
         * Instantiates a new Start end flag frame encoder.
         *
         * @param startEndSameFlag the start end same flag
         */
        public StartEndFlagFrameEncoder(byte[] startEndSameFlag) {
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        /**
         * Instantiates a new Start end flag frame encoder.
         *
         * @param startFlag the start flag
         * @param endFlag   the end flag
         */
        public StartEndFlagFrameEncoder(byte[] startFlag, byte[] endFlag) {
            this.startFlag = startFlag;
            this.endFlag   = endFlag;
        }

        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf applicationDataBytes, ByteBuf byteBuf) {
            byteBuf.writeBytes(wrappedBuffer(wrappedBuffer(startFlag), applicationDataBytes, wrappedBuffer(endFlag)));
        }
    }

}
