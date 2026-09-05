package org.fz.nettyx.invalidmodel.privatector;

import org.fz.nettyx.serializer.type.annotation.Struct;

@Struct(endian = Struct.Endian.NATIVE)
public class PrivateNoArgStruct {
    private PrivateNoArgStruct()
    {
    }
}
