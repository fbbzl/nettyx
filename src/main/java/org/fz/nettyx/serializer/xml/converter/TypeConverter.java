package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;

/**
 * The interface Type converter.
 *
 * @param <R> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /2/7 10:51
 */
@SuppressWarnings("unchecked")
public interface TypeConverter<R> {

    /**
     * Convert r.
     *
     * @param prop the prop
     * @param byteBuf the byte buf
     * @return the r
     */
    R convert(Prop prop, ByteBuf byteBuf);

    default byte[] readBytes(Prop prop, ByteBuf byteBuf) {
        // TODO 相对位置 和 绝对位置
        byte[] bytes = new byte[prop.getLength()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

}
