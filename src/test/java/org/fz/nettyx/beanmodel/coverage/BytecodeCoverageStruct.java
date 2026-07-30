package org.fz.nettyx.beanmodel.coverage;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;

@Struct(endian = Struct.Endian.BE)
public class BytecodeCoverageStruct {

    @ToArray(length = 6)
    private cchar[] small;

    @ToArray(length = 128)
    private cchar[] medium;

    @ToArray(length = 32768)
    private cchar[] large;

    @ToArray(length = 1)
    private String[] unsupported;

    public cchar[] getSmall() {
        return small;
    }

    public void setSmall(cchar[] small) {
        this.small = small;
    }

    public cchar[] getMedium() {
        return medium;
    }

    public void setMedium(cchar[] medium) {
        this.medium = medium;
    }

    public cchar[] getLarge() {
        return large;
    }

    public void setLarge(cchar[] large) {
        this.large = large;
    }

    public String[] getUnsupported() {
        return unsupported;
    }

    public void setUnsupported(String[] unsupported) {
        this.unsupported = unsupported;
    }
}
