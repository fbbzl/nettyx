package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;


@Data
@Struct(endian = Struct.Endian.NATIVE)
public class GirlFriend {

    @ToCharSequence(bufferLength = 2)
    private String cup;
}
