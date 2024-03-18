package org.fz.nettyx.codec;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.Throws;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.getBytes;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.codec.EscapeCodec.EscapeMap.*;
import static org.fz.nettyx.util.HexKit.decodeBuf;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
@Slf4j
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public static final Function<Pair<ByteBuf, ByteBuf>, ByteBuf>
            REALS       = Pair::getKey,
            REPLACEMENT = Pair::getValue;

    public EscapeCodec(EscapeMap escapeMap) {
        this(new EscapeDecoder(escapeMap), new EscapeEncoder(escapeMap));
    }

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder) {
        super(escapeDecoder, escapeEncoder);
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final Pair<ByteBuf, ByteBuf>[] mappings;

        public static EscapeDecoder mapHexPairs(Pair<String, String>... realsReplacementsPair) {
            return mapPairs(Arrays.stream(realsReplacementsPair).map(p -> Pair.of(decodeBuf(p.getKey()),
                                                                                  decodeBuf(p.getValue()))).toArray(Pair[]::new));
        }

        public static EscapeDecoder mapPairs(Pair<ByteBuf, ByteBuf>... realsReplacementsPair) {
            return new EscapeDecoder(realsReplacementsPair);
        }

        public static EscapeDecoder mapHex(String realHex, String replacementHex) {
            return map(decodeBuf(realHex), decodeBuf(replacementHex));
        }

        public static EscapeDecoder map(ByteBuf real, ByteBuf replacement) {
            return new EscapeDecoder(Pair.of(real, replacement));
        }

        public EscapeDecoder(Pair<ByteBuf, ByteBuf>... mappings) {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            ByteBuf decode = doEscape(in, mappings, REPLACEMENT, REALS);
            out.add(decode);
        }
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final Pair<ByteBuf, ByteBuf>[] mappings;

        public static EscapeEncoder mapHexPairs(Pair<String, String>... realsReplacementsPair) {
            return mapPairs(Arrays.stream(realsReplacementsPair).map(p -> Pair.of(decodeBuf(p.getKey()),
                                                                                  decodeBuf(p.getValue()))).toArray(Pair[]::new));
        }

        public static EscapeEncoder mapPairs(Pair<ByteBuf, ByteBuf>... realsReplacementsPair) {
            return new EscapeEncoder(realsReplacementsPair);
        }

        public static EscapeEncoder mapHex(String realHex, String replacementHex) {
            return map(decodeBuf(realHex), decodeBuf(replacementHex));
        }

        public static EscapeEncoder map(ByteBuf real, ByteBuf replacement) {
            return new EscapeEncoder(Pair.of(real, replacement));
        }

        public EscapeEncoder(Pair<ByteBuf, ByteBuf>... mappings) {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            ByteBuf encoded = doEscape(msg, mappings, REALS, REPLACEMENT);
            try {
                out.writeBytes(encoded);
            }
            finally {
                encoded.release();
            }
        }
    }

    static void checkMappings(Pair<ByteBuf, ByteBuf>[] mappings) {
        // 1 check if byte buf is valid
        for (Pair<ByteBuf, ByteBuf> mapping : mappings) {
            ByteBuf real = mapping.getKey(), replacement = mapping.getValue();
            Throws.ifTrue(invalid(real) || invalid(replacement), "reals or replacements contains " +
                                                                 "invalid buf, please check");
        }

        // 2 check if intersection is not empty
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


    static boolean invalid(ByteBuf buffer) {
        return buffer == null || !buffer.isReadable();
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

    static ByteBuf doEscape(ByteBuf msgBuf,
                            Pair<ByteBuf, ByteBuf>[] mappings,
                            Function<Pair<ByteBuf, ByteBuf>, ByteBuf> targetFn,
                            Function<Pair<ByteBuf, ByteBuf>, ByteBuf> replacementFn) {
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


    static boolean hasSimilar(ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes(), readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1) { return sameHead; }

        boolean sameTail = msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
        return sameHead && sameTail;
    }
}
