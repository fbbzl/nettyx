package org.fz.nettyx.codec;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;
import org.fz.nettyx.util.Throws;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.getBytes;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.util.stream.Collectors.toList;
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

    public EscapeCodec(EscapeMap escapeMap) {
        this(new EscapeDecoder(escapeMap), new EscapeEncoder(escapeMap));
    }

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
            ByteBuf decode = doEscape(in, escapeMap, REPLACEMENT, REALS);
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
            try {
                out.writeBytes(encoded);
            }
            finally {
                encoded.release();
            }
        }
    }

    @SuppressWarnings("all")
    public static class EscapeMap {

        public static final Function<Pair<ByteBuf, ByteBuf>, ByteBuf>
            REALS       = Pair::getKey,
            REPLACEMENT = Pair::getValue;

        @Getter
        private final Pair<ByteBuf, ByteBuf>[] mappings;

        private EscapeMap(Pair<ByteBuf, ByteBuf>... mappings) {
            checkMappings(mappings);

            this.mappings = mappings;
        }

        void checkMappings(Pair<ByteBuf, ByteBuf>[] mappings) {
            // 1 check if bytebuf is valid
            for (Pair<ByteBuf, ByteBuf> mapping : mappings) {
                ByteBuf real = mapping.getKey(), replacement = mapping.getValue();
                Throws.ifTrue(invalidByteBuf(real) || invalidByteBuf(replacement), "reals or replacements contains " +
                                                                                   "invalid buf," +
                                                                                   " please check");
            }

            // 2 check if has intersection
            List<ByteBuf> reals = Arrays.stream(mappings).map(Pair::getKey).collect(toList()),
                    replacements = Arrays.stream(mappings).map(Pair::getValue).collect(toList());
            Collection<ByteBuf> intersection = intersection(reals, replacements);
            Throws.ifNotEmpty(intersection, "do not let the reals intersect with the replacements, please check");

            // 3 check if replacements contains the reals
            for (ByteBuf real : reals) {
                for (ByteBuf replacement : replacements) {
                    Throws.ifTrue(containsContent(replacement, real), "do not let the replacements contain the reals");
                }
            }

        }

        public static EscapeMap mapHexPair(Pair<String, String>... realsReplacementsPair) {
            return mapPair(Arrays.stream(realsReplacementsPair).map(p -> Pair.of(HexKit.decodeBuf(p.getKey()),
                                                                                 HexKit.decodeBuf(p.getValue()))).toArray(Pair[]::new));
        }

        public static EscapeMap mapBytesPair(Pair<byte[], byte[]>... realsReplacementsPair) {
            return mapPair(Arrays.stream(realsReplacementsPair).map(p -> Pair.of(wrappedBuffer(p.getKey()),
                                                                                 wrappedBuffer(p.getValue()))).toArray(Pair[]::new));
        }

        public static EscapeMap mapPair(Pair<ByteBuf, ByteBuf>... realsReplacementsPair) {
            return new EscapeMap(realsReplacementsPair);
        }

        public static EscapeMap mapHex(String realHex, String replacementHex) {
            return map(HexKit.decodeBuf(realHex), HexKit.decodeBuf(replacementHex));
        }

        public static EscapeMap mapBytes(byte[] realBytes, byte[] replacementBytes) {
            return map(wrappedBuffer(realBytes), wrappedBuffer(replacementBytes));
        }

        public static EscapeMap map(ByteBuf real, ByteBuf replacement) {
            return new EscapeMap(Pair.of(real, replacement));
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

        public boolean equals(final Object o) {
            if (o == this) { return true; }
            if (!(o instanceof EscapeMap)) { return false; }
            final EscapeMap other = (EscapeMap) o;
            if (!other.canEqual((Object) this)) { return false; }
            if (!java.util.Arrays.deepEquals(this.mappings, other.mappings)) { return false; }
            return true;
        }

        protected boolean canEqual(final Object other) { return other instanceof EscapeMap; }

        public int hashCode() {
            final int PRIME  = 59;
            int       result = 1;
            result = result * PRIME + java.util.Arrays.deepHashCode(this.mappings);
            return result;
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
                        // only support one to one mapping
                        break;
                    }
                }
            }
            if (!match) escaped.writeByte(msgBuf.readByte());
        }
        return escaped;
    }

    static boolean equalsContent(byte[] bytes, ByteBuf buf) {
        if (bytes.length != buf.readableBytes()) {
            return false;
        }

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != buf.getByte(i)) {
                return false;
            }
        }

        return true;
    }

    static boolean hasSimilar(ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes(), readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1) { return sameHead; }

        boolean sameTail = msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
        return sameHead && sameTail;
    }
}
