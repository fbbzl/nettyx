package org.fz.nettyx.beanmodel.generic;

import org.fz.nettyx.serializer.type.annotation.Struct;

@Struct(endian = Struct.Endian.LE)
public class LittleEndianGenericValue<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
