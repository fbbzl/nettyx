package org.fz.nettyx.codec;

import cn.hutool.core.map.BiMap;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static cn.hutool.core.collection.CollUtil.intersection;
import static io.netty.buffer.ByteBufUtil.getBytes;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

    public EscapeCodec(ByteBuf real, ByteBuf replacement)
    {
        this(new EscapeDecoder(EscapeMap.map(real, replacement)), new EscapeEncoder(EscapeMap.map(real, replacement)));
    }

    public EscapeCodec(String realHex, String replacementHex)
    {
        this(new EscapeDecoder(EscapeMap.mapHex(realHex, replacementHex)), new EscapeEncoder(EscapeMap.mapHex(realHex, replacementHex)));
    }

    public EscapeCodec(EscapeMap map)
    {
        this(new EscapeDecoder(map), new EscapeEncoder(map));
    }

    public EscapeCodec(EscapeDecoder escapeDecoder, EscapeEncoder escapeEncoder)
    {
        super(escapeDecoder, escapeEncoder);
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap map;

        protected EscapeDecoder(EscapeMap map)
        {
            checkEscapeMap(map);
            this.map = map;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        {
            ByteBuf decode = doEscape(in, map.getInverse());
            out.add(decode);
        }
    }

    @Getter
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap map;

        protected EscapeEncoder(EscapeMap map)
        {
            checkEscapeMap(map);
            this.map = map;
        }

        @Override
        protected void encode(
                ChannelHandlerContext ctx,
                ByteBuf               msg,
                ByteBuf               out)
        {
            ByteBuf encoded = doEscape(msg, map);
            try {
                out.writeBytes(encoded);
            } finally {
                encoded.release();
            }
        }
    }

    static void checkEscapeMap(EscapeMap map)
    {
        // 1 check if byte buf is valid
        for (Entry<ByteBuf, ByteBuf> entry : map.entrySet()) {
            ByteBuf real = entry.getKey(), replacement = entry.getKey();
            Throws.ifTrue(invalid(real) || invalid(replacement),
                          () -> "reals or replacements contains invalid buf, please check");
        }

        // 2 check if intersection is not empty
        List<ByteBuf>
                reals        = new ArrayList<>(map.keySet()),
                replacements = new ArrayList<>(map.values());
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

    static boolean equalsContent(byte[] bytes, ByteBuf buf)
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
    static boolean containsContent(ByteBuf buf, ByteBuf part)
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

    static ByteBuf doEscape(ByteBuf msgBuf, Map<ByteBuf, ByteBuf> map)
    {
        if (ArrayUtil.isEmpty(map)) return msgBuf;

        final ByteBuf escaped = msgBuf.alloc().buffer();
        while (msgBuf.readableBytes() > 0) {
            boolean match = false;
            for (Entry<ByteBuf, ByteBuf> entry : map.entrySet()) {
                ByteBuf target    = entry.getKey();
                int     tarLength = target.readableBytes();

                if (msgBuf.readableBytes() >= tarLength) {
                    match = overlook(msgBuf, target, tarLength);

                    if (match) {
                        msgBuf.skipBytes(tarLength);
                        escaped.writeBytes(entry.getValue().duplicate());
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
            ByteBuf target,
            int     tarLength)
    {
        return switch (tarLength) {
            case 1, 2 -> hasSimilar(msgBuf, target, tarLength);
            default   -> hasSimilar(msgBuf, target, tarLength) && equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
        };
    }

    private static boolean hasSimilar(
            ByteBuf msgBuf,
            ByteBuf target,
            int     tarLength)
    {
        int readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1 || !sameHead) return sameHead;
        else                             return msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
    }

    /**
     * key is the real buf, and the value is the replacement buf
     */
    public static class EscapeMap extends BiMap<ByteBuf, ByteBuf> {

        public EscapeMap(Map<ByteBuf, ByteBuf> raw) {
            super(raw);
        }

        public static EscapeMap map(ByteBuf real, ByteBuf replacement)
        {
            return new EscapeMap(Map.of(real, replacement));
        }

        public static EscapeMap mapHex(String realHex, String replacementHex)
        {
            return map(HexKit.decodeBuf(realHex), HexKit.decodeBuf(replacementHex));
        }

        public static EscapeMap mapBytes(byte[] real, byte[] replacement)
        {
            return map(Unpooled.wrappedBuffer(real), Unpooled.wrappedBuffer(replacement));
        }
    }
}
