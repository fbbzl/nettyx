package org.fz.nettyx.serializer.xml;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:42
 */
@UtilityClass
public class Dtd {

    public static final String NAMESPACE = "namespace", NAMESPACE_SYMBOL = ".";

    public static final Pattern REF_PATTERN = Pattern.compile("^\\{\\{(.*)}}$");

    public static void main(String[] args) {
        String x = "{{namespace.user}}";
        Matcher matcher = REF_PATTERN.matcher(x);
        if (matcher.find()) {
            System.err.println(matcher.group());
        }
    }

    public static String getRefValue(String text) {
        if (!isRefString(text)) return EMPTY;
        return CharSequenceUtil.subBetween(text, "{{", "}}");
    }

    public static boolean isRefString(String text) {
        if (text == null) return false;
        return REF_PATTERN.matcher(text).matches();
    }

    public static final String
            EL_MODELS = " models",
            EL_MODEL = " model",
            EL_PROP = " prop",
            EL_ENUMS = " enums",
            EL_ENUM = " enum",
            EL_SWITCHES = " switches",
            EL_SWITCH = " switch",
            EL_MAPPINGS = " mappings",
            EL_MAPPING = " mapping";


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
