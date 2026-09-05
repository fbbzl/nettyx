package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cshort;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/26 0:32
 */
@Struct(endian = Struct.Endian.NATIVE)
@Data
public class Lover {
    cshort uname;
}
