package org.fz.nettyx.serializer.xml.handler;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;

/**
 * The interface Type handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /2/7 10:51
 */
public interface XmlPropHandler {

    /**
     * the type string value handled
     */
    String forType();

    String read(Prop prop, ByteBuf reading);

    void write(Prop prop, ByteBuf writing);

    default byte[] readBytes(Prop prop, ByteBuf byteBuf) {
        // TODO 相对位置 和 绝对位置
        byte[] bytes = new byte[prop.getLength()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

}
