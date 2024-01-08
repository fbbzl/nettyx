package org.fz.nettyx.util;

import static java.lang.Integer.min;
import static org.apache.logging.log4j.util.Constants.EMPTY_BYTE_ARRAY;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;

/**
 * bytes util
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2020/12/18 15:19
 */
@UtilityClass
public class BytesKit {

    public static final BigEndian be = new BigEndian();
    public static final LittleEndian le = new LittleEndian();
    public static final int KB = 1024;
    public static class BigEndian {

        private BigEndian() { }

        public byte[] fromByteValue(byte bite) {
            byte[] bytes = new byte[1];
            bytes[0] = bite;

            return bytes;
        }

        public byte toByteValue(byte[] bytes) { return bytes[0]; }

        public byte[] fromShortValue(short s) {
            byte[] b = new byte[2];

            b[1] = (byte) (s & 0xff);
            b[0] = (byte) (s >> 8 & 0xff);

            return b;
        }

        public short toShortValue(byte[] bytes) { return (short) ((bytes[0] << 8) | bytes[1] & 0xff); }

        public byte[] fromCharValue(char chr) { return fromShortValue((short) chr); }

        public char toCharValue(byte[] bytes) { return (char) toShortValue(bytes); }

        public byte[] fromIntValue(int i) {
            byte[] b = new byte[4];

            b[3] = (byte) (i & 0xff);
            b[2] = (byte) (i >> 8 & 0xff);
            b[1] = (byte) (i >> 16 & 0xff);
            b[0] = (byte) (i >> 24 & 0xff);

            return b;
        }

        public int toIntValue(byte[] bytes) {
            int int1 = bytes[3] & 0xff;
            int int2 = (bytes[2] & 0xff) << 8;
            int int3 = (bytes[1] & 0xff) << 16;
            int int4 = (bytes[0] & 0xff) << 24;

            return int1 | int2 | int3 | int4;
        }

        public byte[] fromLongValue(long l) {
            byte[] b = new byte[8];

            b[7] = (byte) (l & 0xff);
            b[6] = (byte) (l >> 8 & 0xff);
            b[5] = (byte) (l >> 16 & 0xff);
            b[4] = (byte) (l >> 24 & 0xff);
            b[3] = (byte) (l >> 32 & 0xff);
            b[2] = (byte) (l >> 40 & 0xff);
            b[1] = (byte) (l >> 48 & 0xff);
            b[0] = (byte) (l >> 56 & 0xff);

            return b;
        }

        public long toLongValue(byte[] bytes) {
            return (((long) bytes[0] & 0xff) << 56)
                | (((long) bytes[1] & 0xff) << 48)
                | (((long) bytes[2] & 0xff) << 40)
                | (((long) bytes[3] & 0xff) << 32)
                | (((long) bytes[4] & 0xff) << 24)
                | (((long) bytes[5] & 0xff) << 16)
                | (((long) bytes[6] & 0xff) << 8)
                | ((long) bytes[7] & 0xff);
        }

        public static int  toFloatValue(byte[] b) {
            return  (b[3] & 0xff) |(b[2] & 0xff) << 8|(b[1] & 0xff) << 16|(b[0] & 0xff) << 24;
        }

        public byte[] fromByte(Byte bite)          { return be.fromByte(bite);        }
        public byte[] fromShort(Short shot)        { return be.fromShort(shot);       }
        public byte[] fromInteger(Integer integer) { return be.fromIntValue(integer); }
        public byte[] fromLong(Long lon)           { return be.fromLong(lon);         }

        public short toUnsignedByte(byte[] bytes)  { return (short) Byte.toUnsignedInt(toByteValue(bytes)); }
        public int   toUnsignedShort(byte[] bytes) { return Short.toUnsignedInt(toShortValue(bytes));       }
        public long  toUnsignedInt(byte[] bytes)   { return Integer.toUnsignedLong(toIntValue(bytes));      }
        public float  toUnsignedFloat(byte[] bytes)   { return  Float.intBitsToFloat(toFloatValue(bytes));      }

        public byte[] fromNumber(Number number) {
            if (number instanceof Integer) { return be.fromIntValue(number.intValue());     }
            if (number instanceof Short)   { return be.fromShortValue(number.shortValue()); }
            if (number instanceof Long)    { return be.fromLongValue(number.longValue());   }
            if (number instanceof Byte)    { return be.fromByteValue(number.byteValue());   }

            throw new UnsupportedOperationException("can not create byte array by number [" + number +"]");
        }

        /**
         * cut more and less make up
         * @return byte array of specified length
         */
        public byte[] fromNumber(Number number, int assignBytesLength) {
            byte[] numberBytes = fromNumber(number);
            int numberBytesLength = numberBytes.length;

            if (numberBytesLength > assignBytesLength) {
                numberBytes = ArrayUtil.sub(numberBytes, numberBytesLength - assignBytesLength, numberBytesLength);
            }
            if (numberBytesLength < assignBytesLength) {
                numberBytes = ArrayUtil.addAll(new byte[assignBytesLength - numberBytesLength], numberBytes);
            }

            return numberBytes;
        }
    }

