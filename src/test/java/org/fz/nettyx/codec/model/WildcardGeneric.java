package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.Basic;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class WildcardGeneric {

    private Basic<?> value;

}
