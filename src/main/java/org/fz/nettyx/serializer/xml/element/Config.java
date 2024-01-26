package org.fz.nettyx.serializer.xml.element;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/26 9:15
 */

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private Bytes[] bytes;

    @Data
    public static class Bytes {
        private int index;
        private int length;

        private Bits bits;
    }

    @Data
    public static class Bits {

        private boolean[] value;

    }
}
