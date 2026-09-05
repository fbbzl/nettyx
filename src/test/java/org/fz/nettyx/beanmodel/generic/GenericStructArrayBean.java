package org.fz.nettyx.beanmodel.generic;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;

@Struct(endian = Struct.Endian.BE)
public class GenericStructArrayBean<T> {

    @ToArray(length = 2)
    private T[] fixed;

    @ToArray(flexible = true)
    private T[] flexible;

    public T[] getFixed() {
        return fixed;
    }

    public void setFixed(T[] fixed) {
        this.fixed = fixed;
    }

    public T[] getFlexible() {
        return flexible;
    }

    public void setFlexible(T[] flexible) {
        this.flexible = flexible;
    }
}
