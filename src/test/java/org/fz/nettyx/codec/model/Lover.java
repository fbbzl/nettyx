package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cshort;

/**
 * Provides a native-order short integer for a nested element of the {@code Brother} fixture.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/26 0:32
 */
@Struct(endian = Struct.Endian.NATIVE)
@Data
public class Lover {
    cshort uname;
}
