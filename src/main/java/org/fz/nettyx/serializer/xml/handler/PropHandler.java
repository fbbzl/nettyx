package org.fz.nettyx.serializer.xml.handler;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.dtd.Model.Prop;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/22 22:30
 */
public interface PropHandler {

    String read(Prop prop, ByteBuf reading);

    default void write(Prop prop, ByteBuf writing) {
    }

}
