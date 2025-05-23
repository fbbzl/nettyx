package escape;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.fz.nettyx.util.HexKit;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.util.ArrayUtil.isEmpty;
import static io.netty.buffer.ByteBufUtil.getBytes;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/5/21 10:38
 */
public class NettyxEscape {

    public static void main(String[] args) {
        ByteBuf   in        = Unpooled.copiedBuffer(HexKit.decode("07e27e047d5e0607"));
        EscapeMap escapeMap =  new EscapeMap();
        escapeMap.putHex("7e", "7d5e");

        StopWatch stopWatch = StopWatch.create("");
        stopWatch.start("escape");
        for (int i = 0; i < 1; i++) {
            ByteBuf decode = doEscape(in.duplicate(), escapeMap.getInverse());
            System.err.println(HexKit.encode(decode));
            decode.release();
        }
        stopWatch.stop();

        Console.log("Nettyx " + stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
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

        final ByteBuf escaped = Unpooled.buffer();
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
        return switch (tarLength) {
            case 1, 2 -> hasSimilar(msgBuf, target, tarLength);
            default   -> hasSimilar(msgBuf, target, tarLength) && equalsContent(getBytes(msgBuf, msgBuf.readerIndex(), tarLength), target);
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


}
