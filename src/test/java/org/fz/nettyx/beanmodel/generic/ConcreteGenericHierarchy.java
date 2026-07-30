package org.fz.nettyx.beanmodel.generic;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class ConcreteGenericHierarchy extends GenericHierarchyMiddle<cint, cchar> {
}
