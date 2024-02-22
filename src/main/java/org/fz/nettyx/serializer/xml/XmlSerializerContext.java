package org.fz.nettyx.serializer.xml;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.text.CharSequenceUtil.splitToArray;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.EL_ENUM;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.EL_MODEL;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.EL_SWITCH;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ClassUtil;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.dtd.Model;
import org.fz.nettyx.serializer.xml.dtd.Model.Prop;
import org.fz.nettyx.serializer.xml.dtd.Model.Prop.PropType;
import org.fz.nettyx.serializer.xml.handler.PropTypeHandler;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.Try;

/**
 * application must config this
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 11:16
 */
public class XmlSerializerContext {

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

    private static final Map<String, PropTypeHandler> TYPE_CONVERTERS = new SafeConcurrentHashMap<>(16);
    private static final Map<String, PropTypeHandler> PROP_HANDLERS = new SafeConcurrentHashMap<>(16);

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

    protected void scanHandlers() {
        Set<Class<?>> handlerClasses = ClassScanner.scanPackageBySuper(EMPTY, PropTypeHandler.class);
        for (Class<?> handlerClass : handlerClasses) {
            if (!ClassUtil.isNormalClass(handlerClass)) {
                continue;
            }
            PropTypeHandler handler = (PropTypeHandler) Singleton.get(handlerClass);
            String forType = handler.forType();
            if (CharSequenceUtil.isNotBlank(forType)) {
                PROP_HANDLERS.putIfAbsent(forType, handler);
            } else {

            }
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

    public static Model findModel(String namespace, String modelName) {
        return MODELS.getOrDefault(namespace, emptyMap()).get(modelName);
    }

    public static boolean containsType(String typeValue) {
        return PROP_HANDLERS.containsKey(typeValue);
    }

    public static PropTypeHandler getHandler(String typeValue) {
        return PROP_HANDLERS.get(typeValue);
    }

    //************************************          public start            *****************************************//

}
