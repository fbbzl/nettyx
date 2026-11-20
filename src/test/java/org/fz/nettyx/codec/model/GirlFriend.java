package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;


/**
 * Provides a single two-byte string field for the user struct fixture.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024-02-23
 */
@Data
@Struct(endian = Struct.Endian.NATIVE)
public class GirlFriend {

    @ToCharSequence(bufferLength = 2)
    private String cup;
}
