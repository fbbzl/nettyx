package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

@Data
@Struct
public class NestedArrayGeneric<T, U> {

    @ToArray(length = 3)
    private T[] values;

    @ToArray(length = 2)
    private GenericPair<T, U>[] pairs;

}
