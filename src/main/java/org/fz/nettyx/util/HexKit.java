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
 * @version 1.0
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
        if (hex.length() % 2 != 0) hex = "0" + hex;

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int offset = i * 2;
            result[i] = toByte(hex.substring(offset, offset + 2));
        }
        return result;
    }
}
