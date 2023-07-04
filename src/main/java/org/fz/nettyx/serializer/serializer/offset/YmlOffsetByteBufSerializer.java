package org.fz.nettyx.serializer.serializer.offset;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @since 2022-01-02 09:52
 **/
public class YmlOffsetByteBufSerializer implements OffsetByteBufSerializer {

    @Override
    public <T> Class<T> getDomainType() {
        return null;
    }

    @Override
    public ByteBuf getByteBuf() {
        return null;
    }
}
