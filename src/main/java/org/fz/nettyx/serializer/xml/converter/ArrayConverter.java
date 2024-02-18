package org.fz.nettyx.serializer.xml.converter;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

/**
 * TODO
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:30
 */
public class ArrayConverter implements TypeConverter {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        if (!type.isArray()) {
            return EMPTY;
        }

        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();
        String[] typeArgs = type.getTypeArgs();

        

        return String.join(",", arrayLength + "");
    }
}
