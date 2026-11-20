package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;


/**
 * Provides independently typed name and order fields for nested struct tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Son<B, Y> {

    private B name;
    private Y sonOrder;

}
