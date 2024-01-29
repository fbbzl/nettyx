package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:17
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class HandleElement extends Element {

    private final String handler;

    public HandleElement(org.dom4j.Element element, String handler) {
        super(element);
        this.handler = handler;
    }
}
