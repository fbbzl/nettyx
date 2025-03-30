package org.fz.nettyx.serializer.yml;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.Serializer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/1 22:26
 */
public class YmlSerializer implements Serializer {

    @Override
    public ByteBuf getByteBuf() {
        return null;
    }

    @Override
    public <T> T doDeserialize() {
        return null;
    }

    @Override
    public ByteBuf doSerialize() {
        return null;
    }
}
