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
            EL_ENUM                     = "enum",
            EL_SWITCH                   = "switch";

    public static final String
            ATTR_OFFSET       = ":offset",
            ATTR_LENGTH       = ":length",
            ATTR_TYPE         = ":type",
            ATTR_ARRAY_LENGTH = ":array-length",
            ATTR_EXP          = ":exp",
            ATTR_ORDER        = ":order",
            ATTR_HANDLER      = ":handler";

}
