package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;


@Data
@Struct
public class Bill {

    private cuchar bid;

    @ToCharSequence(bufferLength = 4)
    private String orgName;
}
