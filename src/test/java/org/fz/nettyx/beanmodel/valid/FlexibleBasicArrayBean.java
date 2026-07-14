package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

@Struct(endian = Struct.Endian.LE)
public class FlexibleBasicArrayBean {
    @ToArray(flexible = true)
    private cint[] values;

    public cint[] getValues() {
        return values;
    }

    public void setValues(cint[] values) {
        this.values = values;
    }
}
