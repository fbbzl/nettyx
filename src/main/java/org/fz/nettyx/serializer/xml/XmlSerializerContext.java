package org.fz.nettyx.serializer.xml;

import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.text.CharSequenceUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.util.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.*;

/**
 * application must config this
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 11:16
 */
public class XmlSerializerContext {

    /**
     * first key is target-value, second key is namespace, the value is model
     */
    private static final Map<String, Model> MODEL_MAPPINGS = new SafeConcurrentHashMap<>(64);
    private static final Map<String, Document> NAMESPACES_DOCS = new SafeConcurrentHashMap<>(64);
    /**
     * first key is namespace, second key is enum name, the value is the enum
     */
    private static final Map<String, Map<String, List<String>>> ENUMS = new SafeConcurrentHashMap<>(64);
    /**
     * first key is namespace, second key is switch name, the value is the switch
     */
    private static final Map<String, Map<String, List<String>>> SWITCHES = new SafeConcurrentHashMap<>(64);
    /**
     * first key is namespace, second key is model name, the value is model
     */
    private static final Map<String, Map<String, Model>> MODELS = new SafeConcurrentHashMap<>(64);

    private final Path[] paths;

    public XmlSerializerContext(File... files) {
        this(Arrays.stream(files).map(File::toPath).toArray(Path[]::new));
    }

    public XmlSerializerContext(Path... paths) {
        this.paths = paths;
        this.refresh();
    }

    public void refresh() {
        SAXReader reader = SAXReader.createDefault();
        // first add the doc mapping
        List<Document> docs = Arrays.stream(this.paths).map(Path::toFile).map(Try.apply(reader::read))
                .collect(toList());

        // first scan namespaces
        docs.forEach(XmlSerializerContext::scanNamespaces);

        for (Document doc : docs) {
            Element root = doc.getRootElement();

            scanEnums(root);
            scanSwitches(root);
            scanModels(root);
            scanMappings(root);
        }
    }

    //************************************          private start            *****************************************//

    private static void scanNamespaces(Document doc) {
        NAMESPACES_DOCS.put(XmlUtils.attrValue(doc.getRootElement(), NAMESPACE), doc);
    }

    private static void scanEnums(Element rootElement) {
        Map<String, List<String>> enums = new HashMap<>(8);
        for (Element el : rootElement.elements(EL_ENUMS)) {
            enums.put(XmlUtils.name(el), Arrays.stream(splitToArray(XmlUtils.textTrim(el), ","))
                    .map(CharSequenceUtil::removeAllLineBreaks)
                    .map(CharSequenceUtil::cleanBlank)
                    .collect(toList()));
        }

        ENUMS.put(XmlUtils.namespace(rootElement), enums);
    }

    private static void scanSwitches(Element rootElement) {
        Map<String, List<String>> switches = new HashMap<>(8);
        for (Element el : rootElement.elements(EL_SWITCHES)) {
            switches.put(XmlUtils.name(el), Arrays.stream(splitToArray(XmlUtils.textTrim(el), ","))
                    .map(CharSequenceUtil::removeAllLineBreaks)
                    .map(CharSequenceUtil::cleanBlank)
                    .collect(toList()));
        }

        SWITCHES.put(XmlUtils.namespace(rootElement), switches);
    }

    private static void scanModels(Element rootElement) {
        Element models = rootElement.element(EL_MODELS);

        Map<String, Model> modelMap = new LinkedHashMap<>(16);
        XmlUtils.elements(models).stream().map(Model::new).forEach(m -> modelMap.put(m.getName(), m));

        MODELS.putIfAbsent(XmlUtils.attrValue(rootElement, NAMESPACE), modelMap);
    }

    private static void scanMappings(Element rootElement) {
        Element mappings = rootElement.element(EL_MODEL_MAPPINGS);
        if (mappings == null) {
            return;
        }

        String namespace = XmlUtils.attrValue(rootElement, NAMESPACE);

        for (Element mapping : mappings.elements()) {
            String mappingValue = XmlUtils.textTrim(mapping);
            Model model = MODELS.getOrDefault(namespace, emptyMap()).get(XmlUtils.name(mapping));

            MODEL_MAPPINGS.putIfAbsent(mappingValue, model);
        }
    }

    //************************************           private end             *****************************************//

    //************************************          public start            *****************************************//

    public static Model findModel(String mappingValue) {
        return MODEL_MAPPINGS.get(mappingValue);
    }

    //************************************          public start            *****************************************//

}
