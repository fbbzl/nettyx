package org.fz.nettyx.serializer.xml;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

import cn.hutool.core.lang.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.converter.TypeConverter;
import org.fz.nettyx.serializer.xml.element.ElementHandler;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.PropType;


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
                }
                else
                if (type.isArray()) {
                    text = parseArray(prop, getByteBuf());
                }
                else
                if (XmlSerializerContext.containsType(typeValue)) {
                    text = XmlSerializerContext.getConverter(typeValue).convert(prop, getByteBuf());
                }
                else throw new IllegalArgumentException("this type is not recognized [" + type + "]");

                propEl.setText(text);
                rootModel.add(propEl);
            } catch (Exception exception) {
                throw new IllegalArgumentException("exception occur while analyzing prop [" + prop + "]", exception);
            }
        }

        return document;
    }

    /**
     *
     * @return byte buf
     */
    ByteBuf toByteBuf() {


        return null;
    }

    //*******************************           private start             ********************************************//

    private String parseArray(Prop prop, ByteBuf byteBuf) {
        PropType type = prop.getType();
        if (!type.isArray()) {
            return EMPTY;
        }

        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();

        TypeConverter converter = XmlSerializerContext.getConverter(typeValue);

        StringJoiner values = new StringJoiner(",");
        for (int i = 0; i < arrayLength; i++) {
            values.add(converter.convert(prop, byteBuf));
        }

        return values.toString();
    }

    //*******************************           private end             ********************************************//

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\pc\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\pc\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 0);
        Model model1 = XmlSerializerContext.findModel("stu");
        Document doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc.asXML());
    }

}
