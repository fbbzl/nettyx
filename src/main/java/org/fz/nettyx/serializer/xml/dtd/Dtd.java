package org.fz.nettyx.serializer.xml.dtd;

import lombok.experimental.UtilityClass;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:42
 */
@UtilityClass
public class Dtd {

    public static final String NAMESPACE = "namespace", NAMESPACE_SYMBOL = ".";

    public static final String EL_MODEL = "model",
            EL_ENUM = "enum",
            EL_SWITCH = "switch",
            EL_MODEL_MAPPING = "model-mapping";

    public static final String
            ATTR_OFFSET = ":offset",
            ATTR_ORDER = ":order",
            ATTR_LENGTH = ":length",
            ATTR_TYPE = ":type",
            ATTR_HANDLER = ":handler";

}
