package org.fz.nettyx.serializer.xml;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.text.CharSequenceUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.XmlModel;
import org.fz.nettyx.serializer.xml.element.XmlModel.XmlProp;
import org.fz.nettyx.serializer.xml.element.XmlModel.XmlProp.PropType;
import org.fz.nettyx.serializer.xml.handler.XmlPropHandler;


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
    private final XmlModel model;

    /**
     * Read document.
     *
     * @param byteBuf the byte buf
     * @param model   the model
     * @return the document
     */
    public static Document read(ByteBuf byteBuf, XmlModel model) {
        return new XmlSerializer(byteBuf, model).parseDoc();
    }

    public static ByteBuf write(ByteBuf byteBuf, XmlModel model) {
        return new XmlSerializer(byteBuf, model).toByteBuf();
    }

    /**
     * return a document with a model
     *
     * @return the document
     */
    Document parseDoc() {
        XmlModel currentModel = getModel();
        ByteBuf reading = getByteBuf();

        Element rootModel = new DOMElement(currentModel.getName());
        Document document = DocumentHelper.createDocument(rootModel);

        for (XmlProp prop : currentModel.getProps()) {
            try {
                Element propEl = prop.toElement();
                PropType type = prop.getType();

                if (prop.useHandler()) {
                    propEl.setText(((XmlPropHandler) (Singleton.get(prop.getHandlerQName()))).read(prop, reading));
                } else if (type.isArray()) {
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
        XmlModel currentModel = getModel();
        ByteBuf writing = getByteBuf();

        for (XmlProp prop : currentModel.getProps()) {
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

    private List<Node> readArray(XmlProp prop, ByteBuf reading) {
        PropType type = prop.getType();
        if (!type.isArray()) {
            return Collections.emptyList();
        }

        String arrayElementName = XmlUtils.arrayElementName(prop);
        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();

        XmlPropHandler handler = XmlSerializerContext.getHandler(typeValue);

        List<Node> elements = new ArrayList<>(8);
        for (int i = 0; i < arrayLength; i++) {
            Node node = new DOMElement(arrayElementName);
            node.setText(handler.read(prop, reading));

            elements.add(node);
        }

        return elements;
    }

    private void writeArray(XmlProp prop, ByteBuf writing) {
        PropType type = prop.getType();
        if (!type.isArray()) return;

        int arrayLength = type.getArrayLength();
        String typeValue = type.getValue();

        XmlPropHandler handler = XmlSerializerContext.getHandler(typeValue);

        List<Node> content = prop.getContent();
        for (Node node : content) {
            String text = CharSequenceUtil.trim(node.getText());
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
        File file = new File("C:\\Users\\pc\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\pc\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 0);
        XmlModel model1 = XmlSerializerContext.findModel("stu");
        Document doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        System.err.println(doc.asXML());
    }

}
