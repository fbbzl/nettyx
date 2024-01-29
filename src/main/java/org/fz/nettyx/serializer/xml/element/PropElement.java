package org.fz.nettyx.serializer.xml.element;

import lombok.Data;
import org.dom4j.Element;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:07
 */

@Data
public class PropElement {

    public static final String ATTR_NAME = "name", ATTR_OFFSET = "offset", ATTR_SIZE = "size", ATTR_TYPE = "type";

    public final String name;
    public final String offset;
    public final String size;
    public final String type;

    protected PropElement(Element element) {
        try {
            this.name = element.attribute(ATTR_NAME).getValue();
            this.offset = element.attribute(ATTR_OFFSET).getValue();
            this.size = element.attribute(ATTR_SIZE).getValue();
            this.type = element.attribute(ATTR_TYPE).getValue();
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException("[" + ATTR_NAME + ", " + ATTR_OFFSET + ", " + ATTR_SIZE + ", " + ATTR_TYPE + "] all of them can not be null");
        }
    }

    public static ArrayPropElement arrayPropElement(Element element) {
        String type = element.attribute("type").getValue();

        return new ArrayPropElement(element, 1);
    }

    public static ExpPropElement expPropElement(Element element) {
        return new ExpPropElement(element);
    }

    public static HandlePropElement handlePropElement(Element element) {
        return new HandlePropElement(element);
    }

}
