package org.fz.nettyx.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

import java.util.HexFormat;

/**
 * The Hex util.
 *
 * @author fengbinbin
 * @since 2022 -01-29 21:07
 */
@UtilityClass
public class HexKit {

    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    public static String encode(byte... bytes)
    {
        return bytes == null ? null : HEX_FORMAT.formatHex(bytes);
    }

    public static String encode(ByteBuf buf)
    {
        return ByteBufUtil.hexDump(buf);
    }

    public static byte toByte(String hex)
    {
        return (byte) Integer.parseInt(hex, 16);
    }

    public static ByteBuf decodeBuf(String hex)
    {
        return Unpooled.wrappedBuffer(decode(hex));
    }

    public static byte[] decode(String hex)
    {
        if (hex == null) return new byte[0];
        int    hexLen = hex.length();
        byte[] result;
        if (hexLen % 2 == 1) {
            //odd
            hexLen++;
            result = new byte[(hexLen / 2)];
            hex    = "0" + hex;
        } else {
            //even
            result = new byte[(hexLen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexLen; i += 2) {
            result[j] = toByte(hex.substring(i, i + 2));
            j++;
        }

        return result;
    }
}
