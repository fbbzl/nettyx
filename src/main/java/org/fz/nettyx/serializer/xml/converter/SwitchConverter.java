package org.fz.nettyx.serializer.xml.converter;

import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.Throws;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */
public class SwitchConverter implements TypeConverter<String> {

    @Override
    public String convert(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();
        Throws.ifTrue(typeArgs.length > 1, "enum [" + type.getValue() + "] do not support 2 type args");

        String switchName = typeArgs[0];
        List<String> switches = XmlSerializerContext.findSwitch(switchName);

        byte[] bytes = readBytes(prop, byteBuf);
        BitSet bitSet = BitSet.valueOf(bytes);

        return getByBit(switches, bitSet);
    }

    private static String getByBit(List<String> switches, BitSet bitSet) {
        List<String> resultSwitches = new ArrayList<>(4);
        for (int i = 0; i < switches.size(); i++) {
            if (bitSet.get(i)) {
                resultSwitches.add(switches.get(i));
            }
        }

        return CharSequenceUtil.join(",", resultSwitches);
    }

}
