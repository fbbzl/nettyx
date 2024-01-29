package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.Element;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.fz.nettyx.serializer.xml.Dtd.NAME;
import static org.fz.nettyx.serializer.xml.Dtd.REF;


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
        Map<String, List<Element>> config = new LinkedHashMap<>(64);

        SAXReader reader = SAXReader.createDefault();
        Document document = reader.read(new File("C:\\Users\\fengbinbin\\Desktop\\bytes.txt"));
        org.dom4j.Element rootElement = document.getRootElement();
        System.err.println(rootElement.attribute(REF).getValue());


        for (org.dom4j.Element element : rootElement.elements()) {

            System.err.println(element.attribute(NAME).getValue());

        }


        System.err.println(document.getStringValue());
        System.err.println(document);
    }
}
