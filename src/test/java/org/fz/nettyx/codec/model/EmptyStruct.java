package org.fz.nettyx.codec.model;

import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Provides a struct with zero serialized fields for empty-buffer tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-11
 */
@Struct(endian = Struct.Endian.NATIVE)
public class EmptyStruct {
}
