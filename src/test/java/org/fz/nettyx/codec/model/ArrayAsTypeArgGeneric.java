package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class ArrayAsTypeArgGeneric<T, U> {

    private GenericPair<T[], U[]> pairOfArrays;

}
