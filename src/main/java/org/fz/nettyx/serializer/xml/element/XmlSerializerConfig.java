package org.fz.nettyx.serializer.xml.element;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/26 9:15
 */

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSerializerConfig {

    private Bytes[] bytes;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Bytes {
        @XmlAttribute
        private int index;
        @XmlAttribute
        private int length;

        private Bits[] bits;

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Bits {

        private boolean[] value;

    }
}
