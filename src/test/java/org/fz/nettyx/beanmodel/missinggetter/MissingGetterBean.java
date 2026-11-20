package org.fz.nettyx.beanmodel.missinggetter;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Omits the getter for a struct field to exercise accessor validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.BE)
public class MissingGetterBean {
    private cint value;

    public void setValue(cint value) {
        this.value = value;
    }
}
