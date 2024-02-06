package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.annotation.Type;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.util.BytesKit.Endian;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

@Type("number")
public class NumberConverter implements TypeConverter {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        byte[] bytes = new byte[prop.getLength()];
        byteBuf.readBytes(bytes);
        Endian endianKit = prop.getEndianKit();


        return null;
    }

}
