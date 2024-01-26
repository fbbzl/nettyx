package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.xml.element.XmlSerializerConfig;

import javax.xml.bind.JAXB;
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

    public static void main(String[] args) {
        XmlSerializerConfig unmarshal = JAXB.unmarshal("C:\\Users\\fengbinbin\\Desktop\\ff.txt", XmlSerializerConfig.class);
        System.err.println(unmarshal);
    }
}
