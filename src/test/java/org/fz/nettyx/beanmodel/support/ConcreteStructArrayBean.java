package org.fz.nettyx.beanmodel.support;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Holds fixed and flexible arrays of concrete {@code BigEndianLeaf} structs.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class ConcreteStructArrayBean {

    @ToArray(length = 2)
    private BigEndianLeaf[] fixed;

    @ToArray(flexible = true)
    private BigEndianLeaf[] flexible;
}
