package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Registers a little-endian struct whose field and accessors are inherited.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.LE)
public class InheritedAccessorBean extends InheritedAccessorBase {
}
