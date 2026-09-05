package org.fz.nettyx.beanmodel.missinggetter;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class MissingGetterBean {
    private cint value;

    public void setValue(cint value) {
        this.value = value;
    }
}
