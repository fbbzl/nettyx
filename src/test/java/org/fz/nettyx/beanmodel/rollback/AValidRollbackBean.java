package org.fz.nettyx.beanmodel.rollback;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class AValidRollbackBean {
    private cint value;

    public cint getValue() {
        return value;
    }

    public void setValue(cint value) {
        this.value = value;
    }
}
