package org.fz.nettyx.serializer.xml.element;

import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static cn.hutool.core.text.CharSequenceUtil.subBefore;
import static cn.hutool.core.text.CharSequenceUtil.subBetween;
import static org.fz.nettyx.serializer.xml.XmlUtils.attrValue;
import static org.fz.nettyx.serializer.xml.XmlUtils.name;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_HANDLER;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_LENGTH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_ORDER;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_TYPE;
import static org.fz.nettyx.util.BytesKit.LittleEndian.LE;

import cn.hutool.core.text.CharSequenceUtil;
import java.util.regex.Pattern;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.util.BytesKit;
import org.fz.nettyx.util.BytesKit.Endian;


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
    private final String handler;

    public Prop(Element propEl) {
        this.name = name(propEl);
        this.offset = Integer.parseInt(attrValue(propEl, ATTR_OFFSET));
        this.length = Integer.parseInt(attrValue(propEl, ATTR_LENGTH));
        this.endianKit = LE.equals(attrValue(propEl, ATTR_ORDER)) ? BytesKit.le : BytesKit.be;
        this.type = new PropType(attrValue(propEl, ATTR_TYPE));
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
        private boolean isArray;
        private int arrayLength;

        public PropType(String typeText) {
            this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");
            this.isArray = ARRAY_PATTERN.matcher(typeText).matches();

            if (this.isArray) {
                this.arrayLength = Integer.parseInt(subBetween(typeText, "[", "]"));
                if (CharSequenceUtil.contains(typeText, "(")) {
                    this.value = subBefore(typeText, "(", false);
                }
                else {
                    this.value = subBefore(typeText, "[", false);
                }
            } else {
                this.value = subBefore(typeText, "(", false);
            }
        }
    }
}
