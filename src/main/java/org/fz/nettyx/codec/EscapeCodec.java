package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.HexBins;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

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

        public static EscapeMap mapEachHex(List<String> targets, List<String> replacements) {
            return mapEachHex(targets.toArray(new String[]{}), replacements.toArray(new String[]{}));
        }

        /**
         * Escape bytebuffer as needed, and specify target data and replacement in order
         * @param targetHexes The number and order of target buffers that need to be escaped should be the same as that of replacements
         * @param replacements The number and order of replacement buffers should be the same as those of targets
         * @return Buffer after escaped
         */
        public static EscapeMap mapEachHex(String[] targetHexes, String[] replacements) {
            ByteBuf[] targetBuffers      = Stream.of(targetHexes).map(HexBins::decode).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new),
                      replacementBuffers = Stream.of(replacements).map(HexBins::decode).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new);

            return mapEach(targetBuffers, replacementBuffers);
        }

        public static EscapeMap mapEachBytes(List<byte[]> targets, List<byte[]> replacements) {
            return mapEachBytes(targets.toArray(new byte[][]{}), replacements.toArray(new byte[][]{}));
        }

        public static EscapeMap mapEachBytes(byte[][] targets, byte[][] replacements) {
            ByteBuf[] targetBuffers      = Stream.of(targets).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new),
                      replacementBuffers = Stream.of(replacements).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new);

            return mapEach(targetBuffers, replacementBuffers);
        }

        public static EscapeMap mapEach(List<ByteBuf> targets, List<ByteBuf> replacements) {
            return mapEach(targets.toArray(new ByteBuf[]{}), replacements.toArray(new ByteBuf[]{}));
        }

        public static EscapeMap mapEach(ByteBuf[] targets, ByteBuf[] replacements) {
            checkMapping(targets, replacements);

            EscapeMap escapeMap = new EscapeMap(targets.length);
            for (int i = 0; i < targets.length; i++) {
                escapeMap.put(targets[i], replacements[i]);
            }
            escapeMap.setExcludeMap(targets, replacements);

            return escapeMap;
        }

        public static EscapeMap mapHex(String targetHex, String replacementHex) {
            return map(HexBins.decode(targetHex), HexBins.decode(replacementHex));
        }

        public static EscapeMap map(byte[] targetBytes, byte[] replacementBytes) {
            return map(Unpooled.wrappedBuffer(targetBytes), Unpooled.wrappedBuffer(replacementBytes));
        }

        public static EscapeMap map(ByteBuf target, ByteBuf replacement) {
            EscapeMap escapeMap = new EscapeMap(1);
            escapeMap.put(target, replacement);

            escapeMap.setExcludeMap(new ByteBuf[]{target}, new ByteBuf[]{replacement});
            return escapeMap;
        }

        private static void checkMapping(Object[] target, Object[] replacement) {
            if (target.length != replacement.length) throw new IllegalArgumentException("The target data must be the same as the number of replacement data");
        }

        public ByteBuf[] getExcludes(ByteBuf target) {
            return getExcludeMap().getOrDefault(target, EMPTY_BUFFER_ARRAY);
        }

        public void setExcludeMap(ByteBuf[] targets, ByteBuf[] replacements) {
            for (int i = 0; i < targets.length; i++) {
                List<ByteBuf> excludes = new ArrayList<>(4);
                for (int j = 0; j < replacements.length; j++) {
                    if (i > j && contains(replacements[j], targets[i])) {
                        excludes.add(replacements[j]);
                    }
                }
                this.excludeMap.put(targets[i], excludes.toArray(new ByteBuf[]{}));
            }
        }

        private boolean contains(ByteBuf target, ByteBuf part) {
            ByteBuf buf = target.alloc().buffer(part.readableBytes());
            for (int i = 0; i < target.readableBytes(); i++) {
                target.getBytes(i, buf);
                if (buf.equals(part)) {
                    return true;
                }
                buf.clear();
            }
            return false;
        }
    }

    public static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf target, ByteBuf replacement, ByteBuf... excludes) {
        if (containsInvalidByteBuf(msgBuf, target, replacement)) return msgBuf;
        if (excludes.length != 0 && Arrays.binarySearch(excludes, target) != -1) {
            log.warn("It is not recommended to exclude target [{}], This will cause the escape to fail", target);
        }

        final ByteBuf result = msgBuf.alloc().buffer();

        int readIndex = 0;
        ByteBuf budgetBuffer = msgBuf.alloc().buffer(target.readableBytes());
        while (msgBuf.readableBytes() >= target.readableBytes()) {
            if (hasSimilarBytes(readIndex, msgBuf, target)) {
                // prepare for reset
                msgBuf.markReaderIndex();

                msgBuf.readBytes(budgetBuffer);

                if (budgetBuffer.equals(target) && !equalsAny(readIndex, msgBuf, excludes)) {
                    result.writeBytes(replacement.duplicate());

                    readIndex += target.readableBytes();
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
