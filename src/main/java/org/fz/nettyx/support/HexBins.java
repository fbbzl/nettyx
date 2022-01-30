package org.fz.nettyx.support;

import lombok.experimental.UtilityClass;

/**
 * @author fengbinbin
 * @since 2022-01-29 21:07
 **/
@UtilityClass
public class HexBins {

    private static final int BASE_LENGTH = 128;
    private static final int LOOKUP_LENGTH = 16;
    private static final byte[] hexNumberTable = new byte[BASE_LENGTH];
    private static final char[] lookUpHexAlphabet = new char[LOOKUP_LENGTH];

    static {
        for (int i = 0; i < BASE_LENGTH; i++) {
            hexNumberTable[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            hexNumberTable[i] = (byte) (i - '0');
        }
        for (int i = 'F'; i >= 'A'; i--) {
            hexNumberTable[i] = (byte) (i - 'A' + 10);
        }
        for (int i = 'f'; i >= 'a'; i--) {
            hexNumberTable[i] = (byte) (i - 'a' + 10);
        }

        for (int i = 0; i < 10; i++) {
            lookUpHexAlphabet[i] = (char) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            lookUpHexAlphabet[i] = (char) ('A' + i - 10);
        }
    }

    /**
     * Encode a byte array to hex string
     *
     * @param binaryData array of byte to encode
     * @return return encoded string
     */
    public static String encode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        int lengthEncode = lengthData * 2;
        char[] encodedData = new char[lengthEncode];
        int temp;
        for (int i = 0; i < lengthData; i++) {
            temp = binaryData[i];
            if (temp < 0) {
                temp += 256;
            }
            encodedData[i * 2] = lookUpHexAlphabet[temp >> 4];
            encodedData[i * 2 + 1] = lookUpHexAlphabet[temp & 0xf];
        }
        return new String(encodedData);
    }

    /**
     * Decode hex string to a byte array
     *
     * @param encoded encoded string
     * @return return array of byte to encode
     */
    public static byte[] decode(String encoded) {
        if (encoded == null) {
            return new byte[0];
        }
        int lengthData = encoded.length();
        if (lengthData % 2 != 0) {
            return new byte[0];
        }

        char[] binaryData = encoded.toCharArray();
        int lengthDecode = lengthData / 2;
        byte[] decodedData = new byte[lengthDecode];
        byte temp1, temp2;
        char tempChar;
        for (int i = 0; i < lengthDecode; i++) {
            tempChar = binaryData[i * 2];
            temp1 = (tempChar < BASE_LENGTH) ? hexNumberTable[tempChar] : -1;
            if (temp1 == -1) {
                return new byte[0];
            }
            tempChar = binaryData[i * 2 + 1];
            temp2 = (tempChar < BASE_LENGTH) ? hexNumberTable[tempChar] : -1;
            if (temp2 == -1) {
                return new byte[0];
            }
            decodedData[i] = (byte) ((temp1 << 4) | temp2);
        }
        return decodedData;
    }
}
