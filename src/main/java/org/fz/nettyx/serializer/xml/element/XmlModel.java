package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.xml.XmlUtils;
import org.fz.nettyx.util.EndianKit;

import java.util.List;
import java.util.regex.Pattern;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.XmlUtils.attrValue;
import static org.fz.nettyx.serializer.xml.XmlUtils.name;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.*;
import static org.fz.nettyx.util.EndianKit.LE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class XmlModel {

    private String namespace;
    private String name;
    private List<XmlProp> props;

    public XmlModel(Element modelEl) {
        this.namespace = XmlUtils.attrValue(modelEl.getDocument().getRootElement(), NAMESPACE);
        this.name = XmlUtils.name(modelEl);
        this.props = XmlUtils.elements(modelEl).stream().map(XmlProp::new).collect(toList());
    }

    @Data
    public static class XmlProp {

        private static final EndianKit DEFAULT_ENDIAN = EndianKit.BE;

        private final String name;
        private final String text;
        private final int offset;
        private final int length;
        private final PropType type;
        private final EndianKit endianKit;
        private final String handlerQName;

        public XmlProp(Element propEl) {
            this.name = name(propEl);
            this.text = XmlUtils.textTrim(propEl);
            this.offset = Integer.parseInt(attrValue(propEl, ATTR_OFFSET));
            this.length = Integer.parseInt(attrValue(propEl, ATTR_LENGTH));

            String typeText = attrValue(propEl, ATTR_TYPE);
            this.type = XmlUtils.isArrayProp(propEl) ? new PropType(typeText, attrValue(propEl, ATTR_ARRAY_LENGTH)) : new PropType(typeText);

            this.endianKit = LE.name().equalsIgnoreCase(attrValue(propEl, ATTR_ORDER)) ? EndianKit.LE : DEFAULT_ENDIAN;
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

            if (endianKit == LE) {
                el.setAttribute(ATTR_ORDER, LE.name());
            }
            if (CharSequenceUtil.isNotBlank(getText())) {
                el.setText(getText());
            }

            return el;
        }

        //**************************************           private start              ************************************//

        //**************************************           private end                ************************************//

        @Data
        public static class PropType {

            public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");
            public static final Pattern TYPE_ARGS_PATTERN = Pattern.compile("^(.+)\\(.+\\)$");

            private final String typeText;
            private final String value;
            private final String[] typeArgs;
            private boolean isArray;
            private int arrayLength;

            public PropType(String typeText, String arrayLength) {
                this.typeText = typeText;
                this.value = subBefore(typeText, "(", false);
                this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");

                this.isArray = true;
                this.arrayLength = Integer.parseInt(arrayLength);
            }

            public PropType(String typeText) {
                this.typeText = typeText;
                this.value = subBefore(typeText, "(", false);
                this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");
            }

            @Override
            public String toString() {
                return typeText;
            }
        }
    }
}
