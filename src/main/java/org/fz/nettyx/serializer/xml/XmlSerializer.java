package org.fz.nettyx.serializer.xml;

import cn.hutool.core.lang.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.converter.*;
import org.fz.nettyx.serializer.xml.element.ElementHandler;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


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

    /**
     * Read document.
     *
     * @param byteBuf the byte buf
     * @param model the model
     * @return the document
     */
    public static Document read(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).parseDoc();
    }

    /**
     * return a document with a model
     *
     * @return the document
     */
    Document parseDoc() {
        Element rootModel = new DOMElement(getModel().getName());
        Document document = DocumentHelper.createDocument(rootModel);

        for (Prop prop : getModel().getProps()) {
            try {
                Element propEl = new DOMElement(prop.getName());
                PropType type = prop.getType();
                String typeValue = type.getValue();

                String text;
                if (prop.useHandler()) {
                    String handlerClassQName = prop.getHandler();
                    text = ((ElementHandler) (Singleton.get(handlerClassQName))).read(prop, getByteBuf());
                } else if (NumberConverter.convertible(typeValue)) {
                    text = NumberConverter.getConverter(prop).convert(prop, getByteBuf());
                } else if (type.isString()) {
                    text = Singleton.get(StringConverter.class).convert(prop, getByteBuf());
                } else if (type.isEnum()) {
                    text = Singleton.get(EnumConverter.class).convert(prop, getByteBuf());
                } else if (type.isSwitch()) {
                    text = Singleton.get(SwitchConverter.class).convert(prop, getByteBuf());
                } else if (type.isArray()) {
                    text = Singleton.get(ArrayConverter.class).convert(prop, getByteBuf());
                } else {
                    throw new IllegalArgumentException("this type is not recognized [" + type + "]");
                }
                propEl.setText(text);
                rootModel.add(propEl);
            } catch (Exception exception) {
                throw new IllegalArgumentException("exception occur while analyzing prop [" + prop + "]", exception);
            }
        }

        return document;
    }

    /**
     * 将有值的xml转化成bytebuf
     *
     * @return byte buf
     */
    ByteBuf toByteBuf() {

        return null;
    }

    //*******************************           private start             ********************************************//

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
        Document doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc.asXML());
    }

}
