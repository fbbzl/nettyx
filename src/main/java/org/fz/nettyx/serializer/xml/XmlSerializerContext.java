package org.fz.nettyx.serializer.xml;

import cn.hutool.core.text.CharSequenceUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.element.Model;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static org.fz.nettyx.serializer.xml.Dtd.*;
import static org.fz.nettyx.serializer.xml.XmlUtils.putConst;

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
    private static final Map<Namespace, Map<String, Model>> MODEL_MAPPINGS = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> ENUMS = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> SWITCHES = new ConcurrentHashMap<>();
    /**
     * first key is namespace, second key is model-ref, the value is model
     */
    private static final Map<Namespace, Map<String, Model>> MODELS = new ConcurrentHashMap<>();

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
        for (Path path : this.paths) {
            Document doc;
            try {
                doc = reader.read(path.toFile());
            } catch (DocumentException exception) {
                throw new IllegalArgumentException("can not read [" + path + "]", exception);
            }

            Element root = doc.getRootElement();

            scanEnum(root);
            scanSwitch(root);
            scanModel(root);
            scanMapping(root);
            System.err.println();
        }
    }

    static void scanEnum(Element rootElement) {
        putConst(rootElement, EL_ENUMS, EL_ENUM, ENUMS);
    }

    static void scanSwitch(Element rootElement) {
        putConst(rootElement, EL_SWITCHES, EL_SWITCH, SWITCHES);
    }

    static void scanModel(Element rootElement) {
        Element models = rootElement.element(EL_MODELS);
        Namespace namespace = rootElement.getNamespace();

        Map<String, Model> modelMap = XmlUtils.elements(models, EL_MODEL).stream()
                .map(el -> new Model(el, namespace))
                .collect(toMap(Model::getRef, identity()));

        MODELS.putIfAbsent(namespace, modelMap);
    }

    static void scanMapping(Element rootElement) {
        Element mappings = rootElement.element(EL_MODEL_MAPPINGS);
        if (mappings == null) return;

        for (Element mapping : mappings.elements(EL_MODEL_MAPPING)) {
            String value = XmlUtils.attrValue(mapping, ATTR_VALUE);
            String typeRef = XmlUtils.attrValue(mapping, ATTR_TYPE);

            Model model = findModel(rootElement, typeRef);

        }
    }

    //************************************          private start            *****************************************//

    private static Model findModel(Element rootElement, String typeRef) {
        String nameSpace = CharSequenceUtil.blankToDefault(XmlUtils.findNameSpace(typeRef), XmlUtils.attrValue(rootElement, NAMESPACE));

        return null;
    }



    //************************************           private end             *****************************************//

}
