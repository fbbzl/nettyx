package org.fz.nettyx.serializer.xml;

import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.text.CharSequenceUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Type;
import org.fz.nettyx.util.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.fz.nettyx.serializer.xml.XmlUtils.putConst;
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
     * first key is namespace, second key is target-value, the value is model
     */
    private static final Map<String, Map<String, Model>> MODEL_MAPPINGS = new SafeConcurrentHashMap<>(64);
    private static final Map<String, Set<String>> ENUMS = new SafeConcurrentHashMap<>(64);
    private static final Map<String, Set<String>> SWITCHES = new SafeConcurrentHashMap<>(64);
    private static final Map<String, Document> NAMESPACES_DOCS = new SafeConcurrentHashMap<>(64);

    /**
     * first key is namespace, second key is model-ref, the value is model
     */
    private static final Map<String, Map<String, Model>> MODELS = new SafeConcurrentHashMap<>(64);

    private final Path[] paths;

    public XmlSerializerContext(File... files) {
        this.paths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
        this.refresh();
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
        putConst(rootElement, EL_ENUMS, EL_ENUM, ENUMS);
    }

    private static void scanSwitches(Element rootElement) {
        putConst(rootElement, EL_SWITCHES, EL_SWITCH, SWITCHES);
    }

    private static void scanModels(Element rootElement) {
        Element models = rootElement.element(EL_MODELS);
        String namespace = XmlUtils.attrValue(rootElement, NAMESPACE);

        Map<String, Model> modelMap = XmlUtils.elements(models, EL_MODEL).stream().map(el -> new Model(el, namespace))
            .collect(toMap(Model::getRef, identity()));

        MODELS.putIfAbsent(namespace, modelMap);
    }

    private static void scanMappings(Element rootElement) {
        Element mappings = rootElement.element(EL_MODEL_MAPPINGS);
        if (mappings == null) {
            return;
        }

        String namespace = XmlUtils.attrValue(rootElement, NAMESPACE);

        Map<String, Model> modelMapping = new HashMap<>(64);
        for (Element mapping : mappings.elements(EL_MODEL_MAPPING)) {
            String targetValue = XmlUtils.attrValue(mapping, ATTR_VALUE), modelRef = CharSequenceUtil.subBetween(
                XmlUtils.textTrim(mapping), "{{", "}}");

            Model model = findModel(namespace, modelRef);

            modelMapping.putIfAbsent(targetValue, model);
        }

        MODEL_MAPPINGS.putIfAbsent(namespace, modelMapping);
    }

    //************************************           private end             *****************************************//

    //************************************          public start            *****************************************//

    public static Model findModel(Type type) {
        return MODELS.getOrDefault(type.getNamespace(), emptyMap()).get(type.getTypeValue());
    }

    public static Model findModel(String namespace, String modelRef) {
        return MODELS.getOrDefault(namespace, emptyMap()).get(modelRef);
    }

    //************************************          public start            *****************************************//

}
