package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class BaseGeneric<T> {

    private T baseField;

}
