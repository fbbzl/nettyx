package org.fz.nettyx.beanmodel.coverage;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.c.signed.cchar;

/**
 * Exercises bytecode generation for different array length constants and the fallback for unsupported array types.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class BytecodeCoverageStruct {

    @ToArray(length = 6)
    private cchar[] small;

    @ToArray(length = 128)
    private cchar[] medium;

    @ToArray(length = 32768)
    private cchar[] large;

    @ToArray(length = 1)
    private String[] unsupported;
}
