package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.support.HexBins;

/**
 * @author fengbinbin
 * @since 2022-01-27 18:07
 **/
@Slf4j
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public EscapeCodec(EscapeMap escapeMap) {
        this(new EscapeDecoder(escapeMap), new EscapeEncoder(escapeMap));
    }

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder) {
        super(escapeDecoder, escapeEncoder);
    }

    @RequiredArgsConstructor
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap escapeMap;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                in = replace(in, bufEntry.getValue(), bufEntry.getKey());
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

    @NoArgsConstructor
    public static class EscapeMap extends HashMap<ByteBuf, ByteBuf> {

        public EscapeMap(int initialCapacity) {
            super(initialCapacity);
        }

        public EscapeMap mapping(ByteBuf real, ByteBuf replacement) {
            super.put(real, replacement);
            return this;
        }

        public EscapeMap mapping(String real, String replacement) {
            super.put(Unpooled.wrappedBuffer(HexBins.decode(real)), Unpooled.wrappedBuffer(HexBins.decode(replacement)));
            return this;
        }

        public static EscapeMap ofEachHex(List<String> target, List<String> replacement) {
            return ofEachHex(target.toArray(new String[]{}), replacement.toArray(new String[]{}));
        }

        public static EscapeMap ofEachHex(String[] target, String[] replacement) {
            checkMapping(target, replacement);

            EscapeMap escapeMap = new EscapeMap();
            for (int i = 0; i < target.length; i++) {
                escapeMap.put(Unpooled.wrappedBuffer(HexBins.decode(target[i])), Unpooled.wrappedBuffer(HexBins.decode(replacement[i])));
            }

            return escapeMap;
        }

        public static EscapeMap ofEach(List<ByteBuf> target, List<ByteBuf> replacement) {
            return ofEach(target.toArray(new ByteBuf[]{}), replacement.toArray(new ByteBuf[]{}));
        }

        public static EscapeMap ofEach(ByteBuf[] target, ByteBuf[] replacement) {
            checkMapping(target, replacement);

            EscapeMap escapeMap = new EscapeMap();
            for (int i = 0; i < target.length; i++) {
                escapeMap.put(target[i], replacement[i]);
            }

            return escapeMap;
        }

        public static EscapeMap ofHex(String targetHex, String replacementHex) {
            return of(HexBins.decode(targetHex), HexBins.decode(replacementHex));
        }

        public static EscapeMap of(byte[] targetBytes, byte[] replacementBytes) {
            return of(Unpooled.wrappedBuffer(targetBytes), Unpooled.wrappedBuffer(replacementBytes));
        }

        public static EscapeMap of(ByteBuf target, ByteBuf replacement) {
            EscapeMap escapeMap = new EscapeMap();
            escapeMap.put(target, replacement);
            return escapeMap;
        }

        private static void checkMapping(Object[] real, Object[] replacement) {
            if (real.length != replacement.length) {
                throw new IllegalArgumentException("The real data must be the same as the number of replacement data");
            }
        }
    }

    static ByteBuf replace(ByteBuf msgBuf, ByteBuf real, ByteBuf replacement) {
        final ByteBuf result = msgBuf.alloc().buffer();

        int readIndex = 0;
        while (msgBuf.readableBytes() >= real.readableBytes()) {
            if (hasSimilarBytes(readIndex, msgBuf, real)) {
                msgBuf.markReaderIndex();

                ByteBuf budget = msgBuf.alloc().buffer(real.readableBytes());
                msgBuf.readBytes(budget);

                if (budget.equals(real)) {
                    replacement.markReaderIndex();
                    result.writeBytes(replacement);
                    replacement.resetReaderIndex();

                    readIndex += real.readableBytes();
                } else {
                    msgBuf.resetReaderIndex();

                    result.writeByte(msgBuf.readByte());
                    readIndex++;
                }
            } else {
                result.writeByte(msgBuf.readByte());
                readIndex++;
            }
        }

        // deal left
        if (msgBuf.readableBytes() > 0) {
            result.writeBytes(msgBuf.readBytes(msgBuf.readableBytes()));
        }

        return result;
    }

    static boolean hasSimilarBytes(int index, ByteBuf msgBuf, ByteBuf real) {
        return msgBuf.getByte(index) == real.getByte(0)
               &&
               msgBuf.getByte(index + real.readableBytes() - 1) == real.getByte(real.readableBytes() - 1);
    }
}
