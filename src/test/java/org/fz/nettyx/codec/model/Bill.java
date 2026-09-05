package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToCharSequence;
import org.fz.nettyx.serializer.type.basic.c.unsigned.cuchar;


@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Bill {

    private cuchar bid;

    @ToCharSequence(bufferLength = 4)
    private String orgName;
}
