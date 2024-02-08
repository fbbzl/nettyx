package org.fz.nettyx.serializer.xml.element;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/26 17:56
 */
public interface ElementHandler {

    String read(Prop prop, ByteBuf reading);

    void write(Prop prop, String value, ByteBuf writing);

}
