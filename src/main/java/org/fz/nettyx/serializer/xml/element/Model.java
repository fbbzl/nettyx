package org.fz.nettyx.serializer.xml.element;

import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_OFFSET_TYPE;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_REF;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.EL_PROP;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;
import static org.fz.nettyx.serializer.xml.element.Model.OffsetType.RELATIVE;

import cn.hutool.core.util.EnumUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.dom4j.Element;
import org.fz.nettyx.serializer.xml.XmlUtils;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class Model {

    private static final OffsetType DEFAULT_OFFSET_TYPE = OffsetType.ABSOLUTE;

    private String name;
    private String namespace;
    private String ref;
    private OffsetType offsetType;
    private List<Prop> props;

    public Model(Element modelEl) {
        this.namespace = XmlUtils.attrValue(modelEl.getDocument().getRootElement(), NAMESPACE);
        this.ref = XmlUtils.attrValue(modelEl, ATTR_REF);
        this.offsetType = EnumUtil.fromString(OffsetType.class,
                XmlUtils.attrValue(modelEl, ATTR_OFFSET_TYPE).toUpperCase(), RELATIVE);
        this.props = XmlUtils.elements(modelEl, EL_PROP).stream().map(Prop::new).collect(Collectors.toList());
    }

    public enum OffsetType {
        RELATIVE, ABSOLUTE,
        ;
    }

    @Data
    @AllArgsConstructor
    public static class Counter {
        // todo 相对绝对
        private int index;
        private OffsetType offsetType;
    }

}
