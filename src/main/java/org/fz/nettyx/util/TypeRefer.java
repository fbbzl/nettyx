package org.fz.nettyx.util;

import cn.hutool.core.util.TypeUtil;
import java.lang.reflect.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/15 11:24
 */

@Getter
@NoArgsConstructor
public abstract class TypeRefer<T> implements Type {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    public String toString() {
        return this.type.toString();
    }
}
