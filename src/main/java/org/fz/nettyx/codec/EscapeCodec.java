package org.fz.nettyx.codec;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;
import org.fz.nettyx.util.Throws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static io.netty.buffer.ByteBufUtil.hexDump;

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
            msg.release();
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
            ByteBuf[] realBuffers = Stream.of(realHexes)
                                          .map(HexKit::decode)
                                          .map(Unpooled::wrappedBuffer)
                                          .toArray(ByteBuf[]::new),
                    replacementBuffers = Stream.of(replacementHexes)
                                               .map(HexKit::decode)
                                               .map(Unpooled::wrappedBuffer)
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
            ByteBuf[] realBuffers = Stream.of(reals)
                                          .map(Unpooled::wrappedBuffer)
                                          .toArray(ByteBuf[]::new),
                    replacementBuffers = Stream.of(replacements)
                                               .map(Unpooled::wrappedBuffer)
                                               .toArray(ByteBuf[]::new);

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
            checkMappings(reals, replacements);

            EscapeMap escapeMap = new EscapeMap(reals.length);
            for (int i = 0; i < reals.length; i++) {
                escapeMap.put(reals[i], replacements[i]);
            }

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

        public static EscapeMap map(ByteBuf target, ByteBuf replacement) {
            checkMapping(target, replacement);

            EscapeMap escapeMap = new EscapeMap(1);
            escapeMap.put(target, replacement);

            return escapeMap;
        }

        static void checkMapping(ByteBuf target, ByteBuf replacement) {
            if (target == null || replacement == null)
                throw new IllegalArgumentException("neither the target nor the replacement can be null, please check");

            Throws.ifTrue(ByteBufUtil.equals(target, replacement), "target [" + hexDump(target) + "] can not be the " +
                                                                   "same as the replacement [" + hexDump(replacement) + "]");
        }

        private static void checkMappings(ByteBuf[] reals, ByteBuf[] replacements) {
            if (reals.length != replacements.length) {
                throw new IllegalArgumentException(
                        "the count of the targets must be the same as the count of replacements");
            }

            final Map<ByteBuf, ByteBuf[]> excludeMap = new HashMap<>(4);

            for (int i = 0; i < reals.length; i++) {
                List<ByteBuf> excludes = new ArrayList<>(4);
                for (int j = 0; j < replacements.length; j++) {
                    if (i > j && contains(replacements[j], reals[i])) {
                        excludes.add(replacements[j]);
                    }
                }
                excludeMap.put(reals[i], excludes.toArray(new ByteBuf[]{}));
            }

        }

        static boolean contains(ByteBuf real, ByteBuf part) {
            if (real.readableBytes() < part.readableBytes()) {
                return false;
            }

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
     */
    protected static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf target, ByteBuf replacement) {
        if (containsInvalidByteBuf(msgBuf, target, replacement)) { return msgBuf; }
        final ByteBuf result = msgBuf.alloc().buffer();

        int    readIndex = 0;
        byte[] sample    = new byte[target.readableBytes()];
        while (msgBuf.readableBytes() >= target.readableBytes()) {
            if (isSimilar(readIndex, msgBuf, target)) {
                // prepare for reset
                msgBuf.markReaderIndex().readBytes(sample);

                if (equalsContent(sample, target)) {
                    result.writeBytes(replacement.duplicate());

                    readIndex += target.readableBytes();
                }
                else {
                    // if not equals, will reset the read index
                    msgBuf.resetReaderIndex();

                    result.writeByte(msgBuf.readByte());
                    readIndex++;
                }

            }
            else {
                result.writeByte(msgBuf.readByte());
                readIndex++;
            }
        }

        // write the left buffer
        result.writeBytes(msgBuf);

        return result;
    }

    //
    protected static ByteBuf doEscape(ByteBuf msgBuf, EscapeMap escapeMap) {
        if (containsInvalidByteBuf(msgBuf, target, replacement)) { return msgBuf; }
        Throws.ifTrue(excludes.length != 0 && ArrayUtil.contains(excludes, target),
                      format("do not exclude real [{}], this will cause the escape to fail", target));
        final ByteBuf result = msgBuf.alloc().buffer();

        int    readIndex = 0;
        byte[] sample    = new byte[target.readableBytes()];
        while (msgBuf.readableBytes() >= target.readableBytes()) {
            if (isSimilar(readIndex, msgBuf, target)) {
                // prepare for reset
                msgBuf.markReaderIndex().readBytes(sample);

                if (equalsContent(sample, target) && !equalsAny(readIndex, msgBuf, excludes)) {
                    result.writeBytes(replacement.duplicate());

                    readIndex += target.readableBytes();
                }
                else {
                    // if not equals, will reset the read index
                    msgBuf.resetReaderIndex();

                    result.writeByte(msgBuf.readByte());
                    readIndex++;
                }

            }
            else {
                result.writeByte(msgBuf.readByte());
                readIndex++;
            }
        }

        // write the left buffer
        result.writeBytes(msgBuf);

        return result;
    }

    private static boolean equalsContent(byte[] bytes, ByteBuf buf) {
        if (bytes.length != buf.readableBytes()) {
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

    private static boolean isSimilar(int index, ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes();

        boolean sameHead = msgBuf.getByte(index) == target.getByte(0);
        if (tarLength == 1) { return sameHead; }

        boolean sameTail = msgBuf.getByte(index + tarLength - 1) == target.getByte(tarLength - 1);
        return sameHead && sameTail;
    }
}
