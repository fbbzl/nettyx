package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.fz.nettyx.serializer.Serializer;

import java.io.File;


/**
 * a xml bytebuf serializer
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 9:35
 */
@Getter
@RequiredArgsConstructor
public class XmlSerializer implements Serializer {

    private final ByteBuf byteBuf;
    private final Document doc;

    public static Document read(ByteBuf byteBuf, Document doc) {
        return new XmlSerializer(byteBuf, doc).parseDoc();
    }

    /**
     * 将没有值的xml根据配置填写上值
     *
     * @return
     */
    Document parseDoc() {


        return null;
    }

    /**
     * 将有值的xml转化成bytebuf
     *
     * @return
     */
    ByteBuf toByteBuf() {


        return null;
    }


    public static void main(String[] args) {
        File file = new File("C:\\Users\\\\pc\\Desktop\\school.xml");
        File file2 = new File("C:\\Users\\\\pc\\Desktop\\bank.xml");
        XmlSerializerContext xmlSerializerContext = new XmlSerializerContext(file, file2);


    }
}
