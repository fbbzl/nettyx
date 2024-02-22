package org.fz.nettyx.serializer.xml;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Model.Prop;
import org.fz.nettyx.serializer.xml.element.Model.Prop.PropType;
import org.fz.nettyx.serializer.xml.handler.XmlPropHandler;
import org.fz.nettyx.util.Throws;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.fz.nettyx.util.Exceptions.newIllegalArgException;


/**
 * a xml bytebuf serializer
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 9:35
 */
@Getter
@RequiredArgsConstructor
public final class XmlSerializer implements Serializer {

    private final ByteBuf byteBuf;
    private final Model model;

    public static Dict read(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).parse();
    }

    Dict parse() {
        Model currentModel = getModel();
        ByteBuf reading = getByteBuf();

        Map<String, Object> map = new LinkedHashMap<>();

        for (Prop prop : currentModel.getProps()) {
            try {
                PropType type = prop.getType();

                Object value;
                if (prop.useHandler()) {
                    value = ((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).read(prop, reading);
                } else if (prop.isArray()) {
                    value = readArray(prop, reading);
                } else if (XmlSerializerContext.containsType(type.getValue())) {
                    value = XmlSerializerContext.getHandler(type.getValue()).read(prop, reading);
                } else throw new IllegalArgumentException("type not recognized [" + type + "]");

                map.put(prop.getName(), value);
            } catch (Exception exception) {
                throw new IllegalArgumentException("exception occur while analyzing prop [" + prop + "]", exception);
            }
        }

        return new Dict(map);
    }

    //*******************************           private start             ********************************************//

    private List<String> readArray(Prop prop, ByteBuf reading) {
        int arrayBytesLength = prop.getLength(),
                arrayLength = prop.getArrayLength();

        Throws.ifTrue(prop.getLength() % arrayLength != 0, newIllegalArgException(
                "illegal array config, array bytes length is [" + arrayBytesLength + "], nut array element size is ["
                        + arrayLength + "]"));

        PropType type = prop.getType();

        XmlPropHandler handler = XmlSerializerContext.getHandler(type.getValue());

        List<String> elements = new ArrayList<>(8);
        for (int i = 0; i < arrayLength; i++) {
            elements.add(handler.read(prop, reading));
        }

        return elements;
    }

    //*******************************           private end             ********************************************//

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\fengbinbin\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\fengbinbin\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 0);
        Model model1 = XmlSerializerContext.findModel("stu");
        Dict doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc);
    }

}
