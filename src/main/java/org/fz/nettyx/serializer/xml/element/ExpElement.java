package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:16
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class ExpElement extends Element {

    public final String exp;

    public ExpElement(org.dom4j.Element element, String exp) {
        super(element);
        this.exp = exp;
    }
}
