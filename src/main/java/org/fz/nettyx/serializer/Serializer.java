package org.fz.nettyx.serializer;

import io.netty.buffer.ByteBuf;

/**
 * The top level interface Byte buf serializer.
 *
 * @author fengbinbin
 * @since 2022 -01-02 10:15
 */
public interface Serializer {

    /**
     * get byte buf.
     *
     * @return the byte buf
     */
    ByteBuf getByteBuf();

    /**
     * Do deserialize t.
     *
     * @param <T> the type parameter
     * @return the t
     */
    <T> T doDeserialize();

    /**
     * Do serialize byte buf.
     *
     * @return the byte buf
     */
    ByteBuf doSerialize();
}
