package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Exercises a self-referential generic field excluded from serialization.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class RecursiveGeneric<T> {

    private T value;

    @Ignore
    private RecursiveGeneric<T> next;

}
