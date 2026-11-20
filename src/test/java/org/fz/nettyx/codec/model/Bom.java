package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;


/**
 * Provides three independently typed fields for the generic components of the user struct fixture.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/19 11:17
 */

@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Bom<T, W, U> {

    private T t;
    private W gg;
    private U mm;

}
