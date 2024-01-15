package org.fz.nettyx.util;

import cn.hutool.core.util.TypeUtil;
import java.lang.reflect.Type;
import lombok.NoArgsConstructor;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/15 11:24
 */
@NoArgsConstructor
public abstract class TypeRef<T> implements Type {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    public Type getType() {
        return this.type;
    }

    public String toString() {
        return this.type.toString();
    }
}
