package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Prop;
import org.fz.nettyx.serializer.xml.element.Prop.Type;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;


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
     */
    Document parseDoc() {
        Element rootModel = new DOMElement(model.getName());
        Document document = DocumentHelper.createDocument(rootModel);

        // 生成一个只带有Model的xml document文件最好是
        for (Prop prop : getModel().getProps()) {
            Element propEl = new DOMElement(prop.getName());
            Type type = prop.getType();

            if (prop.useHandler()) {
                // new handler and invoke method

            } else if (type.isNumber()) {
                propEl.setText("isNumber");
            } else if (type.isString()) {
                propEl.setText("isString");
            } else if (type.isEnum()) {
                propEl.setText("isEnum");
            } else if (type.isSwitch()) {
                propEl.setText("isSwitch");
            } else if (type.isArray()) {
                propEl.setText("isArray");
            }

            rootModel.add(propEl);
        }

        return document;
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

    private static void writeXml(Writer writer, Document doc) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();

        XMLWriter xmlWriter = new XMLWriter(writer, format);

        xmlWriter.write(doc);

        xmlWriter.close();

    }

    //*******************************           private end             ********************************************//

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\fengbinbin\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\fengbinbin\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);

        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 1);
        Model model1 = XmlSerializerContext.findModel("bkca");
        Document doc = XmlSerializer.read(Unpooled.wrappedBuffer(bytes), model1);

        FileWriter fileWriter = new FileWriter("C:\\Users\\fengbinbin\\Desktop\\asdfasdf.xml");
        writeXml(fileWriter, doc);
        System.err.println();
    }


}
