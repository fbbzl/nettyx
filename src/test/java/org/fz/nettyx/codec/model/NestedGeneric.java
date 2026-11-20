package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Combines a generic box and pair that share the first type parameter.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class NestedGeneric<T, U> {

    private GenericBox<T> boxedFirst;

    private GenericPair<T, U> pair;

}
