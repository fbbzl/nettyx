package org.fz.nettyx.serializer.xml.converter;

import cn.hutool.core.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.Throws;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */
public class StringConverter implements TypeConverter<String> {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();

        Throws.ifTrue(typeArgs.length > 1, "string do not support params more than 1");
        byte[] bytes = readBytes(prop, byteBuf);

        Charset charset = CharsetUtil.charset(typeArgs[0].toUpperCase());

        return new String(bytes, charset);
    }

}
