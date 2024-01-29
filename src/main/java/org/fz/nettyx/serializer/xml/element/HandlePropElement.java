package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dom4j.Element;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:17
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class HandlePropElement extends PropElement {

    private final String handler;

    public HandlePropElement(Element element, String handler) {
        super(element);
        this.handler = handler;
    }
}
