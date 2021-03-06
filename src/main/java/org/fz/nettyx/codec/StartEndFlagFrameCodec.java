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
 * @since 2021/1/20 15:13
 */
@Slf4j
public class StartEndFlagFrameCodec extends CombinedChannelDuplexHandler<StartEndFlagFrameDecoder, StartEndFlagFrameEncoder> {

    /**
     * the default frame size is 1mb
     */
    private static final int DEFAULT_MAX_FRAME_LENGTH = 1024 * 1024;

    public StartEndFlagFrameCodec(int maxFrameLength, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, true, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, true, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    public StartEndFlagFrameCodec(ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(true, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(true, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, stripDelimiter, startFlag, endFlag), new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(int maxFrameLength, boolean stripDelimiter, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(maxFrameLength, stripDelimiter, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    public StartEndFlagFrameCodec(boolean stripDelimiter, ByteBuf startEndSameFlag) {
        super(new StartEndFlagFrameDecoder(stripDelimiter, startEndSameFlag), new StartEndFlagFrameEncoder(startEndSameFlag));
    }

    public StartEndFlagFrameCodec(boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
        super(new StartEndFlagFrameDecoder(stripDelimiter, startFlag, endFlag),
            new StartEndFlagFrameEncoder(startFlag, endFlag));
    }

    public StartEndFlagFrameCodec(StartEndFlagFrameDecoder decoder, StartEndFlagFrameEncoder encoder) {
        super(decoder, encoder);
    }

    public static class StartEndFlagFrameDecoder extends DelimiterBasedFrameDecoder {

        private final ByteBuf startFlag, endFlag;
        private final boolean startEndStripDelimiter;

        @Override
        public final boolean isSharable() {
            return false;
        }

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startEndSameFlag) {
            super(maxFrameLength, true, startEndSameFlag);

            this.startEndStripDelimiter = stripDelimiter;
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        public StartEndFlagFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
            super(maxFrameLength, true, startFlag, endFlag);

            this.startEndStripDelimiter = stripDelimiter;
            this.startFlag = startFlag;
            this.endFlag = endFlag;
        }

        public StartEndFlagFrameDecoder(boolean stripDelimiter, ByteBuf startEndSameFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startEndSameFlag);

            this.startEndStripDelimiter = stripDelimiter;
            this.startFlag = this.endFlag = startEndSameFlag;
        }

        public StartEndFlagFrameDecoder(boolean stripDelimiter, ByteBuf startFlag, ByteBuf endFlag) {
            super(DEFAULT_MAX_FRAME_LENGTH, true, startFlag, endFlag);

            this.startEndStripDelimiter = stripDelimiter;
            this.startFlag = startFlag;
            this.endFlag = endFlag;
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
            ByteBuf decodedByteBuf = (ByteBuf) super.decode(ctx, buf);

            if (decodedByteBuf != null && decodedByteBuf.readableBytes() > 0) {
                return startEndStripDelimiter ? decodedByteBuf : Unpooled.wrappedBuffer(startFlag.duplicate(), decodedByteBuf, endFlag.duplicate());
            }

            return null;
        }
    }

    public static class StartEndFlagFrameEncoder extends MessageToByteEncoder<ByteBuf> {

        private final ByteBuf startFlag;
        private final ByteBuf endFlag;

        @Override
        public final boolean isSharable() {
            return false;
        }

        public StartEndFlagFrameEncoder(ByteBuf startEndSameFlag) {
            this.startFlag = this.endFlag = startEndSameFlag;
        }

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
