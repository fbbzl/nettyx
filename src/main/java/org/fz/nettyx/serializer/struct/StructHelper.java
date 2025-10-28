package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ModifierUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.Struct.Endian;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.reflect.*;
import java.nio.ByteOrder;

import static cn.hutool.core.annotation.AnnotationUtil.hasAnnotation;
import static cn.hutool.core.util.ModifierUtil.hasModifier;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
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

    public static <B extends Basic<?>> B newEmptyBasic(Class<?> basicClass, ByteOrder byteOrder)
    {
        return newBasic(basicClass, byteOrder, Unpooled.wrappedBuffer(new byte[findBasicSize(basicClass)]));
    }

    public static int findBasicSize(Type basicClass) {
        return BASIC_SIZE_CACHE.get(basicClass);
    }

    public static ByteOrder getByteOrder(Class<?> clazz) {
        Struct annotation = AnnotationUtil.getAnnotation(clazz, Struct.class);
        if (annotation == null) return Endian.NATIVE.getByteOrder();

        return annotation.endian().getByteOrder();
    }

    public static int reflectForSize(Class<? extends Basic<?>> basicClass)
    {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try
        {
            return newBasic(basicClass, ByteOrder.nativeOrder(), fillingBuf).getSize();
        }
        finally
        {
            fillingBuf.skipBytes(fillingBuf.readableBytes()).release();
        }
    }

    /**
     * New basic instance t.
     *
     * @param <B>        the type parameter
     * @param basicClass the basic class
     * @param byteOrder  the byte order
     * @param buf        the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(
            Class<?>  basicClass,
            ByteOrder byteOrder,
            ByteBuf   buf)
    {
        try
        {
            return (B) BASIC_CONSTRUCTOR_CACHE.get(basicClass).apply(byteOrder, buf);
        }
        catch (Exception instanceError)
        {
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
     * @param <S>        the type parameter
     * @param structType the struct type
     * @return the t
     */
    public static <S> S newStruct(Type structType)
    {
        try
        {
            return (S) getStructDefinition(structType).constructor().get();
        }
        catch (Exception instanceError)
        {
            throw new SerializeException("struct [" + structType + "] instantiate failed...", instanceError);
        }
    }

    public static boolean legalStructField(Field field)
    {
        return !Modifier.isStatic(field.getModifiers()) && !isIgnore(field);
    }

    public static boolean isIgnore(Field field)
    {
        return hasAnnotation(field, Ignore.class) || hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }

    public static <T> T[] newArray(Type componentType, int length)
    {
        return switch (componentType) {
            case Class<?> clazz ->
                    (T[]) Array.newInstance(clazz, length);
            case ParameterizedType parameterizedType ->
                    (T[]) Array.newInstance((Class<?>) parameterizedType.getRawType(), length);
            default ->
                    (T[]) Array.newInstance(Object.class, length);
        };
    }

    public static <T> T basicNullDefault(
            Object    fieldValue,
            ByteOrder byteOrder,
            Class<?>  fieldActualType)
    {
        return (T) defaultIfNull(fieldValue, () -> newEmptyBasic(fieldActualType, byteOrder));
    }

    public static <T> T structNullDefault(
            Object fieldValue,
            Type   fieldActualType)
    {
        return (T) defaultIfNull(fieldValue, () -> newStruct(fieldActualType));
    }
}
