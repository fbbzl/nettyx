package org.fz.nettyx.codec.model;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Tests whether a generated reader can invoke a protected no-argument constructor.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.NATIVE)
public class ProtectedConstructorStruct {
    protected ProtectedConstructorStruct()
    {
    }
}
