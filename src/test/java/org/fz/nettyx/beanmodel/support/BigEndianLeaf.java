package org.fz.nettyx.beanmodel.support;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Provides a big-endian integer leaf for nested struct tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class BigEndianLeaf {

    private cint value;
}
