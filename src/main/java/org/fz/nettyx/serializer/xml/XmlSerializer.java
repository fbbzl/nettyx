package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.Serializer;

import java.io.File;


/**
 * the location of the xml file is scanned
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 9:35
 */
@Getter
@RequiredArgsConstructor
public class XmlSerializer implements Serializer {

    private final ByteBuf byteBuf;

    private final File xml;

    public static void main(String[] args) throws DocumentException {
        SAXReader reader = SAXReader.createDefault();
        Document document = reader.read(new File("C:\\Users\\fengbinbin\\Desktop\\bytes.txt"));
        Element rootElement = document.getRootElement();
        System.err.println(rootElement.attribute("ref").getValue());
        for (Element element : rootElement.elements()) {
            System.err.println(element.attribute("name").getValue());

        }


        System.err.println(document.getStringValue());
        System.err.println(document);
    }
}
