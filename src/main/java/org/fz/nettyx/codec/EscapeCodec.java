package org.fz.nettyx.codec;

import cn.hutool.core.map.BiMap;
import cn.hutool.core.util.PrimitiveArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;

import java.util.*;
import java.util.Map.Entry;

import static cn.hutool.core.collection.CollUtil.intersection;
import static cn.hutool.core.util.ArrayUtil.isEmpty;
import static io.netty.buffer.ByteBufUtil.getBytes;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @since 2022 -01-27 18:07
 */
public class EscapeCodec extends CombinedChannelDuplexHandler<EscapeDecoder, EscapeEncoder> {

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

        public EscapeDecoder(EscapeMap map)
        {
            this.map = map.getInverse();
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        {
            ByteBuf decode = doEscape(in, map);
            out.add(decode);
        }
    }

    @Getter
    @RequiredArgsConstructor
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap map;

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

    static boolean equalsContent(byte[] bytes, byte[] buf)
    {
        if (bytes.length != buf.length) {
            return false;
        }

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != buf[i]) {
                return false;
            }
        }

        return true;
    }

    static ByteBuf doEscape(ByteBuf msgBuf, Map<byte[], byte[]> map)
    {
        if (isEmpty(map)) return msgBuf;

        final ByteBuf escaped = msgBuf.alloc().buffer();
        while (msgBuf.readableBytes() > 0) {
            boolean match = false;
            for (Entry<byte[], byte[]> entry : map.entrySet()) {
                byte[] target    = entry.getKey();
                int    tarLength = target.length;

                if (msgBuf.readableBytes() >= tarLength) {
                    match = overlook(msgBuf, target, tarLength);

                    if (match) {
                        msgBuf.skipBytes(tarLength);
                        escaped.writeBytes(entry.getValue());
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
            byte[]  target,
            int     tarLength)
    {
        switch (tarLength) {
            case 1:
            case 2: return hasSimilar(msgBuf, target, tarLength);
            default: return hasSimilar(msgBuf, target, tarLength)
                            &&
                            equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
        }
    }

    private static boolean hasSimilar(
            ByteBuf msgBuf,
            byte[]  target,
            int     tarLength)
    {
        int readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target[0];
        if (tarLength == 1 || !sameHead) return sameHead;
        else                             return msgBuf.getByte(readerIndex + tarLength - 1) == target[tarLength - 1];
    }

    /**
     * key is the real buf, and the value is the replacement buf
     */
    public static class EscapeMap implements Map<byte[], byte[]> {
        @Delegate
        private final BiMap<byte[], byte[]> biMap;

        public EscapeMap() {
            this.biMap = new BiMap<>(new HashMap<>());
        }

        public EscapeMap(Map<byte[], byte[]> map) {
            this.biMap = new BiMap<>(map);
        }

        public void putHex(String realHex, String replacementHex) {
            putIfAbsent(HexKit.decode(realHex), HexKit.decode(replacementHex));
            checkEscapeMap();
        }

        public void putBuf(ByteBuf real, ByteBuf replacement) {
            byte[] realBytes        = new byte[real.readableBytes()],
                   replacementBytes = new byte[replacement.readableBytes()];
            putIfAbsent(realBytes, replacementBytes);
            checkEscapeMap();
        }

        public EscapeMap getInverse() {
            return new EscapeMap(biMap.getInverse());
        }

        void checkEscapeMap()
        {
            // 1 check if byte buf is valid
            for (Entry<byte[], byte[]> entry : this.entrySet()) {
                byte[] real = entry.getKey(), replacement = entry.getKey();
                Throws.ifTrue(PrimitiveArrayUtil.isEmpty(real) || PrimitiveArrayUtil.isEmpty(replacement),
                              () -> "reals or replacements contains invalid buf, please check");
            }

            // 2 check if intersection is not empty
            List<byte[]>
                    reals        = new ArrayList<>(this.keySet()),
                    replacements = new ArrayList<>(this.values());

            Collection<byte[]> intersection = intersection(reals, replacements);
            Throws.ifNotEmpty(intersection, () -> "do not let the reals intersect with the replacements, please check");

            // 3 check if replacements contains the reals
            for (byte[] real : reals) {
                for (byte[] replacement : replacements) {
                    Throws.ifTrue(equalsContent(replacement, real),
                                  () -> "do not let the replacements: " + Arrays.toString(replacement) + " same as the reals: " + Arrays.toString(real));
                }
            }
        }
    }
}
