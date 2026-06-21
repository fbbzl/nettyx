package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

@Data
@Struct
public class MultiDimArrayGeneric<T> {

    @ToArray(length = 2)
    private T[][] matrix;

}
