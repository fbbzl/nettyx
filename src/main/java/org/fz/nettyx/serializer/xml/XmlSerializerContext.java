package org.fz.nettyx.serializer.xml;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 11:16
 */
public class XmlSerializerContext {

    private static final Map<String, Set<String>> ENUMS = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> SWITCHES = new ConcurrentHashMap<>();
    private String[] path;

    public XmlSerializerContext(String... paths) {
        // invoke scan
    }

    public static class XmlScanner {

        void scanEnum() {

        }

        void scanSwitch() {

        }

        void scanModel() {

        }

        void scanMapping() {
        }

    }
}
