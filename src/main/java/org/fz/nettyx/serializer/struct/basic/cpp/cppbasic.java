package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * The type Cpp basic.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 16:04
 */
public abstract class cppbasic<V extends Comparable<V>> extends cbasic<V> {

    protected cppbasic(ByteOrder byteOrder, V value, int size) {
        super(byteOrder, value, size);
    }

    protected cppbasic(ByteOrder byteOrder, ByteBuf buf, int size) {
        super(byteOrder, buf, size);
    }

}
