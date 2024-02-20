package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.Model.PropElement;
import org.fz.nettyx.util.EndianKit;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static org.fz.nettyx.util.EndianKit.LE;

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
    public String read(PropElement prop, ByteBuf reading) {
        String[] enums = XmlSerializerContext.findEnum(prop);

        if (ArrayUtil.isEmpty(enums)) {
            return EMPTY;
        }

        return enums[this.findEnumOrdinary(prop, reading)];
    }

    @Override
    public void write(PropElement prop, ByteBuf writing) {
        int ordinary = Integer.parseInt(prop.getText());
        EndianKit endianKit = prop.getEndianKit();

        endianKit.fromInt(ordinary);
    }

    protected int findEnumOrdinary(PropElement prop, ByteBuf buf) {
        byte[] bytes = this.readBytes(prop, buf);
        EndianKit endianKit = prop.getEndianKit();

        if (bytes.length < 4) {
            int destPos = LE == endianKit ? 0 : 4 - bytes.length;
            bytes = (byte[]) ArrayUtil.copy(bytes, 0, new byte[4], destPos, bytes.length);
        }

        return endianKit.toInt(bytes);
    }

}
