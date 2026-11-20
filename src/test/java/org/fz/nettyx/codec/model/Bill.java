package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;


/**
 * Provides an unsigned byte identifier and a fixed-length organization name for nested struct tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Bill {

    private cuchar bid;

    @ToCharSequence(bufferLength = 4)
    private String orgName;
}
