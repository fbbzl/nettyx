package org.fz.nettyx.serializer.cmodel;

import lombok.Data;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.unsigned.cuint;
import org.fz.nettyx.serializer.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.struct.annotation.Chunk;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/** Models a C packet with explicit padding before a 32-bit field. */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class NativePaddedPacket {
    private cuchar kind;
    @Chunk(length = 3)
    private byte[] alignment;
    private cuint messageId;
    @ToArray(length = 2)
    private cushort[] samples;
}
