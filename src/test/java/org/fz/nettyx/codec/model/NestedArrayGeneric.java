package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Combines fixed arrays of generic values and parameterized pairs.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class NestedArrayGeneric<T, U> {

    @ToArray(length = 3)
    private T[] values;

    @ToArray(length = 2)
    private GenericPair<T, U>[] pairs;

}
