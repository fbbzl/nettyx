package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.annotated.annotation.Struct;
import org.fz.nettyx.serializer.annotated.annotation.ToCharSequence;
import org.fz.nettyx.serializer.annotated.basic.c.unsigned.cuchar;


@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Bill {

    private cuchar bid;

    @ToCharSequence(bufferLength = 4)
    private String orgName;
}
