package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;

import static org.fz.nettyx.serializer.xml.dtd.Dtd.MODEL_REF_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 23:41
 */

@Data
public class Type {

    /**
     *
     */
    private final String namespace;

    /**
     * the type value, if it's a ref it will without {{}}
     */
    private final String typeText;

    public Type(String namespace, String typeText) {
//        if (isTypeRef(typeText)) {
//            typeText = CharSequenceUtil.subBetween(typeText, "{{", "}}");
//
//            // use namespace ref
//            if (contains(typeText, NAMESPACE_SYMBOL)) {
//                this.namespace = subBefore(typeText, NAMESPACE_SYMBOL, false);
//                this.typeText = subAfter(typeText, NAMESPACE_SYMBOL, true);
//            } else {
//                // use own namespace
//                this.namespace = namespace;
//                this.typeText = typeText;
//            }
//        } else {
//            // basic type
//
//        }
        this.namespace = null;
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
        return false;
    }



}
