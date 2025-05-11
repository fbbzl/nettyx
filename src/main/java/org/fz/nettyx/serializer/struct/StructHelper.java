package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.ModifierUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.reflect.*;

import static cn.hutool.core.annotation.AnnotationUtil.hasAnnotation;
import static cn.hutool.core.util.ModifierUtil.hasModifier;
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

    public static <B extends Basic<?>> B newEmptyBasic(Class<?> basicClass)
    {
        return newBasic(basicClass, Unpooled.wrappedBuffer(new byte[findBasicSize(basicClass)]));
    }

    public static int findBasicSize(Type basicClass) {
        return BASIC_SIZE_CACHE.get(basicClass);
    }

    public static int reflectForSize(Class<? extends Basic<?>> basicClass)
    {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try
        {
            return newBasic(basicClass, fillingBuf).getSize();
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
     * @param buf        the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(
            Class<?> basicClass,
            ByteBuf  buf)
    {
        try
        {
            return (B) BASIC_BYTEBUF_CONSTRUCTOR_CACHE.get(basicClass).apply(buf);
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

    public static <T> T[] newArray(
            Type componentType,
            int  length)
    {
        if (componentType instanceof Class<?>          clazz)             return (T[]) Array.newInstance(clazz, length);
        if (componentType instanceof ParameterizedType parameterizedType) return (T[]) Array.newInstance((Class<?>) parameterizedType.getRawType(), length);
        else                                                              return (T[]) Array.newInstance(Object.class, length);
    }
}
