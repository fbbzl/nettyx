package org.fz.nettyx.beanmodel.wrongsetter;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Uses a fluent setter to exercise validation of the setter return type.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.BE)
public class WrongSetterBean {
    private cint value;

    public cint getValue() {
        return value;
    }

    public WrongSetterBean setValue(cint value) {
        this.value = value;
        return this;
    }
}
