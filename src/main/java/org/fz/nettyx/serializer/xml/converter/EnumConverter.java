package org.fz.nettyx.serializer.xml.converter;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.BytesKit.Endian;
import org.fz.nettyx.util.Throws;

import java.util.List;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static org.fz.nettyx.serializer.xml.element.Prop.PropType.TYPE_ARGS_PATTERN;
import static org.fz.nettyx.util.BytesKit.LittleEndian.LE;

/**
 * convert int to string value
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public class EnumConverter implements TypeConverter {

    @Override
    public String forType() {
        return "enum";
    }

    public String convert(Prop prop, ByteBuf byteBuf, String defaultEnum) {
        return CharSequenceUtil.blankToDefault(this.convert(prop, byteBuf), defaultEnum);
    }

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();
        Throws.ifTrue(typeArgs.length > 1, "enum [" + type.getValue() + "] do not support 2 type args");

        String enumName = typeArgs[0];

        List<String> enums = XmlSerializerContext.findEnum(enumName);
        if (enums.isEmpty()) {
            return EMPTY;
        }

        int ordinary = this.findEnumIndex(prop, byteBuf);

        return enums.get(ordinary);
    }

    protected int findEnumIndex(Prop prop, ByteBuf buf) {
        byte[] bytes = this.readBytes(prop, buf);
        Endian endianKit = prop.getEndianKit();

        if (bytes.length < 4) {
            int destPos = LE.equals(endianKit.getOrder()) ? 0 : 4 - bytes.length;
            bytes = (byte[]) ArrayUtil.copy(bytes, 0, new byte[4], destPos, bytes.length);
        }

        return endianKit.toIntValue(bytes);
    }

    public static boolean convertible(String typeValue) {
        // enum must have type args
        return CharSequenceUtil.startWithIgnoreCase(typeValue, "enum") && TYPE_ARGS_PATTERN.matcher(typeValue)
            .matches();
    }

}
