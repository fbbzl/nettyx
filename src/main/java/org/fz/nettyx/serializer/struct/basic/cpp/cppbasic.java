package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * The type Cpp basic.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 16:04
 */
public abstract class cppbasic<V extends Comparable<V>> extends cbasic<V> {

    protected cppbasic(V value, int size) {
        super(value, size);
    }

    protected cppbasic(ByteBuf buf, int size) {
        super(buf, size);
    }

}
