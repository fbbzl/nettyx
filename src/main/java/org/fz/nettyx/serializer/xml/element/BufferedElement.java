package org.fz.nettyx.serializer.xml.element;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/5 23:07
 */
public class BufferedElement extends DOMElement {

    public BufferedElement(String name) {
        super(name);
    }

    public BufferedElement(QName qname) {
        super(qname);
    }

    public BufferedElement(QName qname, int attributeCount) {
        super(qname, attributeCount);
    }

    public BufferedElement(String name, Namespace namespace) {
        super(name, namespace);
    }
}
