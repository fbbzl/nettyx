package org.fz.nettyx.util;

import cn.hutool.core.util.TypeUtil;
import java.lang.reflect.Type;
import lombok.Getter;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/12 15:24
 */

@Getter
public abstract class TypeRef<T> implements Type {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    public String toString() {
        return this.type.toString();
    }
}
