package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class GenericTriple<T, U, V> {

    private T first;

    private U second;

    private V third;

}
