package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Counts getter and setter calls so tests can verify how the serializer accesses struct fields.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
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
