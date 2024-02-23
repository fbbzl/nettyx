package org.fz.nettyx.serializer.xml.handler;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.dtd.Model.Prop;

/**
 * The interface Type handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /2/7 10:51
 */
public interface PropTypeHandler extends PropHandler {

    /**
     * the type string value handled
     */
    String forType();

    default byte[] readBytes(Prop prop, ByteBuf byteBuf) {
        byte[] bytes = new byte[prop.getLength()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

}
