package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model.Prop;
import org.fz.nettyx.util.EndianKit;
import org.fz.nettyx.util.Throws;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static org.fz.nettyx.util.EndianKit.LE;

/**
 * read int value to the enum which defined in the XML, NOT java enum
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public class EnumHandler implements PropTypeHandler {

    @Override
    public String forType() {
        return "enum";
    }

    @Override
    public String read(Prop prop, ByteBuf reading) {
        String[] enums = XmlSerializerContext.findEnum(prop);

        if (ArrayUtil.isEmpty(enums)) {
            return EMPTY;
        }
        int enumOrdinary = this.findEnumOrdinary(prop, reading);
        Throws.ifTrue(enumOrdinary > enums.length - 1, "can not find enum [" + prop.getType().getTypeArgs()[0] + "] by ordinary [" + enumOrdinary + "]");

        return enums[enumOrdinary];
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        int       ordinary  = Integer.parseInt(prop.getText());
        EndianKit endianKit = prop.getEndianKit();

        endianKit.fromInt(ordinary);
    }

    protected int findEnumOrdinary(Prop prop, ByteBuf buf) {
        byte[]    bytes     = this.readBytes(prop, buf);
        EndianKit endianKit = prop.getEndianKit();

        if (bytes.length < 4) {
            int destPos = LE == endianKit ? 0 : 4 - bytes.length;
            bytes = (byte[]) ArrayUtil.copy(bytes, 0, new byte[4], destPos, bytes.length);
        }

        return endianKit.toInt(bytes);
    }

}
