package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.xml.XmlUtils;
import org.fz.nettyx.util.BytesKit;
import org.fz.nettyx.util.BytesKit.Endian;
import org.fz.nettyx.util.BytesKit.LittleEndian;

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

    private static final Endian DEFAULT_ENDIAN = BytesKit.be;

    private final String name;
    private final String text;
    private final int offset;
    private final int length;
    private final PropType type;
    private final Endian endianKit;
    private final String handlerQName;

    public Prop(Element propEl) {
        this.name = name(propEl);
        this.text = XmlUtils.textTrim(propEl);
        this.offset = Integer.parseInt(attrValue(propEl, ATTR_OFFSET));
        this.length = Integer.parseInt(attrValue(propEl, ATTR_LENGTH));
        this.type = new PropType(attrValue(propEl, ATTR_TYPE));
        this.endianKit = LE.equals(attrValue(propEl, ATTR_ORDER)) ? BytesKit.le : DEFAULT_ENDIAN;
        this.handlerQName = attrValue(propEl, ATTR_HANDLER);
    }

    public boolean useHandler() {
        return CharSequenceUtil.isNotBlank(getHandlerQName());
    }

    public Element toElement() {
        DOMElement el = new DOMElement(getName());
        el.setAttribute(ATTR_OFFSET, String.valueOf(getOffset()));
        el.setAttribute(ATTR_LENGTH, String.valueOf(getLength()));
        el.setAttribute(ATTR_TYPE, getType().toString());

        if(endianKit instanceof LittleEndian) {
            el.setAttribute(ATTR_ORDER, LE);
        }
        if(CharSequenceUtil.isNotBlank(getText())) {
            el.setText(getText());
        }

        return el;
    }

    //**************************************           private start              ************************************//

    //**************************************           private end                ************************************//

    @Data
    public static class PropType {

        public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");
        public static final Pattern ARRAY_PATTERN = Pattern.compile("^(.+)\\[\\d+]$");
        public static final Pattern TYPE_ARGS_PATTERN = Pattern.compile("^(.+)\\(.+\\)$");

        private final String typeText;
        private final String[] typeArgs;
        private final String value;
        private boolean isArray;
        private int arrayLength;

        public PropType(String typeText) {
            this.typeText = typeText;
            this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");
            this.isArray = ARRAY_PATTERN.matcher(typeText).matches();

            if (this.isArray) {
                this.arrayLength = Integer.parseInt(subBetween(typeText, "[", "]"));
                if (CharSequenceUtil.contains(typeText, "(")) {
                    this.value = subBefore(typeText, "(", false);
                } else {
                    this.value = subBefore(typeText, "[", false);
                }
            } else {
                this.value = subBefore(typeText, "(", false);
            }
        }

        @Override
        public String toString() {
            return typeText;
        }
    }
}
