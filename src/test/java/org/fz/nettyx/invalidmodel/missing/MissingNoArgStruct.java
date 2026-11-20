package org.fz.nettyx.invalidmodel.missing;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Declares only a parameterized constructor to exercise rejection of structs without a no-argument constructor.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.NATIVE)
public class MissingNoArgStruct {
    public MissingNoArgStruct(int value)
    {
    }
}
