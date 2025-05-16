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
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.getBytes;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.codec.EscapeCodec.EscapeMapping.REAL;
import static org.fz.nettyx.codec.EscapeCodec.EscapeMapping.REPLACEMENT;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public EscapeCodec(ByteBuf real, ByteBuf replacement)
    {
        this(new EscapeDecoder(EscapeMapping.map(real, replacement)), new EscapeEncoder(EscapeMapping.map(real, replacement)));
    }

    public EscapeCodec(String realHex, String replacementHex)
    {
        this(new EscapeDecoder(EscapeMapping.mapHex(realHex, replacementHex)), new EscapeEncoder(EscapeMapping.mapHex(realHex, replacementHex)));
    }

    public EscapeCodec(EscapeMapping... realsReplacements)
    {
        this(new EscapeDecoder(realsReplacements), new EscapeEncoder(realsReplacements));
    }

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder)
    {
        super(escapeDecoder, escapeEncoder);
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMapping[] mappings;

        protected EscapeDecoder(EscapeMapping... mappings)
        {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        {
            ByteBuf decode = doEscape(in, mappings, REPLACEMENT, REAL);
            out.add(decode);
        }
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMapping[] mappings;

        protected EscapeEncoder(EscapeMapping... mappings)
        {
            checkMappings(mappings);
            this.mappings = mappings;
        }

        @Override
        protected void encode(
                ChannelHandlerContext ctx,
                ByteBuf               msg,
                ByteBuf               out)
        {
            ByteBuf encoded = doEscape(msg, mappings, REAL, REPLACEMENT);
            try {
                out.writeBytes(encoded);
            } finally {
                encoded.release();
            }
        }
    }

    static void checkMappings(EscapeMapping[] mappings)
    {
        // 1 check if byte buf is valid
        for (EscapeMapping mapping : mappings) {
            ByteBuf real = mapping.getReal(), replacement = mapping.getReplacement();
            Throws.ifTrue(invalid(real) || invalid(replacement),
                          () -> "reals or replacements contains invalid buf, please check");
        }

        // 2 check if intersection is not empty
        List<ByteBuf> reals = Arrays.stream(mappings).map(REAL).collect(toList()),
                replacements = Arrays.stream(mappings).map(REPLACEMENT).collect(toList());
        Collection<ByteBuf> intersection = intersection(reals, replacements);
        Throws.ifNotEmpty(intersection, () -> "do not let the reals intersect with the replacements, please check");

        // 3 check if replacements contains the reals
        for (ByteBuf real : reals) {
            for (ByteBuf replacement : replacements) {
                Throws.ifTrue(containsContent(replacement, real),
                              () -> "do not let the replacements: [" + replacement + "] contain the reals: [" + real + "]");
            }
        }

    }

    static boolean equalsContent(
            byte[]  bytes,
            ByteBuf buf)
    {
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


    static boolean invalid(ByteBuf buffer)
    {
        return buffer == null || !buffer.isReadable();
    }

    /**
     * if buf contains part-buf
     * @param buf the source buf
     * @param part the part buf
     */
    static boolean containsContent(
            ByteBuf buf,
            ByteBuf part)
    {
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

    static ByteBuf doEscape(ByteBuf                          msgBuf,
                            EscapeMapping[]                  mappings,
                            Function<EscapeMapping, ByteBuf> targetFn,
                            Function<EscapeMapping, ByteBuf> replacementFn)
    {
        if (ArrayUtil.isEmpty(mappings)) return msgBuf;

        final ByteBuf escaped = msgBuf.alloc().buffer();
        while (msgBuf.readableBytes() > 0) {
            boolean match = false;
            for (EscapeMapping mapping : mappings) {
                ByteBuf target    = targetFn.apply(mapping);
                int     tarLength = target.readableBytes();

                if (msgBuf.readableBytes() >= tarLength) {
                    match = overlook(msgBuf, tarLength, target);

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

    private static boolean overlook(
            ByteBuf msgBuf,
            int     tarLength,
            ByteBuf target)
    {

        return switch (tarLength) {
            case 1, 2 -> hasSimilar(msgBuf, target);
            default   -> hasSimilar(msgBuf, target) && equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
        };
    }


    private static boolean hasSimilar(
            ByteBuf msgBuf,
            ByteBuf target)
    {
        int tarLength = target.readableBytes(), readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1 || !sameHead) return sameHead;
        else                             return msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
    }

    @RequiredArgsConstructor
    public static class EscapeMapping {

        static final Function<EscapeMapping, ByteBuf>
                REAL        = EscapeMapping::getReal,
                REPLACEMENT = EscapeMapping::getReplacement;

        private final Pair<ByteBuf, ByteBuf> mapping;

        public ByteBuf getReal()
        {
            return mapping.getKey();
        }

        public ByteBuf getReplacement()
        {
            return mapping.getValue();
        }

        public static EscapeMapping map(ByteBuf real, ByteBuf replacement) {
            return new EscapeMapping(Pair.of(real, replacement));
        }

        public static EscapeMapping mapHex(String realHex, String replacementHex)
        {
            return map(HexKit.decodeBuf(realHex), HexKit.decodeBuf(replacementHex));
        }

        public static EscapeMapping mapBytes(byte[] real, byte[] replacement)
        {
            return map(Unpooled.wrappedBuffer(real), Unpooled.wrappedBuffer(replacement));
        }

    }
}
