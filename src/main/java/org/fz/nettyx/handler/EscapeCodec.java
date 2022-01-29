package org.fz.nettyx.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.handler.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.handler.EscapeCodec.EscapeEncoder;

/**
 * @author fengbinbin
 * @since 2022-01-27 18:07
 **/
@Slf4j
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public static class EscapeDecoder extends ByteToMessageDecoder {

        private ByteBuf[] target;
        private ByteBuf[] replacement;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        }
    }

    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private ByteBuf[] target;
        private ByteBuf[] replacement;

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {

        }
    }

    static ByteBuf replaceEach(ByteBuf preReplaceBuf, ByteBuf[] target, ByteBuf[] replacement) {
        List<Integer> indexes = new ArrayList<>(10);
        for (int i = 0; i < preReplaceBuf.readableBytes(); i++) {
            byte oByte = preReplaceBuf.getByte(i);
            for (int j = 0; j < target.length; j++) {
                ByteBuf byteBuf = target[i];


            }
        }
        for (Integer index : indexes) {
            preReplaceBuf.setBytes(index, replacement[0]);
        }
    }

}
