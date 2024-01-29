package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dom4j.Element;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:11
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class ArrayPropElement extends PropElement {

    private final int length;

    public ArrayPropElement(Element element, int length) {
        super(element);
        this.length = length;
    }

}
