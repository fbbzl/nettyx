package org.fz.nettyx.serializer.xml.element;

import lombok.Data;
import org.dom4j.Element;
import org.fz.nettyx.serializer.xml.XmlUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class Model {

    private String namespace;
    private String name;
    private List<Prop> props;

    public Model(Element modelEl) {
        this.namespace = XmlUtils.attrValue(modelEl.getDocument().getRootElement(), NAMESPACE);
        this.name = XmlUtils.name(modelEl);
        this.props = XmlUtils.elements(modelEl).stream().map(Prop::new).collect(toList());
    }
}
