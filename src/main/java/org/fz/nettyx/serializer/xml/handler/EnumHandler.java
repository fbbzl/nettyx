package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.util.BytesKit.Endian;

import java.util.List;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static org.fz.nettyx.util.BytesKit.LittleEndian.LE;

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
    public String read(Prop prop, ByteBuf reading) {
        List<String> enums = XmlSerializerContext.findEnum(prop);

        if (enums.isEmpty()) {
            return EMPTY;
        }

        return enums.get(this.findEnumOrdinary(prop, reading));
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        String text = prop.getText();
        int ordinary = Integer.parseInt(text);
        Endian endianKit = prop.getEndianKit();

        endianKit.fromIntValue(ordinary);
    }

    protected int findEnumOrdinary(Prop prop, ByteBuf buf) {
        byte[] bytes = this.readBytes(prop, buf);
        Endian endianKit = prop.getEndianKit();

        if (bytes.length < 4) {
            int destPos = LE.equals(endianKit.getOrder()) ? 0 : 4 - bytes.length;
            bytes = (byte[]) ArrayUtil.copy(bytes, 0, new byte[4], destPos, bytes.length);
        }

        return endianKit.toIntValue(bytes);
    }

}
