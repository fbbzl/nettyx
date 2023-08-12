package org.fz.nettyx.serializer.serializer.offset;

import io.netty.buffer.ByteBuf;

/**
 * 基于注解的序列化器, 返回值为指定的对象
 * @author fengbinbin
 * @since 2022-01-02 09:53
 **/
public class AnnotatedOffsetByteBufSerializer implements OffsetByteBufSerializer {

    @Override
    public <T> Class<T> getDomainType() {
        return null;
    }

    @Override
    public ByteBuf getByteBuf() {
        return null;
    }
}
