package org.fz.nettyx.serializer.xml;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.Serializer;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    public static class Student {

        private String name;

        private int age;

    }

    public static void main(String[] args) {
        Student student = new Student();
        student.setAge(11111256);
        student.setName("bb");

        Student unmarshal = JAXB.unmarshal("C:\\Users\\fengbinbin\\Desktop\\ff.txt", Student.class);

    }
}
