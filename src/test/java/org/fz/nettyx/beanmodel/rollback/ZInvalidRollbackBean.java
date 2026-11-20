package org.fz.nettyx.beanmodel.rollback;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Triggers scan rollback by omitting the accessors for its struct field.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.BE)
public class ZInvalidRollbackBean {
    private cint value;
}
