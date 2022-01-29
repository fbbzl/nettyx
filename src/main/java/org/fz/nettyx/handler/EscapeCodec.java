package org.fz.nettyx.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.handler.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.handler.EscapeCodec.EscapeEncoder;

/**
 * @author fengbinbin
 * @since 2022-01-27 18:07
 **/
@Slf4j
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder) {
        super(escapeDecoder, escapeEncoder);
    }

    @RequiredArgsConstructor
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap escapeMap;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                in = replace(in, bufEntry.getKey(), bufEntry.getValue());
            }
            out.add(in);
        }
    }

    @RequiredArgsConstructor
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap escapeMap;

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                msg = replace(msg, bufEntry.getKey(), bufEntry.getValue());
            }
            out.writeBytes(msg);
        }
    }

    public static class EscapeMap extends HashMap<ByteBuf, ByteBuf> {

        /**
         * create single EscapeMap
         */
        public static EscapeMap of(byte[] targetBytes, byte[] replacementBytes) {
            final EscapeMap escapeMap = new EscapeMap();
            escapeMap.put(Unpooled.wrappedBuffer(targetBytes), Unpooled.wrappedBuffer(replacementBytes));
            return escapeMap;
        }

        /**
         * create single EscapeMap
         */
        public static EscapeMap of(ByteBuf target, ByteBuf replacement) {
            final EscapeMap escapeMap = new EscapeMap();
            escapeMap.put(target, replacement);
            return escapeMap;
        }
    }

    static ByteBuf replace(ByteBuf oriBuf, ByteBuf target, ByteBuf replacement) {
        final ByteBuf replaced = oriBuf.alloc().buffer();

        int readIndex = 0;
        while (oriBuf.readableBytes() > target.readableBytes()) {
            ByteBuf budget = Unpooled.buffer(target.readableBytes());
            oriBuf.getBytes(readIndex, budget);

            if (budget.equals(target)) {
                oriBuf.readBytes(target.readableBytes());
                replaced.writeBytes(replacement);

                readIndex += target.readableBytes();
            } else {
                replaced.writeByte(oriBuf.readByte());
                readIndex++;
            }
        }

        // deal left
        if (oriBuf.readableBytes() > 0) {
            replaced.writeBytes(oriBuf.readBytes(oriBuf.readableBytes()));
        }

        return replaced;
    }

}
