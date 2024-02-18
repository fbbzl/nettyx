package org.fz.nettyx.serializer.xml.converter;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.Throws;

import java.nio.charset.Charset;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */
public class StringConverter implements TypeConverter {

    private static final String[] DEFAULT_CHARSET = {"UTF-8"};

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        String[] typeArgs = ArrayUtil.defaultIfEmpty(type.getTypeArgs(), DEFAULT_CHARSET);

        Throws.ifTrue(typeArgs.length > 1, "string do not support params more than 1");

        Charset charset = CharsetUtil.charset(typeArgs[0].toUpperCase());

        return new String(readBytes(prop, byteBuf), charset);
    }

}
