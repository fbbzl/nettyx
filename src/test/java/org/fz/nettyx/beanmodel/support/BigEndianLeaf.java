package org.fz.nettyx.beanmodel.support;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class BigEndianLeaf {

    private cint value;

    public cint getValue() {
        return value;
    }

    public void setValue(cint value) {
        this.value = value;
    }
}
