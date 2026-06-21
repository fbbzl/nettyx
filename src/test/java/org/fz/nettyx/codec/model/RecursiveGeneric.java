package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class RecursiveGeneric<T> {

    private T value;

    private RecursiveGeneric<T> next;

}
