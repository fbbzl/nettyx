package org.fz.nettyx.codec;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.getBytes;
import static java.util.Arrays.asList;
import static org.fz.nettyx.codec.EscapeCodec.EscapeMap.REALS;
import static org.fz.nettyx.codec.EscapeCodec.EscapeMap.REPLACEMENT;

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
        protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
            ByteBuf decode = doEscape(msg, escapeMap, REPLACEMENT, REALS);
            out.add(decode);
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
            ByteBuf encoded = doEscape(msg, escapeMap, REALS, REPLACEMENT);
            out.writeBytes(encoded);
            encoded.release();
        }
    }

    /**
     * The type Escape map.
     */
    @Getter
    @SuppressWarnings("all")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false)
    public static class EscapeMap {

        public static final Function<Pair<ByteBuf, ByteBuf>, ByteBuf>
                REALS       = Pair::getKey,
                REPLACEMENT = Pair::getValue;

        static final ByteBuf[] EMPTY_BUFFER_ARRAY = new ByteBuf[0];

        private final Pair<ByteBuf, ByteBuf>[] mappings;

        public static EscapeMap mapEach(ByteBuf[] reals, ByteBuf[] replacements) {
            // distinct each
            reals        = ArrayUtil.distinct(reals);
            replacements = ArrayUtil.distinct(replacements);

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

        static void checkMappings(ByteBuf[] reals, ByteBuf[] replacements) {
            if (reals.length != replacements.length) {
                throw new IllegalArgumentException(
                        "the count of the reals must be the same as the count of replacements, reals count is ["
                        + reals.length + "], the replacements count is [" + replacements.length + "]");
            }

            Throws.ifTrue(containsInvalidByteBuf(reals) || containsInvalidByteBuf(replacements), "reals or " +
                                                                                                 "replacements " +
                                                                                                 "contains invalid " +
                                                                                                 "buf, please check");

            Collection<ByteBuf> intersection = intersection(asList(reals), asList(replacements));
            Throws.ifNotEmpty(intersection, "do not let the reals intersect with the replacements, please check");

            for (ByteBuf real : reals) {
                for (ByteBuf replacement : replacements) {
                    Throws.ifTrue(containsContent(replacement, real), "do not let the replacements contain the reals");
                }
            }
        }

        static boolean containsContent(ByteBuf buf, ByteBuf part) {
            if (buf.readableBytes() < part.readableBytes()) { return false; }

            byte[] sample = new byte[part.readableBytes()];
            for (int i = 0; i < buf.readableBytes(); i++) {
                if (buf.readableBytes() - i < sample.length) {
                    return false;
                }
                buf.getBytes(i, sample);
                if (equalsContent(sample, part)) { return true; }
            }

            return false;
        }

        static boolean invalidByteBuf(ByteBuf buffer) {
            return buffer == null || !buffer.isReadable();
        }

        static boolean containsInvalidByteBuf(ByteBuf... buffers) {
            for (ByteBuf byteBuf : buffers) {
                if (invalidByteBuf(byteBuf)) {
                    return true;
                }
            }
            return false;
        }
    }

    protected static ByteBuf doEscape(ByteBuf msgBuf,
                                      EscapeMap escapeMap,
                                      Function<Pair<ByteBuf, ByteBuf>, ByteBuf> targetFn,
                                      Function<Pair<ByteBuf, ByteBuf>, ByteBuf> replacementFn) {
        Pair<ByteBuf, ByteBuf>[] mappings = escapeMap.getMappings();
        if (ArrayUtil.isEmpty(mappings)) return msgBuf;

        final ByteBuf escaped = msgBuf.alloc().buffer();
        while (msgBuf.readableBytes() > 0) {
            boolean match = false;
            for (Pair<ByteBuf, ByteBuf> mapping : mappings) {
                ByteBuf target    = targetFn.apply(mapping);
                int     tarLength = target.readableBytes();

                if (msgBuf.readableBytes() >= tarLength) {
                    switch (tarLength) {
                        case 1:
                        case 2:
                            match = hasSimilar(msgBuf, target);
                            break;
                        default:
                            match = hasSimilar(msgBuf, target) && equalsContent(
                                    getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
                            break;
                    }

                    if (match) {
                        msgBuf.skipBytes(tarLength);
                        escaped.writeBytes(replacementFn.apply(mapping).duplicate());
                        // only support single same, so if match we break
                        break;
                    }
                }
            }
            if (!match) escaped.writeByte(msgBuf.readByte());
        }
        return escaped;
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

    private static boolean hasSimilar(ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes();

        boolean sameHead = msgBuf.getByte(msgBuf.readerIndex()) == target.getByte(0);
        if (tarLength == 1) { return sameHead; }

        boolean sameTail = msgBuf.getByte(msgBuf.readerIndex() + tarLength - 1) == target.getByte(tarLength - 1);
        return sameHead && sameTail;
    }
}
