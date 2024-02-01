package org.fz.nettyx.serializer.xml;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:42
 */
@UtilityClass
public class Dtd {

    public static final String NAMESPACE = "namespace", NAMESPACE_SYMBOL = ".";

    public static final Pattern REF_PATTERN = Pattern.compile("^\\{\\{(.*)}}$");

    public static final String
            EL_MODELS = "models",
            EL_MODEL = "model",
            EL_PROP = "prop",
            EL_ENUMS = "enums",
            EL_ENUM = "enum",
            EL_SWITCHES = "switches",
            EL_SWITCH = "switch",
            EL_MODEL_MAPPINGS = "model-mappings",
            EL_MODEL_MAPPING = "model-mapping";


    public static final String
            ATTR_REF = "ref",
            ATTR_VALUE ="value",
            ATTR_NAME = "name",
            ATTR_OFFSET = "offset",
            ATTR_SIZE = "size",
            ATTR_TYPE = "type",
            ATTR_EXP = "exp",
            ATTR_COUNTER_TYPE = "counter-type",
            ATTR_HANDLER = "handler";

}
