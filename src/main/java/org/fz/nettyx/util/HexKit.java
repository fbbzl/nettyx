package org.fz.nettyx.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

/**
 * The Hex util.
 *
 * @author fengbinbin
 * @since 2022 -01-29 21:07
 */
@UtilityClass
public class HexKit {

    private static final int    LOOKUP_LENGTH        = 16;
    private static final char[] LOOK_UP_HEX_ALPHABET = new char[LOOKUP_LENGTH];

    static {
        for (int i = 0; i < 10; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('A' + i - 10);
        }
    }

    public static String encode(byte... bytes)
    {
        if (bytes == null) return null;

        int    lengthData   = bytes.length;
        int    lengthEncode = lengthData * 2;
        char[] encodedData  = new char[lengthEncode];
        int    temp;
        for (int i = 0; i < lengthData; i++) {
            temp = bytes[i];
            if (temp < 0) {
                temp += 256;
            }
            encodedData[i * 2]     = LOOK_UP_HEX_ALPHABET[temp >> 4];
            encodedData[i * 2 + 1] = LOOK_UP_HEX_ALPHABET[temp & 0xf];
        }
        return new String(encodedData);
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
