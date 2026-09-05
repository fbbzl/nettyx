package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class InterfaceImplGeneric<T> implements GenericInterface<T> {

    private T value;

    @Override
    public T getValue() {
        return value;
    }

}
