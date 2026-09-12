package org.fz.nettyx.beanmodel.generic;

import org.fz.nettyx.serializer.annotated.annotation.Struct;
import org.fz.nettyx.serializer.annotated.basic.c.signed.cchar;
import org.fz.nettyx.serializer.annotated.basic.c.signed.cint;

@Struct(endian = Struct.Endian.BE)
public class ConcreteGenericHierarchy extends GenericHierarchyMiddle<cint, cchar> {
}
