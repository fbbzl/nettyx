package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * The type Cpp basic.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 16:04
 */
public abstract class Cppbasic<V extends Comparable<V>> extends Cbasic<V> {

    protected Cppbasic(V value, int size) {
        super(value, size);
    }

    protected Cppbasic(ByteOrder byteOrder, V value, int size) {
        super(byteOrder, value, size);
    }

    protected Cppbasic(ByteOrder byteOrder, ByteBuf buf, int size) {
        super(byteOrder, buf, size);
    }

}
