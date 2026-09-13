package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class RecursiveGeneric<T> {

    private T value;

    @Ignore
    private RecursiveGeneric<T> next;

}
