package org.fz.nettyx.codec.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Adds a second generic field to the one inherited from {@code BaseGeneric}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Struct(endian = Struct.Endian.NATIVE)
public class DerivedGeneric<T, U> extends BaseGeneric<T> {

    private U derivedField;

}
