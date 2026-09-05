package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;

@Struct(endian = Struct.Endian.LE)
public class FlexibleGenericBasicArrayBean<T> {

    @ToArray(flexible = true)
    private T[] values;

    public T[] getValues() {
        return values;
    }

    public void setValues(T[] values) {
        this.values = values;
    }
}
