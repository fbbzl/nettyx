package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.util.Throws;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/6 22:20
 */
public class SwitchHandler implements XmlPropHandler {

    @Override
    public String forType() {
        return "switch";
    }

    @Override
    public String read(Prop prop, ByteBuf reading) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();
        Throws.ifTrue(typeArgs.length > 1, "enum [" + type.getValue() + "] do not support 2 type args");

        String switchName = typeArgs[0];
        List<String> switches = XmlSerializerContext.findSwitch(switchName);

        byte[] bytes = readBytes(prop, reading);
        BitSet bitSet = BitSet.valueOf(bytes);

        return getByBitSet(switches, bitSet);
    }

    @Override
    public void write(Prop prop, ByteBuf writing) {
        
    }

    private static String getByBitSet(List<String> switches, BitSet bitSet) {
        List<String> resultSwitches = new ArrayList<>(4);
        for (int i = 0; i < switches.size(); i++) {
            if (bitSet.get(i)) {
                resultSwitches.add(switches.get(i));
            }
        }

        return CharSequenceUtil.join(",", resultSwitches);
    }

}
