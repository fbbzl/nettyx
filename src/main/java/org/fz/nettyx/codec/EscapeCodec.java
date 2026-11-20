package org.fz.nettyx.codec;

import cn.hutool.core.map.BiMap;
import cn.hutool.core.util.PrimitiveArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.codec.EscapeCodec.EscapeDecoder;
import org.fz.nettyx.codec.EscapeCodec.EscapeEncoder;
import org.fz.nettyx.util.HexKit;

import java.util.*;
import java.util.Map.Entry;

import static cn.hutool.core.util.ArrayUtil.isEmpty;
import static io.netty.buffer.ByteBufUtil.getBytes;

/**
 * used to escape messages some sensitive characters can be replaced
 *
 * @author fengbinbin
 * @version 1.0
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
        if (escapeDecoder.getMap().isEmpty() || escapeEncoder.getMap().isEmpty()) throw new IllegalArgumentException("escape map must not be empty");
    }

    /**
     * Decodes configured escape sequences in inbound bytes.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2022-01-29
     */
    @SuppressWarnings("all")
    public static class EscapeDecoder extends ByteToMessageDecoder {

        private final EscapeMap map;

        public EscapeDecoder(EscapeMap map)
        {
            this.map = map.snapshot().getInverse();
        }

        public EscapeMap getMap() { return map.snapshot(); }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        {
            ByteBuf decode = null;
            try {
                decode = doEscape(in, map.biMap);
                out.add(decode);
            } catch (Exception e) {
                ReferenceCountUtil.release(decode);
                throw e;
            }
        }
    }

    /**
     * Replaces configured byte sequences with their escaped forms on write.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2022-01-29
     */
    @SuppressWarnings("all")
    public static class EscapeEncoder extends MessageToByteEncoder<ByteBuf> {

        private final EscapeMap map;

        public EscapeEncoder(EscapeMap map) {
            this.map = map.snapshot();
        }

        public EscapeMap getMap() { return map.snapshot(); }

        @Override
        protected void encode(
                ChannelHandlerContext ctx,
                ByteBuf               msg,
                ByteBuf               out)
        {
            ByteBuf encoded = doEscape(msg, map.biMap);
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
        if (isEmpty(map)) return msgBuf.retain();

        final ByteBuf escaped = msgBuf.alloc().buffer();
        try {
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
        } catch (Exception error) {
            ReferenceCountUtil.safeRelease(escaped);
            throw error;
        }
    }

    private static boolean overlook(
            ByteBuf msgBuf,
            byte[]  target,
            int     tarLength)
    {
        return switch (tarLength)
        {
            case 1, 2 -> hasSimilar(msgBuf, target, tarLength);
            default -> hasSimilar(msgBuf, target, tarLength)
                       &&
                       equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
        };
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
     * Maps original bytes to escaped bytes. An original sequence must not
     * contain another original sequence or any escaped sequence.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2022-01-29
     */
    public static class EscapeMap {
        private final BiMap<byte[], byte[]> biMap;
        private final boolean inverse;

        public EscapeMap() {
            this.biMap = new BiMap<>(new HashMap<>());
            this.inverse = false;
        }

        public EscapeMap(Map<byte[], byte[]> map) {
            this(map, false);
        }

        private EscapeMap(Map<byte[], byte[]> map, boolean inverse) {
            Map<byte[], byte[]> copy = new LinkedHashMap<>();
            map.forEach((real, replacement) -> copy.put(real == null ? null : real.clone(),
                                                        replacement == null ? null : replacement.clone()));
            this.biMap = new BiMap<>(copy);
            this.inverse = inverse;
            EscapeMapValidator.validate(biMap, inverse);
        }

        public int size() {
            return biMap.size();
        }

        public boolean isEmpty() {
            return biMap.isEmpty();
        }

        public byte[] put(byte[] real, byte[] replacement) {
            byte[] realCopy = real == null ? null : real.clone();
            byte[] replacementCopy = replacement == null ? null : replacement.clone();
            Map<byte[], byte[]> candidate = new LinkedHashMap<>(biMap);
            byte[] previous = null;
            byte[] existingKey = null;
            if (realCopy != null) {
                for (Entry<byte[], byte[]> entry : candidate.entrySet()) {
                    if (equalsContent(entry.getKey(), realCopy)) {
                        existingKey = entry.getKey();
                        previous = entry.getValue();
                        break;
                    }
                }
            }
            if (existingKey != null) candidate.remove(existingKey);
            candidate.put(realCopy, replacementCopy);
            EscapeMapValidator.validate(candidate, inverse);

            if (existingKey != null) biMap.remove(existingKey);
            biMap.put(realCopy, replacementCopy);
            return previous == null ? null : previous.clone();
        }

        public void putHex(String realHex, String replacementHex) {
            put(HexKit.decode(realHex), HexKit.decode(replacementHex));
        }

        public void putBuf(ByteBuf real, ByteBuf replacement) {
            byte[] realBytes        = new byte[real.readableBytes()],
                   replacementBytes = new byte[replacement.readableBytes()];

            real.readBytes(realBytes);
            replacement.readBytes(replacementBytes);

            put(realBytes, replacementBytes);
        }

        private EscapeMap snapshot() {
            return new EscapeMap(biMap, inverse);
        }

        public EscapeMap getInverse() {
            return new EscapeMap(biMap.getInverse(), !inverse);
        }

    }

    /**
     * Checks that escape mappings can be applied without ambiguity.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-18
     */
    private static final class EscapeMapValidator
    {
        private EscapeMapValidator() { }

        private static void validate(Map<byte[], byte[]> map, boolean inverse)
        {
            List<byte[]> reals = new ArrayList<>(map.size());
            List<byte[]> replacements = new ArrayList<>(map.size());
            // Apply containment rules to the original encoding direction.
            map.forEach((key, value) -> {
                byte[] real = inverse ? value : key;
                byte[] replacement = inverse ? key : value;
                Throws.ifTrue(PrimitiveArrayUtil.isEmpty(real) || PrimitiveArrayUtil.isEmpty(replacement),
                              () -> "reals or replacements contains invalid buf, please check");
                reals.add(real);
                replacements.add(replacement);
            });

            for (byte[] real : reals) {
                for (byte[] replacement : replacements) {
                    Throws.ifTrue(contains(real, replacement),
                                  () -> "original buffer " + Arrays.toString(real) + " must not contain replacement buffer " + Arrays.toString(replacement));
                }
            }

            for (int i = 0; i < replacements.size(); i++) {
                byte[] real = reals.get(i);
                byte[] replacement = replacements.get(i);
                for (int j = i + 1; j < replacements.size(); j++) {
                    byte[] otherReal = reals.get(j);
                    Throws.ifTrue(contains(real, otherReal) || contains(otherReal, real),
                                  () -> "original buffers must not contain one another: " + Arrays.toString(real) + " and " + Arrays.toString(otherReal));
                    byte[] other = replacements.get(j);
                    Throws.ifTrue(equalsContent(replacement, other),
                                  () -> "replacement buffers must be unique: " + Arrays.toString(replacement));
                    Throws.ifTrue(isPrefix(replacement, other) || isPrefix(other, replacement),
                                  () -> "replacement buffers must not overlap by prefix: " + Arrays.toString(replacement) + " and " + Arrays.toString(other));
                }
            }
        }

        private static boolean contains(byte[] value, byte[] part)
        {
            for (int offset = 0; offset <= value.length - part.length; offset++) {
                if (Arrays.equals(value, offset, offset + part.length, part, 0, part.length)) return true;
            }
            return false;
        }

        private static boolean isPrefix(byte[] prefix, byte[] value)
        {
            if (prefix.length >= value.length) return false;
            for (int i = 0; i < prefix.length; i++) {
                if (prefix[i] != value[i]) return false;
            }
            return true;
        }
    }
}
