package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class AccessorBean {
    private cint value;
    private transient int getterCalls;
    private transient int setterCalls;

    public cint getValue() {
        getterCalls++;
        return value;
    }

    public void setValue(cint value) {
        setterCalls++;
        this.value = value;
    }

    public int getterCalls() {
        return getterCalls;
    }

    public int setterCalls() {
        return setterCalls;
    }
}
