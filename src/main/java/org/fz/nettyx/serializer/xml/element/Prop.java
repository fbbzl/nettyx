package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.util.BytesKit;
import org.fz.nettyx.util.BytesKit.Endian;

import java.util.regex.Pattern;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static org.fz.nettyx.serializer.xml.XmlUtils.attrValue;
import static org.fz.nettyx.serializer.xml.XmlUtils.name;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.*;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 14:07
 */

@Data
public class Prop {

    private final String name;
    private final int offset;
    private final int length;
    private final Endian endianKit;
    private final PropType type;
    private final String exp;
    private final String handler;

    public Prop(Element propEl) {
        this.name = name(propEl);
        this.offset = Integer.parseInt(attrValue(propEl, ATTR_OFFSET));
        this.length = Integer.parseInt(attrValue(propEl, ATTR_LENGTH));
        this.endianKit = LE.equals(attrValue(propEl, ATTR_ORDER)) ? BytesKit.le : BytesKit.be;
        this.type = new PropType(attrValue(propEl, ATTR_TYPE));
        this.exp = attrValue(propEl, ATTR_EXP);
        this.handler = attrValue(propEl, ATTR_HANDLER);
    }

    public boolean useHandler() {
        return CharSequenceUtil.isNotBlank(getHandler());
    }

    public Element toElement() {
        return new DOMElement(getName());
    }

    //**************************************           private start              ************************************//

    //**************************************           private end                ************************************//

    @Data
    public static class PropType {

        public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");
        public static final Pattern ARRAY_PATTERN = Pattern.compile("^(.+)\\[\\d+]$");
        public static final Pattern TYPE_ARGS_PATTERN = Pattern.compile("^(.+)\\(.+\\)$");

        private final String[] typeArgs;
        private final String type;

        public PropType(String typeText) {
            boolean hasTypeArgs = TYPE_ARGS_PATTERN.matcher(typeText).matches();

            this.typeArgs = hasTypeArgs ? splitToArray(subBetween(typeText, "(", ")"), ",") : new String[0];

            this.type = subBefore(typeText, "(", false);
        }

        public boolean isNumber() {
            return CharSequenceUtil.startWithIgnoreCase(getType(), "number");
        }

        public boolean isString() {
            return CharSequenceUtil.startWithIgnoreCase(getType(), "string");
        }

        public boolean isEnum() {
            return CharSequenceUtil.startWithIgnoreCase(getType(), "enum");
        }

        public boolean isSwitch() {
            return CharSequenceUtil.startWithIgnoreCase(getType(), "switch");
        }

        public boolean isArray() {
            return ARRAY_PATTERN.matcher(getType()).matches();
        }

    }
}
