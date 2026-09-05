package org.fz.nettyx.beanmodel.primitive;

import org.fz.nettyx.serializer.type.annotation.Struct;

@Struct(endian = Struct.Endian.BE)
public class PrimitiveStruct {

    @PrimitiveField
    private boolean booleanValue;
    @PrimitiveField
    private byte byteValue;
    @PrimitiveField
    private char charValue;
    @PrimitiveField
    private short shortValue;
    @PrimitiveField
    private int intValue;
    @PrimitiveField
    private long longValue;
    @PrimitiveField
    private float floatValue;
    @PrimitiveField
    private double doubleValue;

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }
}
