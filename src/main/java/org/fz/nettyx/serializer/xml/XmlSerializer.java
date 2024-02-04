package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import java.io.File;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.Serializer;


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

    public static Map<String, Object> read(ByteBuf byteBuf) {
        return new XmlSerializer(byteBuf).toMap();
    }

    public Map<String, Object> toMap() {




        return null;
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\\\pc\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\\\pc\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);


    }
}
