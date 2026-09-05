package org.fz.nettyx.beanmodel.wrongsetter;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

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
