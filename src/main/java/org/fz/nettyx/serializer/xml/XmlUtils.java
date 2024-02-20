package org.fz.nettyx.serializer.xml;

import lombok.experimental.UtilityClass;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.fz.nettyx.serializer.xml.element.Model.PropElement;

import java.util.List;
import java.util.function.Predicate;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.text.CharSequenceUtil.subBefore;
import static java.util.Collections.emptyList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/25 16:54
 */

@UtilityClass
public class XmlUtils {

    public static String arrayElementName(PropElement prop) {
        return subBefore(prop.getName(), "-array", true);
    }

    /**
     * You can use namespace to make calls across XML elements
     */
    public static String namespace(Element root) {
        return XmlUtils.attrValue(root, NAMESPACE);
    }

    public static List<Node> content(Element element) {
        if (element == null) {
            return emptyList();
        }

        return element.content();
    }

    public static String name(Element element) {
        if (element == null) {
            return EMPTY;
        }
        return element.getName();
    }

    public static String text(Element element) {
        if (element == null) {
            return EMPTY;
        }
        return element.getText();
    }

    public static String textTrim(Element element) {
        if (element == null) {
            return EMPTY;
        }
        return element.getTextTrim();
    }

    public static String attrValue(Element element, String name) {
        Attribute attribute;
        if (element == null || (attribute = element.attribute(name)) == null) {
            return EMPTY;
        }

        return attribute.getValue();
    }

    public static List<Element> elements(Element element, String name, Predicate<Element> filter) {
        if (element == null) {
            return emptyList();
        }
        List<Element> elements = element.elements(name);
        elements.removeIf(filter.negate());
        return elements;
    }

    public static List<Element> elements(Element element, String name) {
        if (element == null) {
            return emptyList();
        }
        return element.elements(name);
    }

    public static List<Element> elements(Element element) {
        if (element == null) {
            return emptyList();
        }
        return element.elements();
    }

}
