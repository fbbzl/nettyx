package org.fz.nettyx.codec.model;

import org.fz.nettyx.serializer.struct.annotation.Struct;

@Struct(endian = Struct.Endian.NATIVE)
public class ProtectedConstructorStruct {
    protected ProtectedConstructorStruct()
    {
    }
}
