package org.fz.nettyx.serializer.xml;

import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_LENGTH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_TYPE;
import static org.fz.nettyx.util.Exceptions.newIllegalArgException;

import cn.hutool.core.lang.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Model.PropElement;
import org.fz.nettyx.serializer.xml.element.Model.PropElement.PropType;
import org.fz.nettyx.serializer.xml.handler.XmlPropHandler;
import org.fz.nettyx.util.Throws;


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

        for (PropElement prop : currentModel.getProps()) {
            try {
                Element propEl = prop.copy();
                PropType type = prop.getType();

                if (prop.useHandler()) {
                    propEl.setText(((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).read(prop, reading));
                } else if (prop.isArray()) {
                    propEl.setContent(readArray(prop, reading));
                } else if (XmlSerializerContext.containsType(type.getValue())) {
                    propEl.setText(XmlSerializerContext.getHandler(type.getValue()).read(prop, reading));
                } else throw new IllegalArgumentException("type not recognized [" + type + "]");

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

        for (PropElement prop : currentModel.getProps()) {
            PropType type = prop.getType();
            String typeValue = type.getValue();

            if (prop.useHandler()) {
                ((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).write(prop, writing);
            } else if (prop.isArray()) {
                writeArray(prop, writing);
            } else if (XmlSerializerContext.containsType(typeValue)) {
                XmlSerializerContext.getHandler(typeValue).write(prop, writing);
            } else throw new IllegalArgumentException("type not recognized [" + type + "]");

        }
        return null;
    }

    //*******************************           private start             ********************************************//

    private List<Node> readArray(PropElement prop, ByteBuf reading) {
        int arrayBytesLength = prop.getLength(),
            arrayLength = prop.getArrayLength(),
            elementByteLength = arrayBytesLength / arrayLength,
            offset = prop.getOffset();

        Throws.ifTrue(prop.getLength() % arrayLength != 0, newIllegalArgException(
            "illegal array config, array bytes length is [" + arrayBytesLength + "], nut array element size is ["
                + arrayLength + "]"));

        String arrayElementName = XmlUtils.arrayElementName(prop);
        PropType type = prop.getType();

        XmlPropHandler handler = XmlSerializerContext.getHandler(type.getValue());

        List<Node> elements = new ArrayList<>(8);
        for (int i = 0; i < arrayLength; i++) {
            Element el = new DOMElement(arrayElementName);
            try {
                el.addAttribute(ATTR_OFFSET, String.valueOf(offset));
                el.addAttribute(ATTR_LENGTH, String.valueOf(elementByteLength));
                el.addAttribute(ATTR_TYPE, type.getTypeText());

                el.setText(handler.read(prop, reading));
                elements.add(el);
            } finally {
                offset += elementByteLength;
            }
        }

        return elements;
    }

    private void writeArray(PropElement prop, ByteBuf writing) {
        PropType type = prop.getType();

        int arrayLength = prop.getArrayLength();
        String typeValue = type.getValue();

        XmlPropHandler handler = XmlSerializerContext.getHandler(typeValue);

        List<PropElement> elements = prop.propElements();
        for (PropElement propEl : elements) {
            handler.write(propEl, writing);
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
