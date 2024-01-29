package org.fz.nettyx.serializer.yml;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.Serializer;

/**
 * the location of the yml file that was scanned
 *
 * name:
 *   offset: 0
 *   length: 3
 *   formatter: short
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/4 16:45
 */
public class YmlSerializer implements Serializer {

    @Override
    public ByteBuf getByteBuf() {
        return null;
    }
}
