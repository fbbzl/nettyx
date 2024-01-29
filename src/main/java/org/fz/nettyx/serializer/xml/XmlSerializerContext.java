package org.fz.nettyx.serializer.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.fz.nettyx.serializer.xml.element.Prop;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.fz.nettyx.serializer.xml.XmlUtils.putConst;
import static org.fz.nettyx.serializer.xml.element.Prop.ATTR_REF;

/**
 * application must config this
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 11:16
 */
public class XmlSerializerContext {

    private static final Map<String, Set<String>> ENUMS = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> SWITCHES = new ConcurrentHashMap<>();

    /**
     * first key is namespace, second key is model-ref, the value is prop
     */
    private static final Map<String, org.fz.nettyx.serializer.xml.element.Model> MODELS = new ConcurrentHashMap<>();
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
        putConst(rootElement, "enums", "enum", ENUMS);
    }

    static void scanSwitch(Element rootElement) {
        putConst(rootElement, "switches", "switch", SWITCHES);
    }

    static void scanModel(Element rootElement) {
        Element models = rootElement.element("models");

        for (Element model : XmlUtils.elements(models, "model")) {
            String modelRef = XmlUtils.attrValue(model, ATTR_REF);
            for (Element prop : XmlUtils.elements(model, "prop")) {
                Prop propElement = new Prop(prop);

            }
        }
    }

    static void scanMapping(Element rootElement) {
        Element mappings = rootElement.element("mappings");
        if (mappings == null) return;

        for (Element mapping : mappings.elements("mapping")) {

        }

    }


}
