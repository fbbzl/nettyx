package org.fz.nettyx.beanmodel.invalidcharsequence;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToCharSequence;

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
