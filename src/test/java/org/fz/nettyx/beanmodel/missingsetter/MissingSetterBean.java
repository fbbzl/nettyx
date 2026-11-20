package org.fz.nettyx.beanmodel.missingsetter;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Omits the setter for a struct field to exercise accessor validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.BE)
public class MissingSetterBean {
    private cint value;

    public cint getValue() {
        return value;
    }
}
