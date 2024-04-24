package org.fz.nettyx.util;

import cn.hutool.core.util.PrimitiveArrayUtil;

import java.math.BigInteger;

import static java.lang.Integer.min;

/**
 * endian util
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2020/12/18 15:19
 */
public enum EndianKit {

    BE() {
        public byte[] fromShort(short s) {
            byte[] b = new byte[2];

            b[1] = (byte) (s & 0xff);
            b[0] = (byte) (s >> 8 & 0xff);

            return b;
        }

        public short toShort(byte[] bytes) {
            return (short) ((bytes[0] << 8) | bytes[1] & 0xff);
        }

        @Override
        public byte[] fromUnsignedShort(int s) {
            byte[] b = new byte[2];

            b[1] = (byte) (s & 0xff);
            b[0] = (byte) (s >> 8 & 0xff);

            return b;
        }

        public int toUnsignedShort(byte[] bytes) {
            return Short.toUnsignedInt(toShort(bytes));
        }

        public byte[] fromChar(char chr) {
            return fromShort((short) chr);
        }

        public char toChar(byte[] bytes) {
            return (char) toShort(bytes);
        }

        public byte[] fromInt(int i) {
            byte[] b = new byte[4];

            b[3] = (byte) (i & 0xff);
            b[2] = (byte) (i >> 8 & 0xff);
            b[1] = (byte) (i >> 16 & 0xff);
            b[0] = (byte) (i >> 24 & 0xff);

            return b;
        }

        public int toInt(byte[] bytes) {
            int int1 = bytes[3] & 0xff;
            int int2 = (bytes[2] & 0xff) << 8;
            int int3 = (bytes[1] & 0xff) << 16;
            int int4 = (bytes[0] & 0xff) << 24;

            return int1 | int2 | int3 | int4;
        }

        @Override
        public byte[] fromUnsignedInt(long i) {
            byte[] b = new byte[4];

            b[3] = (byte) (i & 0xff);
            b[2] = (byte) (i >> 8 & 0xff);
            b[1] = (byte) (i >> 16 & 0xff);
            b[0] = (byte) (i >> 24 & 0xff);

            return b;
        }

        public long toUnsignedInt(byte[] bytes) {
            return Integer.toUnsignedLong(toInt(bytes));
        }


        public byte[] fromLong(long l) {
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

        public long toLong(byte[] bytes) {
            return (((long) bytes[0] & 0xff) << 56)
                   | (((long) bytes[1] & 0xff) << 48)
                   | (((long) bytes[2] & 0xff) << 40)
                   | (((long) bytes[3] & 0xff) << 32)
                   | (((long) bytes[4] & 0xff) << 24)
                   | (((long) bytes[5] & 0xff) << 16)
                   | (((long) bytes[6] & 0xff) << 8)
                   | ((long) bytes[7] & 0xff);
        }

        @Override
        public byte[] fromUnsignedLong(BigInteger l) {
            return l.toByteArray();
        }

        @Override
        public BigInteger toUnsignedLong(byte[] bytes) {
            return new BigInteger(bytes);
        }

        public float toFloat(byte[] bytes) {
            int i = ((((bytes[0] & 0xff) << 8 | (bytes[1] & 0xff)) << 8) | (bytes[2] & 0xff)) << 8 | (bytes[3] & 0xff);

            return Float.intBitsToFloat(i);
        }

        @Override
        public double toDouble(byte[] bytes) {
            long h = (bytes[0] & 0xff) << 8;
            h = (h | (bytes[1] & 0xff)) << 8;
            h = (h | (bytes[2] & 0xff)) << 8;
            h = (h | (bytes[3] & 0xff)) << 8;
            h = (h | (bytes[4] & 0xff)) << 8;
            h = (h | (bytes[5] & 0xff)) << 8;
            h = (h | (bytes[6] & 0xff)) << 8;
            h = (h | (bytes[7] & 0xff));

            return Double.longBitsToDouble(h);
        }

        @Override
        public short reverseUnsignedByte(short b) {
            byte[] bytes = fromUnsignedByte(b);
            return LE.toUnsignedByte(bytes);
        }

        @Override
        public short reverseShort(short s) {
            byte[] bytes = fromShort(s);
            return LE.toShort(bytes);
        }

        @Override
        public int reverseUnsignedShort(int s) {
            byte[] bytes = fromUnsignedShort(s);
            return LE.toUnsignedShort(bytes);
        }

        @Override
        public int reverseInt(int i) {
            byte[] bytes = fromInt(i);
            return LE.toInt(bytes);
        }

        @Override
        public long reverseUnsignedInt(long i) {
            byte[] bytes = fromUnsignedInt(i);
            return LE.toUnsignedInt(bytes);
        }

        @Override
        public long reverseLong(long l) {
            byte[] bytes = fromLong(l);
            return LE.toLong(bytes);
        }

        @Override
        public BigInteger reverseUnsignedLong(BigInteger l) {
            byte[] bytes = fromUnsignedLong(l);
            return LE.toUnsignedLong(bytes);
        }

        @Override
        public float reverseFloat(float f) {
            byte[] bytes = fromFloat(f);
            return LE.toFloat(bytes);
        }

        @Override
        public double reverseDouble(double d) {
            byte[] bytes = fromDouble(d);
            return LE.toDouble(bytes);
        }

        /**
         * cut more and less make up
         *
         * @return byte array of specified length
         */
        public byte[] fromNumber(Number number, int assignBytesLength) {
            byte[] numberBytes       = fromNumber(number);
            int    numberBytesLength = numberBytes.length;

            if (numberBytesLength > assignBytesLength) {
                numberBytes = PrimitiveArrayUtil.sub(numberBytes, numberBytesLength - assignBytesLength,
                                                     numberBytesLength);
            }
            if (numberBytesLength < assignBytesLength) {
                numberBytes = PrimitiveArrayUtil.addAll(new byte[assignBytesLength - numberBytesLength], numberBytes);
            }

            return numberBytes;
        }
    },
    LE() {
        @Override
        public byte[] fromShort(short s) {
            byte[] b = new byte[2];

            b[0] = (byte) (s & 0xff);
            b[1] = (byte) (s >> 8 & 0xff);

            return b;
        }

        @Override
        public short toShort(byte[] bytes) {
            return (short) ((bytes[1] << 8) | bytes[0] & 0xff);
        }

        @Override
        public byte[] fromUnsignedShort(int s) {
            byte[] b = new byte[2];

            b[0] = (byte) (s & 0xff);
            b[1] = (byte) (s >> 8 & 0xff);

            return b;
        }

        @Override
        public int toUnsignedShort(byte[] bytes) {
            return Short.toUnsignedInt(toShort(bytes));
        }

        @Override
        public byte[] fromChar(char chr) {
            return fromShort((short) chr);
        }

        @Override
        public char toChar(byte[] bytes) {
            return (char) toShort(bytes);
        }

        @Override
        public byte[] fromInt(int i) {
            byte[] b = new byte[4];

            b[0] = (byte) (i & 0xff);
            b[1] = (byte) (i >> 8 & 0xff);
            b[2] = (byte) (i >> 16 & 0xff);
            b[3] = (byte) (i >> 24 & 0xff);

            return b;
        }

        @Override
        public int toInt(byte[] bytes) {
            int int1 = bytes[0] & 0xff;
            int int2 = (bytes[1] & 0xff) << 8;
            int int3 = (bytes[2] & 0xff) << 16;
            int int4 = (bytes[3] & 0xff) << 24;

            return int1 | int2 | int3 | int4;
        }

        @Override
        public byte[] fromUnsignedInt(long i) {
            byte[] b = new byte[4];

            b[0] = (byte) (i & 0xff);
            b[1] = (byte) (i >> 8 & 0xff);
            b[2] = (byte) (i >> 16 & 0xff);
            b[3] = (byte) (i >> 24 & 0xff);

            return b;
        }

        @Override
        public long toUnsignedInt(byte[] bytes) {
            return Integer.toUnsignedLong(toInt(bytes));
        }

        @Override
        public byte[] fromLong(long l) {
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

        @Override
        public long toLong(byte[] bytes) {
            return ((long) bytes[0] & 0xff)
                   | (((long) bytes[1] & 0xff) << 8)
                   | (((long) bytes[2] & 0xff) << 16)
                   | (((long) bytes[3] & 0xff) << 24)
                   | (((long) bytes[4] & 0xff) << 32)
                   | (((long) bytes[5] & 0xff) << 40)
                   | (((long) bytes[6] & 0xff) << 48)
                   | (((long) bytes[7] & 0xff) << 56);
        }

        @Override
        public byte[] fromUnsignedLong(BigInteger l) {
            return PrimitiveArrayUtil.reverse(l.toByteArray());
        }

        @Override
        public BigInteger toUnsignedLong(byte[] bytes) {
            return new BigInteger(PrimitiveArrayUtil.reverse(bytes));
        }

        @Override
        public float toFloat(byte[] bytes) {
            int i = ((((bytes[3] & 0xff) << 8 | (bytes[2] & 0xff)) << 8) | (bytes[1] & 0xff)) << 8 | (bytes[0] & 0xff);
            return Float.intBitsToFloat(i);
        }

        @Override
        public double toDouble(byte[] bytes) {
            long l = (bytes[7] & 0xff) << 8;
            l = (l | (bytes[6] & 0xff)) << 8;
            l = (l | (bytes[5] & 0xff)) << 8;
            l = (l | (bytes[4] & 0xff)) << 8;
            l = (l | (bytes[3] & 0xff)) << 8;
            l = (l | (bytes[2] & 0xff)) << 8;
            l = (l | (bytes[1] & 0xff)) << 8;
            l = (l | (bytes[0] & 0xff));

            return Double.longBitsToDouble(l);
        }

        @Override
        public short reverseUnsignedByte(short b) {
            byte[] bytes = fromUnsignedByte(b);
            return BE.toUnsignedByte(bytes);
        }

        @Override
        public short reverseShort(short s) {
            byte[] bytes = fromShort(s);
            return BE.toShort(bytes);
        }

        @Override
        public int reverseUnsignedShort(int s) {
            byte[] bytes = fromUnsignedShort(s);
            return BE.toUnsignedShort(bytes);
        }

        @Override
        public int reverseInt(int i) {
            byte[] bytes = fromInt(i);
            return BE.toInt(bytes);
        }

        @Override
        public long reverseUnsignedInt(long i) {
            byte[] bytes = fromUnsignedInt(i);
            return BE.toUnsignedInt(bytes);
        }

        @Override
        public long reverseLong(long l) {
            byte[] bytes = fromLong(l);
            return BE.toLong(bytes);
        }

        @Override
        public BigInteger reverseUnsignedLong(BigInteger l) {
            byte[] bytes = fromUnsignedLong(l);
            return BE.toUnsignedLong(bytes);
        }

        @Override
        public float reverseFloat(float f) {
            byte[] bytes = fromFloat(f);
            return BE.toFloat(bytes);
        }

        @Override
        public double reverseDouble(double d) {
            byte[] bytes = fromDouble(d);
            return BE.toDouble(bytes);
        }

        /**
         * cut more and less make up
         *
         * @return byte array of specified length
         */
        @Override
        public byte[] fromNumber(Number number, int assignBytesLength) {
            byte[] numberBytes = fromNumber(number);
            byte[] assignBytes = new byte[assignBytesLength];

            System.arraycopy(numberBytes, 0, assignBytes, 0, min(assignBytesLength, numberBytes.length));

            return assignBytes;
        }
    },
    ;

    // byte
    public byte[] fromByteValue(byte bite) {
        return new byte[]{bite};
    }

    public byte toByteValue(byte[] bytes) {
        return bytes[0];
    }

    public byte[] fromUnsignedByte(short s) {
        return new byte[]{(byte) (s & 0xff)};
    }

    public short toUnsignedByte(byte[] bytes) {
        return (short) Byte.toUnsignedInt(toByteValue(bytes));
    }

    // short
    public abstract byte[] fromShort(short s);

    public abstract short toShort(byte[] bytes);

    public abstract byte[] fromUnsignedShort(int s);

    public abstract int toUnsignedShort(byte[] bytes);

    // char
    public abstract byte[] fromChar(char chr);

    public abstract char toChar(byte[] bytes);

    // int
    public abstract byte[] fromInt(int i);

    public abstract int toInt(byte[] bytes);

    public abstract byte[] fromUnsignedInt(long i);

    public abstract long toUnsignedInt(byte[] bytes);

    // long
    public abstract byte[] fromLong(long l);

    public abstract long toLong(byte[] bytes);

    public abstract byte[] fromUnsignedLong(BigInteger l);

    public abstract BigInteger toUnsignedLong(byte[] bytes);

    // float
    public byte[] fromFloat(float f) {
        return fromInt(Float.floatToRawIntBits(f));
    }

    public abstract float toFloat(byte[] bytes);

    // double
    public byte[] fromDouble(double d) {
        return fromLong(Double.doubleToRawLongBits(d));
    }

    public abstract double toDouble(byte[] bytes);

    public abstract short reverseUnsignedByte(short b);

    public abstract short reverseShort(short s);

    public abstract int reverseUnsignedShort(int s);

    public abstract int reverseInt(int i);

    public abstract long reverseUnsignedInt(long i);

    public abstract long reverseLong(long l);

    public abstract BigInteger reverseUnsignedLong(BigInteger l);

    public abstract float reverseFloat(float f);

    public abstract double reverseDouble(double d);

    // number
    public byte[] fromNumber(Number num) {
        if (num instanceof Integer) {
            return this.fromInt(num.intValue());
        }
        if (num instanceof Short) {
            return this.fromShort(num.shortValue());
        }
        if (num instanceof Long) {
            return this.fromLong(num.longValue());
        }
        if (num instanceof Byte) {
            return this.fromByteValue(num.byteValue());
        }
        if (num instanceof Float) {
            return this.fromFloat(num.floatValue());
        }
        if (num instanceof Double) {
            return this.fromDouble(num.doubleValue());
        }

        throw new UnsupportedOperationException("can not create byte array by number [" + num + "]");
    }

    public abstract byte[] fromNumber(Number number, int assignBytesLength);

}
