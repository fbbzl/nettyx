package org.fz.nettyx.serializer.xml.element;

import lombok.Data;
import org.dom4j.Element;
import org.fz.nettyx.serializer.xml.XmlUtils;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:07
 */

@Data
public class Prop {

    public static final String NAMESPACE = "namespace";
    public static final String
            ATTR_REF = "ref",
            ATTR_NAME = "name",
            ATTR_OFFSET = "offset",
            ATTR_SIZE = "size",
            ATTR_TYPE = "type",
            ATTR_EXP = "exp",
            ATTR_HANDLER = "handler";

    public final String name;
    public final String offset;
    public final String size;
    public final String type;

    public String exp;
    private String handler;

    public Prop(Element el) {
        try {
            this.name = XmlUtils.attrValue(el, ATTR_NAME);
            this.offset = XmlUtils.attrValue(el, ATTR_OFFSET);
            this.size = XmlUtils.attrValue(el, ATTR_SIZE);
            this.type = XmlUtils.attrValue(el, ATTR_TYPE);
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(el + "[" + ATTR_NAME + ", " + ATTR_OFFSET + ", " + ATTR_SIZE + ", " + ATTR_TYPE + "] all of them can not be null");
        }

        /* ext prop */
        this.exp = XmlUtils.attrValue(el, ATTR_EXP);
        this.handler = XmlUtils.attrValue(el, ATTR_HANDLER);
    }

}
