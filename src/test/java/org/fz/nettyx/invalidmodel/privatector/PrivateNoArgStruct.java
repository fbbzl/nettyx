package org.fz.nettyx.invalidmodel.privatector;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Declares a private no-argument constructor to exercise scanner visibility checks.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.NATIVE)
public class PrivateNoArgStruct {
    private PrivateNoArgStruct()
    {
    }
}
