package org.fz.nettyx.beanmodel.generic;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Wraps one generic field in a big-endian struct for type resolution tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class BigEndianGenericValue<T> {

    private T value;
}
