package org.fz.nettyx.serializer;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Type;

/**
 * The top level interface Byte buf serializer.
 *
 * @author fengbinbin
 * @since 2022 -01-02 10:15
 */
public interface Serializer {

    /**
     * object type
     *
     * @return the target type
     */
    Type getType();

    /**
     * get byte buf.
     *
     * @return the byte buf
     */
    ByteBuf getByteBuf();

}
