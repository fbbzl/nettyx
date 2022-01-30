package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.Arrays;
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

        public ByteBuf test(ByteBuf in) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                in = replace(in, bufEntry.getValue(), bufEntry.getKey());
            }
            return in;
        }

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

        private static void checkMapping(Object[] target, Object[] replacement) {
            if (target.length != replacement.length) {
                throw new IllegalArgumentException("target length and replacement length miss-match");
            }
        }
    }

    static ByteBuf replace(ByteBuf msgBuf, ByteBuf real, ByteBuf replacement) {
        final ByteBuf result = msgBuf.alloc().buffer();

        int readIndex = 0;
        while (msgBuf.readableBytes() > real.readableBytes()) {
            byte headByte = msgBuf.getByte(readIndex),
                 tailByte = msgBuf.getByte(readIndex + real.readableBytes() - 1);

            // filter by head byte and tail byte
            if (headByte == real.getByte(0) && tailByte == real.getByte(real.readableBytes() - 1)) {

                ByteBuf budget = msgBuf.alloc().buffer(real.readableBytes());
                msgBuf.getBytes(readIndex, budget);

                if (budget.equals(real)) {
                    msgBuf.readBytes(real.readableBytes());
                    result.writeBytes(replacement);

                    readIndex += real.readableBytes();
                } else {
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

    /**
     * 0x7E -> 0x7D, 0x5E, 0x7D -> 0x7D, 0x5D
     */
    public static void main(String[] args) {
        byte[] bytes = {0x7d, 111, 111, 111, 111, 111, 111, 111, 111, 111, 0x7d,0x5e, 111, 0x7d,0x5d, 111, 111,111,111, 111, 111, 111, 111, 111, 111,111, 111, 111, 111, 111, 111, 111,111, 111, 111, 111, 111, 111, 111,111, 111, 111, 111, 111, 111, 111};
        System.err.println(Arrays.toString(bytes));
        final EscapeDecoder escapeDecoder = new EscapeDecoder(EscapeMap.ofEachHex(Arrays.asList("7e", "7d"), Arrays.asList("7d5e", "7d5d")));
        final ByteBuf test = escapeDecoder.test(Unpooled.wrappedBuffer(bytes));
        final byte[] bytes1 = ByteBufUtil.getBytes(test);

        System.err.println(Arrays.toString(bytes1));
    }
}
