package org.fz.nettyx.serializer.xml.element;

import static org.fz.nettyx.serializer.xml.Dtd.ATTR_COUNTER_TYPE;
import static org.fz.nettyx.serializer.xml.Dtd.ATTR_REF;
import static org.fz.nettyx.serializer.xml.Dtd.EL_PROP;
import static org.fz.nettyx.serializer.xml.element.Model.OffsetType.RELATIVE;

import cn.hutool.core.util.EnumUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.fz.nettyx.serializer.xml.XmlUtils;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class Model {

    private static final OffsetType DEFAULT_OFFSET_TYPE = RELATIVE;

    private Namespace namespace;
    private String ref;
    private OffsetType offsetType;
    private List<Prop> props;

    public Model(Element modelEl, Namespace namespace) {
        this(modelEl);
        this.namespace = namespace;
    }

    public Model(Element modelEl) {
        this.ref = XmlUtils.attrValue(modelEl, ATTR_REF);
        this.offsetType = EnumUtil.fromString(OffsetType.class,
            XmlUtils.attrValue(modelEl, ATTR_COUNTER_TYPE).toUpperCase(), RELATIVE);
        this.props = XmlUtils.elements(modelEl, EL_PROP).stream().map(Prop::new).collect(Collectors.toList());
    }

    public enum OffsetType {
        RELATIVE, ABSOLUTE,
        ;
    }
}
