package escape;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.EscapeCodec.EscapeMapping;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.netty.buffer.ByteBufUtil.getBytes;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/5/21 10:38
 */
public class NettyxEscape {
    static final Function<EscapeMapping, ByteBuf>
            REAL        = EscapeMapping::getReal,
            REPLACEMENT = EscapeMapping::getReplacement;

    public static void main(String[] args) {
        ByteBuf in = Unpooled.copiedBuffer(new byte[]{ 0x01, 0x02, 0x7E, 0x04, 0x7D, 0x5E, 0x06, 0x07});
        EscapeMapping[] escapeMappings = { EscapeMapping.mapHex("7e", "7d5e") };

        StopWatch stopWatch = StopWatch.create("");
        stopWatch.start("escape");
        for (int i = 0; i < 5_000_000; i++) {
            ByteBuf decode = doEscape(in.duplicate(), escapeMappings, EscapeMapping::getReal, EscapeMapping::getReplacement);
            decode.release();
        }
        stopWatch.stop();

        Console.log("Nettyx " + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
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

    static ByteBuf doEscape(ByteBuf msgBuf,
                            EscapeMapping[] mappings,
                            Function<EscapeMapping, ByteBuf> targetFn,
                            Function<EscapeMapping, ByteBuf> replacementFn) {
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
            int tarLength,
            ByteBuf target) {

        return switch (tarLength) {
            case 1, 2 -> hasSimilar(msgBuf, target);
            default -> hasSimilar(msgBuf, target) && equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength),
                                                                   target);
        };
    }


    private static boolean hasSimilar(ByteBuf msgBuf, ByteBuf target) {
        int tarLength = target.readableBytes(), readerIndex = msgBuf.readerIndex();

        boolean sameHead = msgBuf.getByte(readerIndex) == target.getByte(0);
        if (tarLength == 1 || !sameHead) return sameHead;
        else return msgBuf.getByte(readerIndex + tarLength - 1) == target.getByte(tarLength - 1);
    }

}
