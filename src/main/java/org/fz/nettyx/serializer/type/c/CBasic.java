package org.fz.nettyx.serializer.type.c;

import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import org.fz.nettyx.serializer.type.Basic;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 15:50
 */
public abstract class CBasic<V> extends Basic<V> {

    private static final boolean C_DEFAULT_SINGED = true;
    private static final ByteOrder C_DEFAULT_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    protected CBasic(V value, int size) {
        super(value, size);
    }

    protected CBasic(ByteBuf buf, int size) {
        super(buf, size);
    }

    @Override
    public boolean hasSinged() {
        return C_DEFAULT_SINGED;
    }

    @Override
    public ByteOrder order() {
        return C_DEFAULT_ENDIAN;
    }

}
