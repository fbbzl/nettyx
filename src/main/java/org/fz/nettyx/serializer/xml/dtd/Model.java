package org.fz.nettyx.serializer.xml.dtd;

import static cn.hutool.core.text.CharSequenceUtil.endWithIgnoreCase;
import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static cn.hutool.core.text.CharSequenceUtil.subBefore;
import static cn.hutool.core.text.CharSequenceUtil.subBetween;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_ARRAY_LENGTH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_HANDLER;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_LENGTH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_ORDER;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_TYPE;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;
import static org.fz.nettyx.util.EndianKit.LE;

import java.util.List;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.experimental.Delegate;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.fz.nettyx.serializer.xml.XmlUtils;
import org.fz.nettyx.util.EndianKit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class Model {

    private final String namespace;
    private final String name;
    private final List<Prop> props;

    public Model(Element modelEl) {
        this.namespace = XmlUtils.attrValue(modelEl.getDocument().getRootElement(), NAMESPACE);
        this.name = XmlUtils.name(modelEl);
        this.props = XmlUtils.elements(modelEl).stream().map(Prop::new).collect(toList());
    }

    public static class Prop {

        @Delegate
        private final Element propEl;

        public Prop(Element propEl) {
            this.propEl = propEl;
        }

        public int getOffset() {
            return Integer.parseInt(propEl.attributeValue(ATTR_OFFSET));
        }

        public int getLength() {
            return Integer.parseInt(propEl.attributeValue(ATTR_LENGTH));
        }

        public PropType getType() {
            return new PropType(propEl.attributeValue(ATTR_TYPE));
        }

        public int getArrayLength() {
            return Integer.parseInt(propEl.attributeValue(ATTR_ARRAY_LENGTH));
        }

        public EndianKit getEndianKit() {
            return LE.name().equalsIgnoreCase(propEl.attributeValue(ATTR_ORDER)) ? EndianKit.LE : EndianKit.BE;
        }

        public boolean isArray() {
            return endWithIgnoreCase(propEl.getName(), "-array") && getArrayLength() != 0;
        }

        public boolean useHandler() {
            return propEl.attribute(ATTR_HANDLER) != null;
        }

        public String getHandlerQName() {
            return propEl.attribute(ATTR_HANDLER).getValue();
        }

        public List<Prop> propElements() {
            return propEl.elements().stream().map(Prop::new).collect(toList());
        }

        public Element copy() {
            Element copy = new DOMElement(propEl.getName());

            copy.appendAttributes(propEl);
            copy.appendContent(propEl);
            copy.setParent(propEl.getParent());
            copy.setDocument(propEl.getDocument());
            copy.setData(propEl.getData());

            return copy;
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
