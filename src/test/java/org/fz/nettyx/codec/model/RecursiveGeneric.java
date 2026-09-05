package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Ignore;
import org.fz.nettyx.serializer.type.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class RecursiveGeneric<T> {

    private T value;

    @Ignore
    private RecursiveGeneric<T> next;

}
