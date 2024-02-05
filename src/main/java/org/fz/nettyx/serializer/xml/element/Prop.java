package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.dom4j.Element;
import org.fz.nettyx.serializer.xml.XmlUtils;

import java.util.regex.Pattern;

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
    private final Type type;
    private final String exp;
    private final String handler;

    public Prop(Element propEl) {
        this.name = XmlUtils.name(propEl);
        this.offset = Integer.parseInt(XmlUtils.attrValue(propEl, ATTR_OFFSET));
        this.length = Integer.parseInt(XmlUtils.attrValue(propEl, ATTR_LENGTH));
        this.type = new Type(XmlUtils.attrValue(propEl, ATTR_TYPE));
        this.exp = XmlUtils.attrValue(propEl, ATTR_EXP);
        this.handler = XmlUtils.attrValue(propEl, ATTR_HANDLER);
    }

    public boolean useHandler() {
        return CharSequenceUtil.isNotBlank(getHandler());
    }

    //**************************************           private start              ************************************//

    //**************************************           private end                ************************************//

    @Data
    public static class Type {

        public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");

        public static final Pattern ARRAY_PATTERN = Pattern.compile("^(.+)\\[\\d+]$");

        private final String typeText;

        public boolean isNumber() {
            return CharSequenceUtil.startWithIgnoreCase(getTypeText(), "number");
        }

        public boolean isString() {
            return CharSequenceUtil.startWithIgnoreCase(getTypeText(), "string");
        }

        public boolean isEnum() {
            return CharSequenceUtil.startWithIgnoreCase(getTypeText(), "enum");
        }

        public boolean isSwitch() {
            return CharSequenceUtil.startWithIgnoreCase(getTypeText(), "switch");
        }

        public boolean isArray() {
            return ARRAY_PATTERN.matcher(getTypeText()).matches();
        }

    }
}
