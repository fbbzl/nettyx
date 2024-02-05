package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;

import java.util.regex.Pattern;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 23:41
 */

@Data
public class Type {

    public static final Pattern MODEL_REF_PATTERN = Pattern.compile("^\\{\\{(\\S+)}}$");

    public static final Pattern ARRAY_PATTERN = Pattern.compile("^(.+)\\[\\d+]$");

    /**
     * the type value, if it's a ref it will without {{}}
     */
    private final String typeText;

    public Type(  String typeText) {
        this.typeText = typeText;
    }

    public boolean isString() {
        // TODO 根据字符前面的编码集进行判断
        return CharSequenceUtil.endWithIgnoreCase(typeText, "string");
    }

    public boolean isNumber() {
        // TODO 根据length和小数 字符规则进行判断
        return CharSequenceUtil.startWithIgnoreCase(typeText, "number");
    }

    public boolean isModel() {
        return MODEL_REF_PATTERN.matcher(typeText).matches();
    }

    public boolean isArray() {
        return ARRAY_PATTERN.matcher(typeText).matches();
    }

}
