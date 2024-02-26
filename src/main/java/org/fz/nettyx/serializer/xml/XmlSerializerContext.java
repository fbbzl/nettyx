package org.fz.nettyx.serializer.xml;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model.Prop;
import org.fz.nettyx.serializer.xml.XmlSerializerContext.Model.PropType;
import org.fz.nettyx.serializer.xml.handler.PropTypeHandler;
import org.fz.nettyx.util.EndianKit;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.*;
import static org.fz.nettyx.util.EndianKit.LE;

/**
 * application must config this
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 11:16
 */
@Getter
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
    private static final Map<String, Map<String, Model>> MODELS = new SafeConcurrentHashMap<>(256);

    private static final Map<String, PropTypeHandler> PROP_TYPE_CONVERTERS = new SafeConcurrentHashMap<>(64);

    private final Path[] paths;

    public XmlSerializerContext(File... files) {
        this(Arrays.stream(files).map(File::toPath).toArray(Path[]::new));
    }

    public XmlSerializerContext(Path... paths) {
        this.paths = paths;
        this.doScan();
    }

    public synchronized void doScan() {
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

        scanTypeHandlers();
    }

    //************************************          protected start            *****************************************//

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

        Map<String, String[]> switches = new HashMap<>(64);
        for (Element el : switchEl.elements()) {
            switches.put(XmlUtils.name(el),
                         Arrays.stream(splitToArray(XmlUtils.textTrim(el), ","))
                               .map(CharSequenceUtil::removeAllLineBreaks)
                               .map(CharSequenceUtil::cleanBlank).toArray(String[]::new));
        }

        SWITCHES.putAll(switches);
    }

    protected void scanModels(Element rootElement) {
        Element models = rootElement.element(EL_MODEL);

        Map<String, Model> modelMap = new LinkedHashMap<>(16);
        XmlUtils.elements(models).stream().map(Model::new).forEach(m -> modelMap.put(m.getName(), m));

        MODELS.putIfAbsent(XmlUtils.attrValue(rootElement, NAMESPACE), modelMap);
    }

    protected void scanTypeHandlers() {
        Set<Class<?>> handlerClasses = ClassScanner.scanPackageBySuper(EMPTY, PropTypeHandler.class);

        Map<String, PropTypeHandler> handlers =
                handlerClasses.stream()
                              .filter(ClassUtil::isNormalClass)
                              .map(hc -> (PropTypeHandler) Singleton.get(hc))
                              .collect(Collectors.toMap(PropTypeHandler::forType, Function.identity()));

        PROP_TYPE_CONVERTERS.putAll(handlers);
    }

    //************************************           protected end             *****************************************//

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
        return PROP_TYPE_CONVERTERS.containsKey(typeValue);
    }

    public static PropTypeHandler getTypeHandler(String typeValue) {
        return PROP_TYPE_CONVERTERS.get(typeValue);
    }

    @Data
    public static class Model {

        private final String namespace;
        private final String name;
        private final List<Prop> props;

        public Model(Element modelEl) {
            this.namespace = XmlUtils.attrValue(modelEl.getDocument().getRootElement(), NAMESPACE);
            this.name = XmlUtils.name(modelEl);
            this.props = XmlUtils.elements(modelEl).stream().map(Prop::new).collect(toList());
        }

        public static class Prop {

            @Delegate
            private final Element propEl;

            public Prop(Element propEl) {
                this.propEl = propEl;
            }

            public int getOffset() {
                return Integer.parseInt(propEl.attributeValue(ATTR_OFFSET));
            }

            public int getLength() {
                return Integer.parseInt(propEl.attributeValue(ATTR_LENGTH));
            }

            public PropType getType() {
                return new PropType(propEl.attributeValue(ATTR_TYPE));
            }

            public int getArrayLength() {
                return Integer.parseInt(propEl.attributeValue(ATTR_ARRAY_LENGTH));
            }

            public EndianKit getEndianKit() {
                return LE.name().equalsIgnoreCase(propEl.attributeValue(ATTR_ORDER)) ? EndianKit.LE : EndianKit.BE;
            }

            public boolean isArray() {
                return endWithIgnoreCase(propEl.getName(), "-array") && getArrayLength() != 0;
            }

            public boolean hasExpression() {
                return propEl.attribute(ATTR_EXP) != null;
            }

            public String getExpression() {
                return propEl.attribute(ATTR_EXP).getValue();
            }

            public boolean hasHandler() {
                return propEl.attribute(ATTR_HANDLER) != null;
            }

            public String getHandlerQName() {
                return propEl.attribute(ATTR_HANDLER).getValue();
            }

            public List<Prop> propElements() {
                return propEl.elements().stream().map(Prop::new).collect(toList());
            }

            public Element copy() {
                Element copy = new DOMElement(propEl.getName());

                copy.appendAttributes(propEl);
                copy.appendContent(propEl);
                copy.setParent(propEl.getParent());
                copy.setDocument(propEl.getDocument());
                copy.setData(propEl.getData());

                return copy;
            }


        }

        @Data
        public static class PropType {

            public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");
            public static final Pattern TYPE_ARGS_PATTERN = Pattern.compile("^(.+)\\(.+\\)$");

            private final String typeText;
            private final String value;
            private final String[] typeArgs;

            public PropType(String typeText) {
                this.typeText = typeText;
                this.value = subBefore(typeText, "(", false);
                this.typeArgs = splitToArray(subBetween(typeText, "(", ")"), ",");
            }

            @Override
            public String toString() {
                return typeText;
            }
        }

        //**************************************           public end              ************************************//
    }
}
