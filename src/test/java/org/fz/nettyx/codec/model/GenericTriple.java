package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class GenericTriple<T, U, V> {

    private T first;

    private U second;

    private V third;

}
