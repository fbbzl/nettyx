package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fz.nettyx.exception.TypeJudgmentException;

import java.lang.reflect.*;

/**
 * type parameter at the time of serialization
 *
 * @param <T> the type parameter
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/15 11:24
 */
@Getter
@SuppressWarnings("unchecked")
@NoArgsConstructor
public abstract class TypeRefer<T> implements Type {

    private final Type type = TypeUtil.getTypeArgument(this.getClass());

    public String toString() {
        return this.type.toString();
    }

    /**
     * Gets raw type.
     *
     * @param <T>  the type parameter
     * @param type the type
     *
     * @return the raw type
     */
    public static <T> Class<T> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        throw new TypeJudgmentException(type);
    }

    public static <T> Class<T> getFieldActualType(Type rootType, Field field) {
        Type fieldType = TypeUtil.getType(field);
        if (fieldType instanceof Class) return (Class<T>) fieldType;
        if (fieldType instanceof ParameterizedType) return (Class<T>) ((ParameterizedType) fieldType).getRawType();
        if (fieldType instanceof TypeVariable) return getActualType(rootType, fieldType);

        return (Class<T>) Object.class;
    }

    // TODO
    public static <T> Class<T> getFieldActualComponentType(Type rootType, Field field) {
        Type fieldType = TypeUtil.getType(field);
        if (fieldType instanceof Class) return (Class<T>) fieldType;
        if (fieldType instanceof ParameterizedType) return (Class<T>) ((ParameterizedType) fieldType).getRawType();
        if (fieldType instanceof TypeVariable) return (Class<T>) ((ParameterizedType) fieldType).getRawType();
        else return getActualType(rootType, fieldType);
    }

    public static <T> Class<T> getActualType(Type root, Type type) {
        return getActualType(root, type, 0);
    }

    public static <T> Class<T> getActualType(Type root, Type type, int index) {
        if (type instanceof Class) return (Class<T>) type;
        if (!(root instanceof ParameterizedType) || type instanceof WildcardType) return (Class<T>) Object.class;

        if (type instanceof TypeVariable) return getActualType(root, TypeUtil.getActualType(root, type));
        if (type instanceof ParameterizedType) {
            Type   actualType          = TypeUtil.getActualType(root, type);
            Type[] actualTypeArguments = ((ParameterizedType) actualType).getActualTypeArguments();
            if (actualTypeArguments.length == 0) {
                return (Class<T>) Object.class;
            }
            return getActualType(root, actualTypeArguments[index]);
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) TypeUtil.getActualType(root, type);
            return getActualType(root, TypeUtil.getActualType(root, genericArrayType.getGenericComponentType()));
        }

        return (Class<T>) Object.class;
    }
}
