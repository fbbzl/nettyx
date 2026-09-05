package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class MultiDimArrayGeneric<T> {

    @ToArray(length = 2)
    private T[][] matrix;

}
