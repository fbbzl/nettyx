package org.fz.nettyx.codec;

import cn.hutool.core.lang.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;
import org.fz.nettyx.util.Throws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.hexDump;
import static java.util.Arrays.asList;

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
//            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
//                in = doEscape(in, bufEntry.getValue(), bufEntry.getKey());
//            }

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
//            for (Entry<ByteBuf, ByteBuf> bufEntry : escapeMap.entrySet()) {
//                msg = doEscape(msg, bufEntry.getKey(), bufEntry.getValue());
//            }

            out.writeBytes(msg);
            msg.release();
        }
    }

    /**
     * The type Escape map.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false)
    public static class EscapeMap {

        private static final ByteBuf[] EMPTY_BUFFER_ARRAY = new ByteBuf[0];

        private final Pair<ByteBuf, ByteBuf>[] mappings;

        public static EscapeMap mapEach(ByteBuf[] reals, ByteBuf[] replacements) {
            checkMappings(reals, replacements);

            List<Pair<ByteBuf, ByteBuf>> mappings = new ArrayList<>(reals.length);
            for (int i = 0; i < reals.length; i++) {
                mappings.add(Pair.of(reals[i], replacements[i]));
            }

            return new EscapeMap(mappings.toArray(new Pair[0]));
        }

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

        public static EscapeMap mapEachBytes(List<byte[]> reals, List<byte[]> replacements) {
            return mapEachBytes(reals.toArray(new byte[][]{}), replacements.toArray(new byte[][]{}));
        }

        public static EscapeMap mapEachBytes(byte[][] reals, byte[][] replacements) {
            ByteBuf[] realBuffers = Stream.of(reals)
                                          .map(Unpooled::wrappedBuffer)
                                          .toArray(ByteBuf[]::new),
                    replacementBuffers = Stream.of(replacements)
                                               .map(Unpooled::wrappedBuffer)
                                               .toArray(ByteBuf[]::new);

            return mapEach(realBuffers, replacementBuffers);
        }

        public static EscapeMap mapEach(List<ByteBuf> reals, List<ByteBuf> replacements) {
            return mapEach(reals.toArray(new ByteBuf[]{}), replacements.toArray(new ByteBuf[]{}));
        }

        public static EscapeMap mapHex(String realHex, String replacementHex) {
            return map(HexKit.decode(realHex), HexKit.decode(replacementHex));
        }

        public static EscapeMap map(byte[] realBytes, byte[] replacementBytes) {
            return map(Unpooled.wrappedBuffer(realBytes), Unpooled.wrappedBuffer(replacementBytes));
        }

        public static EscapeMap map(ByteBuf real, ByteBuf replacement) {
            return mapEach(new ByteBuf[]{real}, new ByteBuf[]{replacement});
        }

        static void checkMapping(ByteBuf target, ByteBuf replacement) {
            if (target == null || replacement == null)
                throw new IllegalArgumentException("neither the target nor the replacement can be null, please check");

            Throws.ifTrue(ByteBufUtil.equals(target, replacement), "target ["
                                                                   + hexDump(target) + "] can not be the " +
                                                                   "same as the replacement [" + hexDump(replacement) + "]");

            Throws.ifTrue(containsContent(replacement, target), "do not let replacement ["
                                                                + hexDump(replacement) +
                                                                "] contain target [" + hexDump(target) + "]");
        }

        static void checkMappings(ByteBuf[] reals, ByteBuf[] replacements) {
            if (reals.length != replacements.length) {
                throw new IllegalArgumentException(
                        "the count of the targets must be the same as the count of replacements, reals count is ["
                        + reals.length + "], the replacements length is [" + replacements.length + "]");
            }

            Collection<ByteBuf> intersection = intersection(asList(reals), asList(replacements));
            Throws.ifNotEmpty(intersection, "do not let the real data intersect with the replacement data");

            for (ByteBuf real : reals) {
                for (ByteBuf replacement : replacements) {
                    Throws.ifTrue(containsContent(replacement, real), "do not let the replacement data contain the " +
                                                                      "real data");
                }
            }
        }
    }

    protected static ByteBuf doEscape(ByteBuf msgBuf, ByteBuf target, ByteBuf replacement) {
        if (containsInvalidByteBuf(msgBuf, target, replacement)) { return msgBuf; }
        final ByteBuf result = msgBuf.alloc().buffer();

        int    readIndex = 0;
        byte[] sample    = new byte[target.readableBytes()];
        while (msgBuf.readableBytes() >= target.readableBytes()) {
            if (hasSimilar(readIndex, msgBuf, target)) {
                // prepare for reset
                msgBuf.markReaderIndex().readBytes(sample);

                if (equalsContent(sample, target)) {
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
        result.writeBytes(msgBuf);

        return result;
    }

    public static void main(String[] args) {
        EscapeMap escapeMap = EscapeMap.mapHex("7901", "ffffff");
        ByteBuf   byteBuf   = HexKit.decodeBuf("aaaa7901aa");

        ByteBuf encode = encode(byteBuf, escapeMap);
        System.err.println(hexDump(encode));
    }

    protected static ByteBuf encode(ByteBuf msgBuf, EscapeMap escapeMap) {
        try {
            Pair<ByteBuf, ByteBuf>[] mappings = escapeMap.getMappings();
            // new buf
            final ByteBuf result = msgBuf.alloc().buffer();

            while (msgBuf.readableBytes() > 0) {
                int readIndex = msgBuf.readerIndex();
                for (Pair<ByteBuf, ByteBuf> mapping : mappings) {
                    ByteBuf real        = mapping.getKey();
                    ByteBuf replacement = mapping.getValue();

                    int     realLength = real.readableBytes();
                    boolean match;
                    try {
                        switch (realLength) {
                            case 1:
                            case 2:
                                match = hasSimilar(readIndex, msgBuf, real);
                                break;
                            default:
                                match = hasSimilar(readIndex, msgBuf, real) && equalsContent(ByteBufUtil.getBytes(msgBuf,
                                                                                                                  readIndex, realLength), real);
                        }
                    }
                    catch (IndexOutOfBoundsException toTail) {
                        match = false;
                    }

                    if (match) {
                        result.writeBytes(replacement.duplicate());
                        msgBuf.skipBytes(realLength);
                    } else {
                        result.writeByte(msgBuf.readByte());
                    }
                }
            }
            return result;
        }
        finally {
            msgBuf.release();
        }
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

    private static boolean containsContent(ByteBuf buf, ByteBuf part) {
        if (buf.readableBytes() < part.readableBytes()) {
            return false;
        }

        byte[] sample = new byte[part.readableBytes()];
        for (int i = 0; i < buf.readableBytes(); i++) {
            buf.getBytes(i, sample);
            if (equalsContent(sample, part)) {
                return true;
            }
        }
        return false;
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

    private static boolean hasSimilar(int index, ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes();

        boolean sameHead = msgBuf.getByte(index) == target.getByte(0);
        if (tarLength == 1) { return sameHead; }

        boolean sameTail = msgBuf.getByte(index + tarLength - 1) == target.getByte(tarLength - 1);
        return sameHead && sameTail;
    }
}
