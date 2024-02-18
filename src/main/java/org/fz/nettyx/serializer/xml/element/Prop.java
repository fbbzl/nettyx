package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.xml.converter.NumberConverter;
import org.fz.nettyx.util.BytesKit;
import org.fz.nettyx.util.BytesKit.Endian;

import java.util.regex.Pattern;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static org.fz.nettyx.serializer.xml.XmlUtils.attrValue;
import static org.fz.nettyx.serializer.xml.XmlUtils.name;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.*;
import static org.fz.nettyx.util.BytesKit.LittleEndian.LE;


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
        private final String value;
        /**
         * if is not array, length will be null
         */
        private Integer arrayLength;

        public PropType(String typeText) {
            this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");
            this.value = subBefore(typeText, "(", false);
            if (ARRAY_PATTERN.matcher(typeText).matches()) {
                this.arrayLength = Integer.parseInt(subBetween(typeText, "[", "]"));
            }
        }

        public static boolean isNumber(String value) {
            return NumberConverter.convertible(value) && !isArray();
        }

        public static boolean isString(String value) {
            return CharSequenceUtil.startWithIgnoreCase(value, "string") && !isArray();
        }

        public static boolean isEnum(String value) {
            return CharSequenceUtil.startWithIgnoreCase(value, "enum") && !isArray();
        }

        public static boolean isSwitch(String value) {
            return CharSequenceUtil.startWithIgnoreCase(value, "switch") && !isArray();
        }

        public static boolean isArray(String value) {
            return CharSequenceUtil.startWithIgnoreCase(value, "switch") && !isArray();
        }


    }
}
