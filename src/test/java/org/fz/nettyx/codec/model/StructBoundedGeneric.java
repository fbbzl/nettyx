package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class StructBoundedGeneric<T extends Bill> {

    private T bill;

}
