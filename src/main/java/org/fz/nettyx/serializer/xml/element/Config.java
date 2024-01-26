package org.fz.nettyx.serializer.xml.element;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/26 9:15
 */

@Data
@XmlRootElement
public class Config {

    private Bytes[] bytes;


    @Data
    public static class Bytes {


    }

    @Data
    public static class Bits {
    }
}
