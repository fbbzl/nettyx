package org.fz.nettyx.beanmodel.valid;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Provides a fixed array of two big-endian integers for serialization tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-13
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class BasicArrayBean {
    @ToArray(length = 2)
    private cint[] values;
}
