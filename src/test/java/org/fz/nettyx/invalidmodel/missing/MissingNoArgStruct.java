package org.fz.nettyx.invalidmodel.missing;

import org.fz.nettyx.serializer.type.annotation.Struct;

@Struct(endian = Struct.Endian.NATIVE)
public class MissingNoArgStruct {
    public MissingNoArgStruct(int value)
    {
    }
}
