package org.fz.nettyx.serializer.serializer;

import io.netty.buffer.ByteBuf;

/**
 * The top level interface Byte buf serializer.
 *
 * @author fengbinbin
 * @since 2022 -01-02 10:15
 */
public interface ByteBufSerializer {

    /**
     * Gets domain type.
     *
     * @param <T> the type parameter
     * @return the domain type
     */
    <T> Class<T> getDomainType();

    /**
     * get byte buf.
     *
     * @return the byte buf
     */
    ByteBuf getByteBuf();

}
