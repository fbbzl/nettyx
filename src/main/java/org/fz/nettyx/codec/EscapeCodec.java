package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
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
import org.fz.nettyx.HexBins;

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

    /**
     *  using {@link EscapeMap} to deal message
     */
    @RequiredArgsConstructor
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap escapeMap;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                in = doEscape(in, bufEntry.getValue(), bufEntry.getKey());
            }

            out.add(in);
        }
    }

    /**
     * using {@link EscapeMap} to deal message
     */
    @RequiredArgsConstructor
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap escapeMap;

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
                msg = doEscape(msg, bufEntry.getKey(), bufEntry.getValue());
            }

            out.writeBytes(msg);
        }
    }

    @NoArgsConstructor
    public static class EscapeMap extends HashMap<ByteBuf, ByteBuf> {

        /**
         * init with assigned capacity
         */
        public EscapeMap(int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * @param target the data to be replaced
         * @param replacement the replacement data
         */
        public EscapeMap mapping(ByteBuf target, ByteBuf replacement) {
            super.put(target, replacement);
            return this;
        }

        public EscapeMap mapping(String realHex, String replacementHex) {
            super.put(Unpooled.wrappedBuffer(HexBins.decode(realHex)), Unpooled.wrappedBuffer(HexBins.decode(replacementHex)));
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

        private static void checkMapping(Object[] target, Object[] replacement) {
            if (target.length != replacement.length) throw new IllegalArgumentException("The target data must be the same as the number of replacement data");
        }
    }

    public static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf target, ByteBuf replacement, ByteBuf... excludes) {
        if (containsInvalidByteBuf(msgBuf, target, replacement)) return msgBuf;
        if (excludes.length != 0 && Arrays.binarySearch(excludes, target) != -1) {
            log.warn("It is not recommended to exclude target [{}], This will cause the escape to fail", target);
        }

        final ByteBuf result = msgBuf.alloc().buffer();

        int readIndex = 0;
        while (msgBuf.readableBytes() >= target.readableBytes()) {
            if (hasSimilarBytes(readIndex, msgBuf, target)) {
                msgBuf.markReaderIndex();

                ByteBuf budget = msgBuf.alloc().buffer(target.readableBytes());
                msgBuf.readBytes(budget);

                if (budget.equals(target) && !containExclude(readIndex, msgBuf, excludes)) {
                    result.writeBytes(replacement.duplicate());

                    readIndex += target.readableBytes();
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

        // write the left buffer
        if (msgBuf.readableBytes() > 0) {
            result.writeBytes(msgBuf.readBytes(msgBuf.readableBytes()));
        }

        return result;
    }

    private static boolean containExclude(int index, ByteBuf msgBuf, ByteBuf... excludes) {
        if (excludes.length == 0) {
            return false;
        }

        for (ByteBuf exclude : excludes) {
            for (int i = 0; i < exclude.readableBytes(); i++) {
                try {
                    if (msgBuf.getByte(index + i) != exclude.getByte(i)) {
                        return false;
                    }
                } catch (IndexOutOfBoundsException indexOutOfBounds) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean invalidByteBuf(ByteBuf buffer) {
        return buffer == null || buffer.readableBytes() == 0;
    }

    private static boolean containsInvalidByteBuf(ByteBuf... buffer) {
        for (ByteBuf byteBuf : buffer) {
            if (invalidByteBuf(byteBuf)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSimilarBytes(int index, ByteBuf msgBuf, ByteBuf target) {
        return msgBuf.getByte(index) == target.getByte(0)
               &&
               msgBuf.getByte(index + target.readableBytes() - 1) == target.getByte(target.readableBytes() - 1);
    }
}
