package org.fz.nettyx.serializer.xml.element;

import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_EXP;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_HANDLER;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_LENGTH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_NAME;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET_TYPE;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_TYPE;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.dom4j.Element;
import org.fz.nettyx.serializer.xml.XmlUtils;
import org.fz.nettyx.serializer.xml.element.Model.OffsetType;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:07
 */

@Data
public class Prop {

    public final String name;
    public final Counter counter;
    public final Integer length;
    public final Type type;

    public String exp;
    private String handler;

    public Prop(Element propEl) {
        try {
            this.name = XmlUtils.attrValue(propEl, ATTR_NAME);
            this.counter = new Counter(Integer.parseInt(XmlUtils.attrValue(propEl, ATTR_OFFSET)), this.getModelOffsetType(propEl));
            this.length = Integer.parseInt(XmlUtils.attrValue(propEl, ATTR_LENGTH));
            this.type = new Type(XmlUtils.attrValue(propEl.getDocument().getRootElement(), NAMESPACE), XmlUtils.attrValue(propEl, ATTR_TYPE));
        } catch (Exception exception) {
            throw new IllegalArgumentException(propEl.getName() + "[" + ATTR_NAME + ", " + ATTR_OFFSET + ", " + ATTR_LENGTH + ", " + ATTR_TYPE + "] all of them can not be null");
        }

        /* ext prop */
        this.exp = XmlUtils.attrValue(propEl, ATTR_EXP);
        this.handler = XmlUtils.attrValue(propEl, ATTR_HANDLER);
    }

    //**************************************           private start              ************************************//

    OffsetType getModelOffsetType(Element propEl) {
        Element parentModel = propEl.getParent();
        String offsetType = XmlUtils.attrValue(parentModel, ATTR_OFFSET_TYPE);
        return EnumUtil.fromString(OffsetType.class, offsetType, OffsetType.RELATIVE);
    }

    @Data
    @AllArgsConstructor
    public static class Counter {
        // todo 相对绝对
        private int index;
        private OffsetType offsetType;
    }

}
