package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.HexBins;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;

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
                in = doEscape(in, bufEntry.getValue(), bufEntry.getKey(), escapeMap.getExcludes(bufEntry.getValue()));
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
                msg = doEscape(msg, bufEntry.getKey(), bufEntry.getValue(), escapeMap.getExcludes(bufEntry.getKey()));
            }

            out.writeBytes(msg);
        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class EscapeMap extends HashMap<ByteBuf, ByteBuf> {

        private static final ByteBuf[] EMPTY_BUFFER_ARRAY = new ByteBuf[0];

        @Getter
        private final Map<ByteBuf, ByteBuf[]> excludeMap = new HashMap<>(4);

        /**
         * init with assigned capacity
         */
        private EscapeMap(int initialCapacity) {
            super(initialCapacity);
        }

        public static EscapeMap mapEachHex(List<String> reals, List<String> replacements) {
            return mapEachHex(reals.toArray(new String[]{}), replacements.toArray(new String[]{}));
        }

        /**
         * Escape bytebuffer as needed, and specify real data and replacement in order
         * @param reals The number and order of real buffers that need to be escaped should be the same as that of replacements
         * @param replacements The number and order of replacement buffers should be the same as those of reals
         * @return Buffer after escaped
         */
        public static EscapeMap mapEachHex(String[] reals, String[] replacements) {
            ByteBuf[] realBuffers      = Stream.of(reals).map(HexBins::decode).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new),
                      replacementBuffers = Stream.of(replacements).map(HexBins::decode).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new);

            return mapEach(realBuffers, replacementBuffers);
        }

        public static EscapeMap mapEachBytes(List<byte[]> reals, List<byte[]> replacements) {
            return mapEachBytes(reals.toArray(new byte[][]{}), replacements.toArray(new byte[][]{}));
        }

        public static EscapeMap mapEachBytes(byte[][] reals, byte[][] replacements) {
            ByteBuf[] realBuffers      = Stream.of(reals).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new),
                      replacementBuffers = Stream.of(replacements).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new);

            return mapEach(realBuffers, replacementBuffers);
        }

        public static EscapeMap mapEach(List<ByteBuf> reals, List<ByteBuf> replacements) {
            return mapEach(reals.toArray(new ByteBuf[]{}), replacements.toArray(new ByteBuf[]{}));
        }

        public static EscapeMap mapEach(ByteBuf[] reals, ByteBuf[] replacements) {
            checkMapping(reals, replacements);

            EscapeMap escapeMap = new EscapeMap(reals.length);
            for (int i = 0; i < reals.length; i++) {
                escapeMap.put(reals[i], replacements[i]);
            }
            escapeMap.setExcludeMap(reals, replacements);

            return escapeMap;
        }

        public static EscapeMap mapHex(String realHex, String replacementHex) {
            return map(HexBins.decode(realHex), HexBins.decode(replacementHex));
        }

        public static EscapeMap map(byte[] realBytes, byte[] replacementBytes) {
            return map(Unpooled.wrappedBuffer(realBytes), Unpooled.wrappedBuffer(replacementBytes));
        }

        public static EscapeMap map(ByteBuf real, ByteBuf replacement) {
            EscapeMap escapeMap = new EscapeMap(1);
            escapeMap.put(real, replacement);

            escapeMap.setExcludeMap(new ByteBuf[]{real}, new ByteBuf[]{replacement});
            return escapeMap;
        }

        private static void checkMapping(Object[] real, Object[] replacement) {
            if (real.length != replacement.length) throw new IllegalArgumentException("The real data must be the same as the number of replacement data");
        }

        public ByteBuf[] getExcludes(ByteBuf real) {
            return getExcludeMap().getOrDefault(real, EMPTY_BUFFER_ARRAY);
        }

        public void setExcludeMap(ByteBuf[] reals, ByteBuf[] replacements) {
            for (int i = 0; i < reals.length; i++) {
                List<ByteBuf> excludes = new ArrayList<>(4);
                for (int j = 0; j < replacements.length; j++) {
                    if (i > j && contains(replacements[j], reals[i])) {
                        excludes.add(replacements[j]);
                    }
                }
                this.excludeMap.put(reals[i], excludes.toArray(new ByteBuf[]{}));
            }
        }

        private boolean contains(ByteBuf real, ByteBuf part) {
            ByteBuf buf = real.alloc().buffer(part.readableBytes());
            for (int i = 0; i < real.readableBytes(); i++) {
                real.getBytes(i, buf);
                if (buf.equals(part)) {
                    return true;
                }
                buf.clear();
            }
            return false;
        }
    }

    public static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf real, ByteBuf replacement, ByteBuf... excludes) {
        if (containsInvalidByteBuf(msgBuf, real, replacement)) return msgBuf;
        if (excludes.length != 0 && Arrays.binarySearch(excludes, real) != -1) {
            log.warn("It is not recommended to exclude real [{}], This will cause the escape to fail", real);
        }

        final ByteBuf result = msgBuf.alloc().buffer();

        int readIndex = 0;
        ByteBuf budgetBuffer = msgBuf.alloc().buffer(real.readableBytes());
        while (msgBuf.readableBytes() >= real.readableBytes()) {
            if (hasSimilarBytes(readIndex, msgBuf, real)) {
                // prepare for reset
                msgBuf.markReaderIndex();

                msgBuf.readBytes(budgetBuffer);

                if (budgetBuffer.equals(real) && !equalsAny(readIndex, msgBuf, excludes)) {
                    result.writeBytes(replacement.duplicate());

                    readIndex += real.readableBytes();
                } else {
                    // if not equals, will reset the read index
                    msgBuf.resetReaderIndex();

                    result.writeByte(msgBuf.readByte());
                    readIndex++;
                }

                budgetBuffer.clear();
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

    private static boolean equalsAny(int index, ByteBuf msgBuf, ByteBuf... excludes) {
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

    private static boolean containsInvalidByteBuf(ByteBuf... buffers) {
        for (ByteBuf byteBuf : buffers) {
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
