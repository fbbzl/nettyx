package org.fz.nettyx.serializer.xml.element;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dom4j.Element;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:16
 */

@Getter
@EqualsAndHashCode(callSuper = false)
public class ExpPropElement extends PropElement {

    public static final String ATTR_EXP = "exp";

    public final String exp;

    public ExpPropElement(Element element) {
        super(element);
        this.exp = element.attribute(ATTR_EXP).getValue();
    }
}
