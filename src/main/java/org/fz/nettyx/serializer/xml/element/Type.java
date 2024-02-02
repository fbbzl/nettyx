package org.fz.nettyx.serializer.xml.element;

import cn.hutool.core.lang.Pair;
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
    private final String typeValue;

    public Pair<String, String> findNamespaceModelPair(String defaultNamespace) {
        String refValue = getRefValue();
        if (!startWith(refValue, NAMESPACE_SYMBOL)) {
            refValue = defaultNamespace + NAMESPACE_SYMBOL + refValue;
        }

        String namespace = subBefore(refValue, NAMESPACE_SYMBOL, false),
                model = subAfter(refValue, NAMESPACE_SYMBOL, true);

        return Pair.of(namespace, model);
    }

    public Type(String typeText) {


        this.namespace = namespace;
    }

    public Model getAsModel() {
        return null;
    }

    public Model getAsString() {
        return null;
    }

    public Model getAsNumber() {
        return null;
    }

    public Model getAsEnum() {
        return null;
    }

    public boolean containsNamespace() {
        return CharSequenceUtil.contains(typeText, NAMESPACE_SYMBOL);
    }

    public String getRefValue() {
        if (!isTypeRef()) return EMPTY;
        return CharSequenceUtil.subBetween(typeText, "{{", "}}");
    }

    public TypeEnum findTypeEnum() {
        return null;
    }

    public boolean isTypeRef() {
        if (typeText == null) return false;
        return REF_PATTERN.matcher(typeText).matches();
    }

    public enum TypeEnum {

        STRING,
        NUMBER

    }
}
