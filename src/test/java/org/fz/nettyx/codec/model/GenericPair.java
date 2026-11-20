package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Holds two independently typed fields for generic struct tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class GenericPair<T, U> {

    private T first;

    private U second;

}
