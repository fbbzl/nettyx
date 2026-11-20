package org.fz.nettyx.beanmodel.generic;

import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Binds the reordered hierarchy parameters to {@code cint} and {@code cchar} for inherited field tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
public class ConcreteGenericHierarchy extends GenericHierarchyMiddle<cint, cchar> {
}
