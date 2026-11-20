package org.fz.nettyx.beanmodel.invalidarray;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Applies {@code ToArray} to a scalar string to exercise annotation validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-02
 */
@Struct(endian = Struct.Endian.BE)
public class NonArrayFieldBean {

    @ToArray(length = 1)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
