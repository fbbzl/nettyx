package org.fz.nettyx.coveragemodel;

import org.fz.nettyx.serializer.struct.annotation.Struct;

public final class InvalidNestedStructs {

    private InvalidNestedStructs() {
    }

    public static Class<?> privateStructType() {
        return PrivateStruct.class;
    }

    @Struct(endian = Struct.Endian.BE)
    private static class PrivateStruct {
    }
}
