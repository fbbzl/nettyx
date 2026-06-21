package org.fz.nettyx.codec.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.fz.nettyx.serializer.struct.annotation.Struct;

@Data
@EqualsAndHashCode(callSuper = true)
@Struct
public class DerivedGeneric<T, U> extends BaseGeneric<T> {

    private U derivedField;

}
