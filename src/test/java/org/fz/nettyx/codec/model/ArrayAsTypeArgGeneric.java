package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class ArrayAsTypeArgGeneric<T, U> {

    private GenericPair<T[], U[]> pairOfArrays;

}
