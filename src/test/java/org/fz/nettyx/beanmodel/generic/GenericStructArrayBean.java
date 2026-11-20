package org.fz.nettyx.beanmodel.generic;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Tests generic struct elements in both fixed and flexible arrays.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class GenericStructArrayBean<T> {

    @ToArray(length = 2)
    private T[] fixed;

    @ToArray(flexible = true)
    private T[] flexible;
}
