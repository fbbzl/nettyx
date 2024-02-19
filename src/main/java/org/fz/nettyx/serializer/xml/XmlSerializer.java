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
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;
import org.fz.nettyx.serializer.xml.handler.XmlPropHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringJoiner;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;


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
     * @param model   the model
     * @return the document
     */
    public static Document read(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).parseDoc();
    }

    public static ByteBuf write(ByteBuf byteBuf, Model model) {
        return new XmlSerializer(byteBuf, model).toByteBuf();
    }

    /**
     * return a document with a model
     *
     * @return the document
     */
    Document parseDoc() {
        Model currentModel = getModel();
        ByteBuf reading = getByteBuf();

        Element rootModel = new DOMElement(currentModel.getName());
        Document document = DocumentHelper.createDocument(rootModel);

        for (Prop prop : currentModel.getProps()) {
            try {
                Element propEl = prop.toElement();
                PropType type = prop.getType();

                String text;
                if (prop.useHandler()) {
                    text = ((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).read(prop, reading);
                }
                else
                if (type.isArray()) {
                    text = readArray(prop, reading);
                }
                else
                if (XmlSerializerContext.containsType(type.getValue())) {
                    text = XmlSerializerContext.getHandler(type.getValue()).read(prop, reading);
                }
                else throw new IllegalArgumentException("type not recognized [" + type + "]");

                propEl.setText(text);
                rootModel.add(propEl);
            } catch (Exception exception) {
                throw new IllegalArgumentException("exception occur while analyzing prop [" + prop + "]", exception);
            }
        }

        return document;
    }

    /**
     * @return byte buf
     */
    ByteBuf toByteBuf() {
        Model currentModel = getModel();
        ByteBuf writing = getByteBuf();

        for (Prop prop : currentModel.getProps()) {
            PropType type = prop.getType();
            String typeValue = type.getValue();

            if (prop.useHandler()) {
                ((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).write(prop, writing);
            } else if (type.isArray()) {
                writeArray(prop, writing);
            } else if (XmlSerializerContext.containsType(typeValue)) {
                XmlSerializerContext.getHandler(typeValue).write(prop, writing);
            } else throw new IllegalArgumentException("type not recognized [" + type + "]");

        }
        return null;
    }

    //*******************************           private start             ********************************************//

    private String readArray(Prop prop, ByteBuf reading) {
        PropType type = prop.getType();
        if (!type.isArray()) {
            return EMPTY;
        }

        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();

        XmlPropHandler handler = XmlSerializerContext.getHandler(typeValue);

        StringJoiner values = new StringJoiner(",");
        for (int i = 0; i < arrayLength; i++) {
            values.add(handler.read(prop, reading));
        }

        return values.toString();
    }

    private void writeArray(Prop prop, ByteBuf writing) {
        PropType type = prop.getType();
        if (!type.isArray()) return;

        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();

        XmlPropHandler handler = XmlSerializerContext.getHandler(typeValue);

        for (int i = 0; i < arrayLength; i++) {
            handler.write(prop, writing);
        }

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
        Document doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc.asXML());
    }

}
