package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class BasicArrayBean {
    @ToArray(length = 2)
    private cint[] values;

    public cint[] getValues() {
        return values;
    }

    public void setValues(cint[] values) {
        this.values = values;
    }
}
