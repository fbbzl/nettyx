package org.fz.nettyx.serializer.xml;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static java.util.stream.Collectors.toCollection;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.ATTR_REF;

import cn.hutool.core.text.CharSequenceUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.dom4j.Attribute;
import org.dom4j.Element;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/25 16:54
 */

@UtilityClass
public class XmlUtils {

    public static String name(Element element) {
        if (element == null) {
            return EMPTY;
        }
        return element.getName();
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
            return Collections.emptyList();
        }
        List<Element> elements = element.elements(name);
        elements.removeIf(filter.negate());
        return elements;
    }

    public static List<Element> elements(Element element, String name) {
        if (element == null) {
            return Collections.emptyList();
        }
        return element.elements(name);
    }

    public static void putConst(Element rootElement, String boundary, String name, Map<String, Set<String>> map) {
        for (Iterator<Element> it = rootElement.elementIterator(boundary); it.hasNext(); ) {
            for (Element enumEl : it.next().elements(name)) {
                if (enumEl == null) {
                    continue;
                }

                String enumRef = enumEl.attribute(ATTR_REF).getValue();

                map.putIfAbsent(enumRef,
                    Arrays.stream(splitToArray(enumEl.getTextTrim(), ",")).map(CharSequenceUtil::removeAllLineBreaks)
                        .map(CharSequenceUtil::cleanBlank).collect(toCollection(LinkedHashSet::new)));
            }
        }
    }


}
