package org.fz.nettyx.beanmodel.rollback;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class ZInvalidRollbackBean {
    private cint value;
}
