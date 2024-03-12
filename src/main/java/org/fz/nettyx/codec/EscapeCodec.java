package org.fz.nettyx.codec;

import static cn.hutool.core.text.CharSequenceUtil.format;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayList;
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
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;
import org.fz.nettyx.util.Throws;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
@Slf4j
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    /**
     * Instantiates a new Escape codec.
     *
     * @param escapeMap the escape map
     */
    public EscapeCodec(EscapeMap escapeMap) {
        this(new EscapeDecoder(escapeMap), new EscapeEncoder(escapeMap));
    }

    /**
     * Instantiates a new Escape codec.
     *
     * @param escapeDecoder the escape decoder
     * @param escapeEncoder the escape encoder
     */
    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder) {
        super(escapeDecoder, escapeEncoder);
    }

    /**
     * using {@link EscapeMap} to deal message
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

    /**
     * The type Escape map.
     */
    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class EscapeMap extends HashMap<ByteBuf, ByteBuf> {

        private static final ByteBuf[] EMPTY_BUFFER_ARRAY = new ByteBuf[0];

        private final Map<ByteBuf, ByteBuf[]> excludeMap = new HashMap<>(4);

        /**
         * init with assigned capacity
         */
        private EscapeMap(int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Map each hex escape map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         *
         * @return the escape map
         */
        public static EscapeMap mapEachHex(List<String> reals, List<String> replacements) {
            return mapEachHex(reals.toArray(new String[]{}), replacements.toArray(new String[]{}));
        }

        /**
         * Escape bytebuffer as needed, and specify real data and replacement in order
         *
         * @param realHexes        The number and order of real buffers that need to be escaped should be the same as
         *                         that of replacementHexes
         * @param replacementHexes The number and order of replacement buffers should be the same as those of reals
         *
         * @return Buffer after escaped
         */
        public static EscapeMap mapEachHex(String[] realHexes, String[] replacementHexes) {
            ByteBuf[] realBuffers = Stream.of(realHexes).map(HexKit::decode).map(Unpooled::wrappedBuffer)
                                          .toArray(ByteBuf[]::new),
                replacementBuffers = Stream.of(replacementHexes).map(HexKit::decode).map(Unpooled::wrappedBuffer)
                                           .toArray(ByteBuf[]::new);

            return mapEach(realBuffers, replacementBuffers);
        }

        /**
         * Map each bytes escape map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         *
         * @return the escape map
         */
        public static EscapeMap mapEachBytes(List<byte[]> reals, List<byte[]> replacements) {
            return mapEachBytes(reals.toArray(new byte[][]{}), replacements.toArray(new byte[][]{}));
        }

        /**
         * Map each bytes escape map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         *
         * @return the escape map
         */
        public static EscapeMap mapEachBytes(byte[][] reals, byte[][] replacements) {
            ByteBuf[] realBuffers = Stream.of(reals).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new),
                replacementBuffers = Stream.of(replacements).map(Unpooled::wrappedBuffer).toArray(ByteBuf[]::new);

            return mapEach(realBuffers, replacementBuffers);
        }

        /**
         * Map each escape map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         *
         * @return the escape map
         */
        public static EscapeMap mapEach(List<ByteBuf> reals, List<ByteBuf> replacements) {
            return mapEach(reals.toArray(new ByteBuf[]{}), replacements.toArray(new ByteBuf[]{}));
        }

        /**
         * Map each escape map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         *
         * @return the escape map
         */
        public static EscapeMap mapEach(ByteBuf[] reals, ByteBuf[] replacements) {
            checkMapping(reals, replacements);

            EscapeMap escapeMap = new EscapeMap(reals.length);
            for (int i = 0; i < reals.length; i++) {
                escapeMap.put(reals[i], replacements[i]);
            }
            escapeMap.setExcludeMap(reals, replacements);

            return escapeMap;
        }

        /**
         * Map hex escape map.
         *
         * @param realHex        the real hex
         * @param replacementHex the replacement hex
         *
         * @return the escape map
         */
        public static EscapeMap mapHex(String realHex, String replacementHex) {
            return map(HexKit.decode(realHex), HexKit.decode(replacementHex));
        }

        /**
         * Map escape map.
         *
         * @param realBytes        the real bytes
         * @param replacementBytes the replacement bytes
         *
         * @return the escape map
         */
        public static EscapeMap map(byte[] realBytes, byte[] replacementBytes) {
            return map(Unpooled.wrappedBuffer(realBytes), Unpooled.wrappedBuffer(replacementBytes));
        }

        /**
         * Map escape map.
         *
         * @param real        the real
         * @param replacement the replacement
         *
         * @return the escape map
         */
        public static EscapeMap map(ByteBuf real, ByteBuf replacement) {
            EscapeMap escapeMap = new EscapeMap(1);
            escapeMap.put(real, replacement);

            escapeMap.setExcludeMap(new ByteBuf[]{real}, new ByteBuf[]{replacement});
            return escapeMap;
        }

        private static void checkMapping(Object[] reals, Object[] replacements) {
            if (reals.length != replacements.length) {
                throw new IllegalArgumentException(
                    "The reals data must be the same as the number of replacements data");
            }
        }

        /**
         * Get excludes byte buf [ ].
         *
         * @param real the real
         *
         * @return the byte buf [ ]
         */
        public ByteBuf[] getExcludes(ByteBuf real) {
            return getExcludeMap().getOrDefault(real, EMPTY_BUFFER_ARRAY);
        }

        /**
         * Sets exclude map.
         *
         * @param reals        the reals
         * @param replacements the replacements
         */
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
            byte[] sample = new byte[part.readableBytes()];
            for (int i = 0; i < real.readableBytes(); i++) {
                real.getBytes(i, sample);
                if (equalsContent(sample, part)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Do escape byte buf.
     *
     * @param msgBuf      the msg buf
     * @param target      the real buf
     * @param replacement the replacement
     * @param excludes    the excludes
     *
     * @return the byte buf
     */
    protected static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf target, ByteBuf replacement, ByteBuf... excludes) {
        try {
            if (containsInvalidByteBuf(msgBuf, target, replacement)) { return msgBuf; }
            Throws.ifTrue(excludes.length != 0 && ArrayUtil.contains(excludes, target),
                          format("It is not recommended to exclude real [{}], This will cause the escape to fail",
                                 target));

            final ByteBuf result = msgBuf.alloc().buffer();

            int    readIndex = 0;
            byte[] sample    = new byte[target.readableBytes()];
            while (msgBuf.readableBytes() >= target.readableBytes()) {
                if (similar(readIndex, msgBuf, target)) {
                    // prepare for reset
                    msgBuf.markReaderIndex().readBytes(sample);

                    if (equalsContent(sample, target) && !equalsAny(readIndex, msgBuf, excludes)) {
                        result.writeBytes(replacement.duplicate());

                        readIndex += target.readableBytes();
                    } else {
                        // if not equals, will reset the read index
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
                byte[] bytes = new byte[msgBuf.readableBytes()];
                msgBuf.readBytes(bytes);
                result.writeBytes(bytes);
            }

            return result;
        }
        finally {
            ReferenceCountUtil.release(msgBuf);
        }
    }

    private static boolean equalsContent(byte[] bytes, ByteBuf buf) {
        int readableBytes = buf.readableBytes();

        if (bytes.length != readableBytes) {
            return false;
        }

        // compare content
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != buf.getByte(i)) {
                return false;
            }
        }

        return true;
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
                }
                catch (IndexOutOfBoundsException indexOutOfBounds) {
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

    /**
     * check if it has the same header-byte and tail-byte
     *
     * @param msgBuf reading message buffer
     * @param target the target buffer
     */
    private static boolean similar(int index, ByteBuf msgBuf, ByteBuf target) {

        return msgBuf.getByte(index) == target.getByte(0)
               &&
               msgBuf.getByte(index + target.readableBytes() - 1) == target.getByte(target.readableBytes() - 1);
    }
}
