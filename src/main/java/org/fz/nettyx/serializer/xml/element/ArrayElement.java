package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:11
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class ArrayElement extends Element {

    private final String length;

    public ArrayElement(org.dom4j.Element element, String length) {
        super(element);
        this.length = length;
    }

}
