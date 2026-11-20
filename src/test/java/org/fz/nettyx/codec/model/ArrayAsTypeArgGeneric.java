package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Uses arrays as generic arguments to exercise rejection of this unsupported type shape.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-21
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class ArrayAsTypeArgGeneric<T, U> {

    private GenericPair<T[], U[]> pairOfArrays;

}
