package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ModifierUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.reflect.*;

import static org.fz.nettyx.serializer.struct.StructSerializerContext.*;


/**
 * The type Struct utils.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/26 9:28
 */
@SuppressWarnings("unchecked")
@UtilityClass
public class StructHelper {

    public static <B extends Basic<?>> B newEmptyBasic(Class<?> basicClass) {
        return newBasic(basicClass, Unpooled.wrappedBuffer(new byte[findBasicSize(basicClass)]));
    }

    public static int findBasicSize(Type basicClass) {
        return BASIC_SIZE_CACHE.get(basicClass);
    }

    public static int reflectForSize(Class<? extends Basic<?>> basicClass) {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try {
            return newBasic(basicClass, fillingBuf).getSize();
        }
        finally {
            fillingBuf.skipBytes(fillingBuf.readableBytes()).release();
        }
    }

    /**
     * New basic instance t.
     *
     * @param <B>        the type parameter
     * @param basicClass the basic class
     * @param buf        the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(Class<?> basicClass, ByteBuf buf) {
        try {
            return (B) BASIC_BYTEBUF_CONSTRUCTOR_CACHE.get(basicClass).apply(buf);
        }
        catch (Exception instanceError) {
            Throwable cause = instanceError.getCause();
            if (cause instanceof TooLessBytesException)
                throw new SerializeException(instanceError);
            else
                throw new SerializeException("basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]", instanceError);
        }
    }

    /**
     * New struct instance t.
     *
     * @param <S>         the type parameter
     * @param structClass the struct class
     * @return the t
     */
    public static <S> S newStruct(Type structClass) {
        try {
            if (structClass instanceof Class<?> clazz)
                return (S) NO_ARGS_CONSTRUCTOR_CACHE.get(clazz).get();
            if (structClass instanceof ParameterizedType parameterizedType)
                return (S) NO_ARGS_CONSTRUCTOR_CACHE.get(parameterizedType.getRawType()).get();

            throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not "
                                                    + "find @Struct annotation on class");
        }
        catch (Exception instanceError) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", instanceError);
        }
    }

    public static boolean legalStructField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !isIgnore(field);
    }

    public static boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || ModifierUtil.hasModifier(field,
                                                                                             ModifierUtil.ModifierType.TRANSIENT);
    }

    public static <T> T[] newArray(Type componentType, int length) {
        if (componentType instanceof Class<?> clazz)
            return (T[]) Array.newInstance(clazz, length);
        if (componentType instanceof ParameterizedType parameterizedType)
            return (T[]) Array.newInstance((Class<?>) parameterizedType.getRawType(), length);
        else
            return (T[]) Array.newInstance(Object.class, length);
    }

    public static Type getComponentType(Type root, Type type) {
        if (type instanceof Class<?>         clazz)            return clazz.getComponentType();
        if (type instanceof GenericArrayType genericArrayType) return TypeUtil.getActualType(root, genericArrayType.getGenericComponentType());
        else return type;
    }

    public static Type getElementType(Type root, Type type) {
        if (type instanceof Class<?>          clazz)             return clazz.getComponentType();
        if (type instanceof ParameterizedType parameterizedType) return TypeUtil.getActualType(root, parameterizedType.getActualTypeArguments()[0]);
        else return type;
    }

}
