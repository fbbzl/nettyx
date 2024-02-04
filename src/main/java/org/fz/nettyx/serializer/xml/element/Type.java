package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.NAMESPACE_SYMBOL;
import static org.fz.nettyx.serializer.xml.dtd.Dtd.REF_PATTERN;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 23:41
 */

@Data
public class Type {

    private final String namespace;

    /** the type value, if it's a ref it will without {{}} */
    private final String typeValue;

    public Type(String namespace, String typeText) {
        if (isTypeRef(typeText)) {
            typeText = CharSequenceUtil.subBetween(typeText, "{{", "}}");

            // use namespace ref
            if (contains(typeText, NAMESPACE_SYMBOL)) {
                this.namespace = subBefore(typeText, NAMESPACE_SYMBOL, false);
                this.typeValue = subAfter(typeText, NAMESPACE_SYMBOL, true);
            } else {
                // use own namespace
                this.namespace = namespace;
                this.typeValue = typeText;
            }
        } else {
            // basic type
            this.namespace = null;
            this.typeValue = typeText;
        }
    }

    public Model getAsModel() {

        return null;
    }

    public String getAsString() {
        return null;
    }

    public Number getAsNumber() {
        return null;
    }

    public <E extends Enum<E>> E getAsEnum() {
        return null;
    }

    public boolean isTypeRef(String typeText) {
        if (typeText == null) {
            return false;
        }
        return REF_PATTERN.matcher(typeText).matches();
    }

    public enum TypeEnum {

        STRING, NUMBER

    }
}