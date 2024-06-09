package org.fz.nettyx.serializer.xml;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.Throws;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model.Prop;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model.PropType;
import org.fz.nettyx.serializer.xml.handler.PropHandler;
import org.fz.nettyx.serializer.xml.handler.PropTypeHandler;
import org.mvel2.MVEL;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.fz.nettyx.exception.Exceptions.newIllegalArgException;


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
    private final Model   model;

    public static Dict read(ByteBuf byteBuf, Model model) {
        Throws.ifNull(model, "model can not be null");
        return new XmlSerializer(byteBuf, model).parse();
    }

    Dict parse() {
        Model   currentModel = getModel();
        ByteBuf reading      = getByteBuf();

        Map<String, Object> map = new LinkedHashMap<>();

        for (Prop prop : currentModel.getProps()) {
            try {
                PropType type = prop.getType();

                Object value;
                if (prop.hasHandler()) {
                    value = ((PropHandler) (Singleton.get(prop.getHandlerQName()))).read(prop, reading);

                } else if (prop.isArray()) {
                    value = readArray(prop, reading);
                } else if (XmlSerializerContext.containsType(type.getValue())) {
                    value = XmlSerializerContext.getTypeHandler(type.getValue()).read(prop, reading);
                } else throw new IllegalArgumentException("type not recognized [" + type + "]");

                if (prop.hasExpression()) {
                    value = MVEL.eval(prop.getExpression(), MapUtil.of("$v", value));
                }

                map.put(prop.getName(), value);
            } catch (UtilException hutoolException) {
                throw new IllegalArgumentException("can not find handler [" + prop.getHandlerQName() + "]");
            } catch (Exception exception) {
                throw new IllegalArgumentException("exception occur while analyzing prop [" + prop.getName() + "]", exception);
            }
        }

        return new Dict(map);
    }

    //*******************************           private start             ********************************************//

    private List<String> readArray(Prop prop, ByteBuf reading) {
        int arrayBytesLength = prop.getLength(), arrayLength = prop.getArrayLength();

        Throws.ifTrue(prop.getLength() % arrayLength != 0, newIllegalArgException(
                "illegal array config, array bytes length is [" + arrayBytesLength + "], nut array element size is ["
                + arrayLength + "]"));

        PropType type = prop.getType();

        PropTypeHandler handler = XmlSerializerContext.getTypeHandler(type.getValue());

        List<String> elements = new ArrayList<>(8);
        for (int i = 0; i < arrayLength; i++) {
            elements.add(handler.read(prop, reading));
        }

        return elements;
    }

    //*******************************           private end             ********************************************//
}
