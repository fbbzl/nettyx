package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/15 11:24
 */

@Getter
@SuppressWarnings("unchecked")
@NoArgsConstructor
public abstract class TypeRefer<T> implements Type {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    public String toString() {
        return this.type.toString();
    }

    public static <T> Class<T> getFieldActualType(Type rootType, Field field) {
        return getActualType(rootType, TypeUtil.getType(field));
    }

    public static <T> Class<T> getActualType(Type root, Type type) {
        if (!(root instanceof ParameterizedType) || type instanceof Class || type instanceof WildcardType)
            return (Class<T>) type;


        if (type instanceof TypeVariable) return getActualType(root, TypeUtil.getActualType(root, type));
        if (type instanceof ParameterizedType) {
            Type actualType = TypeUtil.getActualType(root, type);
            Type[] actualTypeArguments = ((ParameterizedType) actualType).getActualTypeArguments();
            if (actualTypeArguments.length == 0) {
                return (Class<T>) Object.class;
            }
            return getActualType(root, actualTypeArguments[0]);
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) TypeUtil.getActualType(root, type);
            return getActualType(root, TypeUtil.getActualType(root, genericArrayType.getGenericComponentType()));
        }

        return (Class<T>) Object.class;
    }
}
