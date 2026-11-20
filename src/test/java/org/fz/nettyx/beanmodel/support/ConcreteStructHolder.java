package org.fz.nettyx.beanmodel.support;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * Holds one concrete {@code BigEndianLeaf} to exercise nested struct serialization.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
@Struct(endian = Struct.Endian.BE)
@Getter
@Setter
public class ConcreteStructHolder {

    private BigEndianLeaf leaf;
}
