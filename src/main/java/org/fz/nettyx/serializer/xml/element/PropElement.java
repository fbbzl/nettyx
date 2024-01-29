package org.fz.nettyx.serializer.xml.element;

import lombok.Data;
import org.dom4j.Element;

import static org.fz.nettyx.serializer.xml.Dtd.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:07
 */

@Data
public class PropElement {

    public final String name;
    public final String offset;
    public final String size;
    public final String type;

    private PropElement(Element element) {
        try {
            this.name = element.attribute(NAME).getValue();
            this.offset = element.attribute(OFFSET).getValue();
            this.size = element.attribute(SIZE).getValue();
            this.type = element.attribute(TYPE).getValue();
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException("[" + NAME + ", " + OFFSET + ", " + SIZE + ", " + TYPE + "] all of them can not be null");
        }
    }

    public static ArrayPropElement arrayPropElement(Element element) {

    }

    public static ArrayPropElement expPropElement(Element element) {

    }

    public static ArrayPropElement handlePropElement(Element element) {

    }

}
