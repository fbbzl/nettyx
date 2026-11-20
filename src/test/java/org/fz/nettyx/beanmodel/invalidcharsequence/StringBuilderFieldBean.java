package org.fz.nettyx.beanmodel.invalidcharsequence;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;

/**
 * Uses {@code ToCharSequence} on a mutable {@code StringBuilder} field to exercise type validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-02
 */
@Struct(endian = Struct.Endian.BE)
public class StringBuilderFieldBean {

    @ToCharSequence(bufferLength = 8)
    private StringBuilder value;

    public StringBuilder getValue() {
        return value;
    }

    public void setValue(StringBuilder value) {
        this.value = value;
    }
}
