package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class NestedGeneric<T, U> {

    private GenericBox<T> boxedFirst;

    private GenericPair<T, U> pair;

}
