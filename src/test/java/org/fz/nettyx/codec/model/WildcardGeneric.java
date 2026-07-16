package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class WildcardGeneric {

    private Basic<?> value;

}
