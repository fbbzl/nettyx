package org.fz.nettyx.util;

import lombok.experimental.UtilityClass;

/**
 * The type Hex bins.
 *
 * @author fengbinbin
 * @since 2022 -01-29 21:07
 */
@UtilityClass
public class HexKit {

    private static final int LOOKUP_LENGTH = 16;
    private static final char[] LOOK_UP_HEX_ALPHABET = new char[LOOKUP_LENGTH];

    static {
        for (int i = 0; i < 10; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('A' + i - 10);
        }
    }

    /**
     * To binary string.
     *
     * @param hex the hex
     * @return the string
     */
    public String toBinary(String hex) {
        if (hex == null || hex.length() % 2 != 0) return null;

        StringBuilder bString = new StringBuilder();
        String tmp;
        for (int i = 0; i < hex.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hex.substring(i, i + 1), 16));
            bString.append(tmp.substring(tmp.length() - 4));
        }

        return bString.toString();
    }

    /**
     * From binary string.
     *
     * @param binaryString the binary string
     * @return the string
     */
    public String fromBinary(String binaryString) {
        if (binaryString == null || binaryString.isEmpty() || binaryString.length() % 8 != 0) {
            return null;
        }
        StringBuilder tmp = new StringBuilder();
        int iTmp;
        for (int i = 0; i < binaryString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(binaryString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }

    /**
     * Encode a byte array to hex string
     *
     * @param bytes array of byte to encode
     * @return return encoded string
     */
    public static String encode(byte... bytes) {
        if (bytes == null) return null;

        int lengthData = bytes.length;
        int lengthEncode = lengthData * 2;
        char[] encodedData = new char[lengthEncode];
        int temp;
        for (int i = 0; i < lengthData; i++) {
            temp = bytes[i];
            if (temp < 0) {
                temp += 256;
            }
            encodedData[i * 2] = LOOK_UP_HEX_ALPHABET[temp >> 4];
            encodedData[i * 2 + 1] = LOOK_UP_HEX_ALPHABET[temp & 0xf];
        }
        return new String(encodedData);
    }

    /**
     * To byte byte.
     *
     * @param hex the hex
     * @return the byte
     */
    public static byte toByte(String hex) {
        return (byte) Integer.parseInt(hex, 16);
    }

    /**
     * Decode hex string to a byte array
     *
     * @param hex hex string
     * @return return array of byte to encode
     */
    public static byte[] decode(String hex) {
        int hexLen = hex.length();
        byte[] result;
        if (hexLen % 2 == 1) {
            //odd
            hexLen++;
            result = new byte[(hexLen / 2)];
            hex = "0" + hex;
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
