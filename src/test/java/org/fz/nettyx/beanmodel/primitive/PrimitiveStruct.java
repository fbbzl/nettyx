package org.fz.nettyx.beanmodel.primitive;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Provides all eight Java primitive types for field accessor round-trip tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
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
}
