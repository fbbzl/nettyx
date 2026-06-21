package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@Struct
public class InterfaceImplGeneric<T> implements GenericInterface<T> {

    private T value;

    @Override
    public T getValue() {
        return value;
    }

}
