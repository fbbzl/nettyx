package org.fz.nettyx.codec;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import static java.util.stream.Collectors.toList;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public EscapeCodec(ByteBuf real, ByteBuf replacement) {
        this(new EscapeDecoder(EscapeMap.map(real, replacement)), new EscapeEncoder(EscapeMap.map(real, replacement)));
    }

    public EscapeCodec(String realHex, String replacementHex) {
        this(new EscapeDecoder(EscapeMap.mapHex(realHex, replacementHex)), new EscapeEncoder(EscapeMap.mapHex(realHex, replacementHex)));
    }

    public EscapeCodec(EscapeMap... realsReplacements) {
        this(new EscapeDecoder(realsReplacements), new EscapeEncoder(realsReplacements));
    }

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder) {
        super(escapeDecoder, escapeEncoder);
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap[] mappings;

        protected EscapeDecoder(EscapeMap... mappings) {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            ByteBuf decode = doEscape(in, mappings, EscapeMap::getReplacement, EscapeMap::getReal);
            out.add(decode);
        }
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap[] mappings;

        protected EscapeEncoder(EscapeMap... mappings) {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            ByteBuf encoded = doEscape(msg, mappings, EscapeMap::getReal, EscapeMap::getReplacement);
            try {
                out.writeBytes(encoded);
            } finally {
                encoded.release();
            }
        }
    }

    static void checkMappings(EscapeMap[] mappings) {
        // 1 check if byte buf is valid
        for (EscapeMap mapping : mappings) {
            ByteBuf real = mapping.getReal(), replacement = mapping.getReplacement();
            Throws.ifTrue(invalid(real) || invalid(replacement), "reals or replacements contains " +
                                                                 "invalid buf, please check");
        }

        // 2 check if intersection is not empty
        List<ByteBuf> reals = Arrays.stream(mappings).map(EscapeMap::getReal).collect(toList()),
                replacements = Arrays.stream(mappings).map(EscapeMap::getReplacement).collect(toList());
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
        if (buf.readableBytes() < part.readableBytes()) {
            return false;
        }

        byte[] sample = new byte[part.readableBytes()];
        for (int i = 0; i < buf.readableBytes(); i++) {
            if (buf.readableBytes() - i < sample.length) {
                return false;
            }
            buf.getBytes(i, sample);
            if (equalsContent(sample, part)) {
                return true;
            }
        }

        return false;
    }

    static ByteBuf doEscape(ByteBuf msgBuf,
                            EscapeMap[] mappings,
                            Function<EscapeMap, ByteBuf> targetFn,
                            Function<EscapeMap, ByteBuf> replacementFn) {
        if (ArrayUtil.isEmpty(mappings)) return msgBuf;

        final ByteBuf escaped = msgBuf.alloc().buffer();
        while (msgBuf.readableBytes() > 0) {
            boolean match = false;
            for (EscapeMap mapping : mappings) {
                ByteBuf target    = targetFn.apply(mapping);
                int     tarLength = target.readableBytes();

                if (msgBuf.readableBytes() >= tarLength) {
                    match = tryMatch(msgBuf, tarLength, target);

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

    private static boolean tryMatch(ByteBuf msgBuf, int tarLength, ByteBuf target) {
        boolean isMatch;
        switch (tarLength) {
            case 1:
            case 2:
                isMatch = hasSimilar(msgBuf, target);
                break;
            default:
                isMatch = hasSimilar(msgBuf, target) && equalsContent(
                        getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
                break;
        }

        return isMatch;
    }


    private static boolean hasSimilar(ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes(), readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1 || !sameHead) return sameHead;
        else                             return msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
    }

    @RequiredArgsConstructor
    public static class EscapeMap {

        private final Pair<ByteBuf, ByteBuf> mapping;

        public ByteBuf getReal() {
            return mapping.getKey();
        }

        public ByteBuf getReplacement() {
            return mapping.getValue();
        }

        public static EscapeMap map(ByteBuf real, ByteBuf replacement) {
            return new EscapeMap(Pair.of(real, replacement));
        }

        public static EscapeMap mapHex(String realHex, String replacementHex) {
            return map(HexKit.decodeBuf(realHex), HexKit.decodeBuf(replacementHex));
        }

        public static EscapeMap mapBytes(byte[] real, byte[] replacement) {
            return map(Unpooled.wrappedBuffer(real), Unpooled.wrappedBuffer(replacement));
        }

    }
}
