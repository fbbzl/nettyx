package org.fz.nettyx.serializer.xml.handler;

import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.xml.XmlSerializerContext;
import org.fz.nettyx.serializer.xml.element.XmlModel.XmlProp;

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
    public String read(XmlProp prop, ByteBuf reading) {
        String[] switches = XmlSerializerContext.findSwitch(prop);

        byte[] bytes = readBytes(prop, reading);
        BitSet bitSet = BitSet.valueOf(bytes);

        return readSwitches(switches, bitSet);
    }

    @Override
    public void write(XmlProp prop, ByteBuf writing) {
        String[] switchValues = CharSequenceUtil.splitToArray(prop.getText(), ",");
        String[] switches = XmlSerializerContext.findSwitch(prop);

        byte[] bytes = writeSwitches(switches, switchValues);

        writing.writeBytes(bytes);
    }

    private static String readSwitches(String[] switches, BitSet bitSet) {
        List<String> resultSwitches = new ArrayList<>(4);
        for (int i = 0; i < switches.length; i++) {
            if (bitSet.get(i)) {
                resultSwitches.add(switches[i]);
            }
        }

        return CharSequenceUtil.join(",", resultSwitches);
    }

    private static byte[] writeSwitches(String[] switches, String... switchesValues) {
        byte[] bytes = new byte[switches.length];
        BitSet bitSet = BitSet.valueOf(bytes);

        for (int i = 0; i < switchesValues.length; i++) {
            if (switchesValues[i].equals(switches[i])) {
                bitSet.set(i);
            }
        }

        return bitSet.toByteArray();
    }

}
