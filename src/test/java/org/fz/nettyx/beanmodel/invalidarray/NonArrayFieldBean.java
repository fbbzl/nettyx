package org.fz.nettyx.beanmodel.invalidarray;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

@Struct(endian = Struct.Endian.BE)
public class NonArrayFieldBean {

    @ToArray(length = 1)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
