package org.fz.nettyx.beanmodel.missingsetter;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class MissingSetterBean {
    private cint value;

    public cint getValue() {
        return value;
    }
}