    public static class LittleEndian {
        private LittleEndian() { }

        public byte[] fromByteValue(byte bite) {
            byte[] bytes = new byte[1];
            bytes[0] = bite;

            return bytes;
        }

        public byte toByteValue(byte[] bytes) { return bytes[0]; }

        public byte[] fromShortValue(short s) {
            byte[] b = new byte[2];

            b[0] = (byte) (s & 0xff);
            b[1] = (byte) (s >> 8 & 0xff);

            return b;
        }

        public short toShortValue(byte[] bytes) { return (short) ((bytes[1] << 8) | bytes[0] & 0xff); }

        public byte[] fromCharValue(char chr) { return fromShortValue((short) chr); }

        public char toCharValue(byte[] bytes) { return (char)toShortValue(bytes); }

        public byte[] fromIntValue(int i) {
            byte[] b = new byte[4];

            b[0] = (byte) (i & 0xff);
            b[1] = (byte) (i >> 8 & 0xff);
            b[2] = (byte) (i >> 16 & 0xff);
            b[3] = (byte) (i >> 24 & 0xff);

            return b;
        }

        public int toIntValue(byte[] bytes) {
            int int1 = bytes[0] & 0xff;
            int int2 = (bytes[1] & 0xff) << 8;
            int int3 = (bytes[2] & 0xff) << 16;
            int int4 = (bytes[3] & 0xff) << 24;

            return int1 | int2 | int3 | int4;
        }

        public byte[] fromLongValue(long l) {
            byte[] b = new byte[8];

            b[0] = (byte) (l & 0xff);
            b[1] = (byte) (l >> 8 & 0xff);
            b[2] = (byte) (l >> 16 & 0xff);
            b[3] = (byte) (l >> 24 & 0xff);
            b[4] = (byte) (l >> 32 & 0xff);
            b[5] = (byte) (l >> 40 & 0xff);
            b[6] = (byte) (l >> 48 & 0xff);
            b[7] = (byte) (l >> 56 & 0xff);

            return b;
        }

        public long toLongValue(byte[] bytes) {
            return ((long) bytes[0] & 0xff)
                | (((long) bytes[1] & 0xff) << 8)
                | (((long) bytes[2] & 0xff) << 16)
                | (((long) bytes[3] & 0xff) << 24)
                | (((long) bytes[4] & 0xff) << 32)
                | (((long) bytes[5] & 0xff) << 40)
                | (((long) bytes[6] & 0xff) << 48)
                | (((long) bytes[7] & 0xff) << 56);
        }

        public static int  toFloatValue(byte[] b) {
            return  (b[0] & 0xff) |(b[1] & 0xff) << 8|(b[2] & 0xff) << 16|(b[3] & 0xff) << 24;
        }

        public byte[] fromByte(Byte bite)           { return le.fromByteValue(bite);      }
        public byte[] fromShort(Short shot)         { return le.fromShortValue(shot);     }
        public byte[] fromChar(Character character) { return le.fromCharValue(character); }
        public byte[] fromInteger(Integer integer)  { return le.fromIntValue(integer);    }
        public byte[] fromLong(Long lon)            { return le.fromLongValue(lon);       }

        public byte[] fromNumber(Number number) {
            if (number instanceof Integer) { return le.fromIntValue(number.intValue());     }
            if (number instanceof Long)    { return le.fromLongValue(number.longValue());   }
            if (number instanceof Short)   { return le.fromShortValue(number.shortValue()); }
            if (number instanceof Byte)    { return le.fromByteValue(number.byteValue());   }

            return EMPTY_BYTE_ARRAY;
        }

        public short toUnsignedByte(byte[] bytes)  { return (short) Byte.toUnsignedInt(toByteValue(bytes)); }
        public int   toUnsignedShort(byte[] bytes) { return Short.toUnsignedInt(toShortValue(bytes));       }
        public long  toUnsignedInt(byte[] bytes)   { return Integer.toUnsignedLong(toIntValue(bytes));      }
        public float  toUnsignedFloat(byte[] bytes)   { return  Float.intBitsToFloat(toFloatValue(bytes));      }

        /**
         * cut more and less make up
         * @return byte array of specified length
         */
        public byte[] fromNumber(Number number, int assignBytesLength) {
            byte[] numberBytes = fromNumber(number);
            byte[] assignBytes = new byte[assignBytesLength];

            System.arraycopy(numberBytes, 0, assignBytes, 0, min(assignBytesLength, numberBytes.length));

            return assignBytes;
        }



    }
}
