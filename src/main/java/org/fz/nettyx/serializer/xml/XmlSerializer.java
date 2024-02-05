package org.fz.nettyx.serializer.xml;

import static org.fz.nettyx.serializer.xml.dtd.Dtd.EL_MODEL;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXWriter;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Type;


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

    public static Document read(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).parseDoc();
    }

    /**
     * return a document with a model
     *
     * @return
     */
    Document parseDoc() {
        SAXWriter saxWriter = new SAXWriter();
        Document document = new DOMDocument();
        Element model = new DOMElement(EL_MODEL);

        document.setRootElement(model);

        // 生成一个只带有Model的xml document文件最好是
        for (Prop prop : getModel().getProps()) {
            Type type = prop.getType();

            if (prop.useHandler()) {
                // new handler and invoke method

            } else if (type.isNumber()) {
                addNumberEl(prop, model);
            } else if (type.isString()) {

            } else if (type.isArray()) {

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

    //*******************************           private start             ********************************************//

    private void addNumberEl(Prop prop, Element model) {

    }

    //*******************************           private end             ********************************************//

    public static void main(String[] args) {
        File file = new File("C:\\Users\\fengbinbin\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\fengbinbin\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 1);
        XmlSerializer.read(Unpooled.wrappedBuffer(bytes), XmlSerializerContext.findModel("bank", "bankcard"));
    }
}
