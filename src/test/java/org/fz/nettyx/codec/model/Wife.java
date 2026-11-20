package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;


/**
 * Combines two generic fields with a fixed-length name in the user struct fixture.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class Wife<I, V> {

    private I      intt;
    @ToCharSequence(bufferLength = 2)
    private String name;

    private V vv;
}
