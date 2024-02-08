package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;

/**
 * TODO
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:30
 */
public class ArrayConverter implements TypeConverter<String[]> {

    @Override
    public String[] convert(Prop prop, ByteBuf byteBuf) {
        return null;
    }
}
