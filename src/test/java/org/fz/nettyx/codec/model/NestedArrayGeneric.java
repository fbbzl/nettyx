package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class NestedArrayGeneric<T, U> {

    @ToArray(length = 3)
    private T[] values;

    @ToArray(length = 2)
    private GenericPair<T, U>[] pairs;

}
