package org.fz.nettyx.serializer.xml;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ClassUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.element.Model;
import org.fz.nettyx.serializer.xml.element.Model.Prop;
import org.fz.nettyx.serializer.xml.element.Model.Prop.PropType;
import org.fz.nettyx.serializer.xml.handler.XmlPropHandler;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
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
     * key is enum name, the value is the enum-string
     */
    private static final Map<String, String[]> ENUMS = new SafeConcurrentHashMap<>(64);
    /**
     * key is switch name, the value is the switch
     */
    private static final Map<String, String[]> SWITCHES = new SafeConcurrentHashMap<>(64);
    /**
     * first key is namespace, second key is model name, the value is model
     */
    private static final Map<String, Map<String, Model>> MODELS = new SafeConcurrentHashMap<>(64);

    private static final Map<String, XmlPropHandler> TYPE_HANDLERS = new SafeConcurrentHashMap<>(16);

    private final Path[] paths;

    public XmlSerializerContext(File... files) {
        this(Arrays.stream(files).map(File::toPath).toArray(Path[]::new));
    }

    public XmlSerializerContext(Path... paths) {
        this.paths = paths;
        this.refresh();
    }

    public synchronized void refresh() {
        SAXReader reader = SAXReader.createDefault();
        // first add the doc mapping
        List<Document> docs = Arrays.stream(this.paths).map(Path::toFile).map(Try.apply(reader::read))
            .collect(toList());

        // scan namespaces
        docs.forEach(this::scanNamespaces);

        for (Document doc : docs) {
            Element root = doc.getRootElement();

            scanEnums(root);
            scanSwitches(root);
            scanModels(root);
            scanMappings(root);
        }

        scanHandlers();
    }

    //************************************          private start            *****************************************//

    protected void scanNamespaces(Document doc) {
        NAMESPACES_DOCS.put(XmlUtils.attrValue(doc.getRootElement(), NAMESPACE), doc);
    }

    protected void scanEnums(Element rootElement) {
        Element enumEl = rootElement.element(EL_ENUM);
        if (enumEl == null) {
            return;
        }

        for (Element el : enumEl.elements()) {
            ENUMS.put(XmlUtils.name(el),
                Arrays.stream(splitToArray(XmlUtils.text(el), ",")).map(CharSequenceUtil::removeAllLineBreaks)
                    .map(CharSequenceUtil::cleanBlank).toArray(String[]::new));
        }
    }

    protected void scanSwitches(Element rootElement) {
        Element switchEl = rootElement.element(EL_SWITCH);
        if (switchEl == null) {
            return;
        }

        for (Element el : switchEl.elements()) {
            SWITCHES.put(XmlUtils.name(el),
                Arrays.stream(splitToArray(XmlUtils.textTrim(el), ",")).map(CharSequenceUtil::removeAllLineBreaks)
                    .map(CharSequenceUtil::cleanBlank).toArray(String[]::new));
        }
    }

    protected void scanModels(Element rootElement) {
        Element models = rootElement.element(EL_MODEL);

        Map<String, Model> modelMap = new LinkedHashMap<>(16);
        XmlUtils.elements(models).stream().map(Model::new).forEach(m -> modelMap.put(m.getName(), m));

        MODELS.putIfAbsent(XmlUtils.attrValue(rootElement, NAMESPACE), modelMap);
    }

    protected void scanMappings(Element rootElement) {
        Element mappings = rootElement.element(EL_MODEL_MAPPING);
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

    protected void scanHandlers() {
        Set<Class<?>> handlerClasses = ClassScanner.scanPackageBySuper(EMPTY, XmlPropHandler.class);
        for (Class<?> handlerClass : handlerClasses) {
            if (!ClassUtil.isNormalClass(handlerClass)) {
                continue;
            }
            XmlPropHandler handler = (XmlPropHandler) Singleton.get(handlerClass);
            TYPE_HANDLERS.putIfAbsent(handler.forType(), handler);
        }
    }

    //************************************           private end             *****************************************//

    //************************************          public start            *****************************************//

    public static String[] findEnum(Prop prop) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();
        Throws.ifTrue(typeArgs.length > 1, "enum [" + type.getValue() + "] do not support 2 type args");

        String enumName = typeArgs[0];

        return findEnum(enumName);
    }

    public static String[] findSwitch(Prop prop) {
        PropType type = prop.getType();
        String[] typeArgs = type.getTypeArgs();
        Throws.ifTrue(typeArgs.length > 1, "switch [" + type.getValue() + "] do not support 2 type args");

        String switchName = typeArgs[0];

        return findSwitch(switchName);
    }

    public static String[] findSwitch(String switchName) {
        return SWITCHES.getOrDefault(switchName, new String[]{});
    }

    public static String[] findEnum(String enumName) {
        return ENUMS.getOrDefault(enumName, new String[]{});
    }

    public static Model findModel(String mappingValue) {
        return MODEL_MAPPINGS.get(mappingValue);
    }

    public static boolean containsType(String typeValue) {
        return TYPE_HANDLERS.containsKey(typeValue);
    }

    public static XmlPropHandler getHandler(String typeValue) {
        return TYPE_HANDLERS.get(typeValue);
    }

    //************************************          public start            *****************************************//

}
