package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
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
public class StringHandler implements XmlPropHandler {

    @Override
    public String forType() {
        return "string";
    }

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Override
    public String read(Prop prop, ByteBuf reading) {
        PropType type = prop.getType();
        String[] typeArgs = ArrayUtil.defaultIfEmpty(type.getTypeArgs(), new String[]{DEFAULT_CHARSET});

        Throws.ifTrue(typeArgs.length > 1, "string do not support params more than 1");

        Charset charset = CharsetUtil.charset(typeArgs[0].toUpperCase());

        return new String(readBytes(prop, reading), charset);
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        String text = prop.getText();
        byte[] bytes = CharSequenceUtil.bytes(text, DEFAULT_CHARSET);
        writing.writeBytes(bytes);
    }

}
