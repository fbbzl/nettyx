package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Type;

import java.io.File;


/**
 * a xml bytebuf serializer
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 9:35
 */
@Getter
@RequiredArgsConstructor
public final class XmlSerializer implements Serializer {

    private final ByteBuf byteBuf;
    private final Model model;

    public static Model read(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).parseDoc();
    }

    /**
     * 将没有值的xml根据配置填写上值
     *
     * @return
     */
    Model parseDoc() {
        for (Prop prop : getModel().getProps()) {
            Type type = prop.getType();
            if (type.isNumber()) {

            }
            if (type.isString()) {

            }
            if (type.isModel()) {

            }
            if (type.isArray()) {

            }

        }



        return null;
    }

    /**
     * 将有值的xml转化成bytebuf
     *
     * @return
     */
    ByteBuf toByteBuf() {


        return null;
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\pc\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\pc\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);


    }
}
