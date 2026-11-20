package org.fz.nettyx.beanmodel.valid;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Provides a little-endian flexible array whose element type is resolved at runtime.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.LE)
@Getter
@Setter
public class FlexibleGenericBasicArrayBean<T> {

    @ToArray(flexible = true)
    private T[] values;
}
