package org.fz.nettyx.serializer.xml.handler;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static org.fz.nettyx.util.EndianKit.LE;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.XmlModel.XmlProp;
import org.fz.nettyx.util.EndianKit;

/**
 * read int to string value
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */

public class EnumHandler implements XmlPropHandler {

    @Override
    public String forType() {
        return "enum";
    }

    @Override
    public String read(XmlProp prop, ByteBuf reading) {
        String[] enums = XmlSerializerContext.findEnum(prop);

        if (ArrayUtil.isEmpty(enums)) {
            return EMPTY;
        }

        return enums[this.findEnumOrdinary(prop, reading)];
    }

    @Override
    public void write(XmlProp prop, ByteBuf writing) {
        String text = prop.getText();
        int ordinary = Integer.parseInt(text);
        EndianKit endianKit = prop.getEndianKit();

        endianKit.fromInt(ordinary);
    }

    protected int findEnumOrdinary(XmlProp prop, ByteBuf buf) {
        byte[] bytes = this.readBytes(prop, buf);
        EndianKit endianKit = prop.getEndianKit();

        if (bytes.length < 4) {
            int destPos = LE == endianKit ? 0 : 4 - bytes.length;
            bytes = (byte[]) ArrayUtil.copy(bytes, 0, new byte[4], destPos, bytes.length);
        }

        return endianKit.toInt(bytes);
    }

}
