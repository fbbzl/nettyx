package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ModifierUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.util.lambda.Try.LambdasException;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.upperFirstAndAddPre;
import static java.lang.invoke.MethodType.methodType;
import static org.fz.nettyx.serializer.struct.StructDefinition.STRUCT_DEFINITION_CACHE;
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

    public static <T> Class<T> getRawType(Type type) {
        if (type instanceof Class<?> clazz)                      return (Class<T>) clazz;
        else
        if (type instanceof ParameterizedType parameterizedType) return (Class<T>) parameterizedType.getRawType();

        throw new TypeJudgmentException(type);
    }

    public static <B extends Basic<?>> B newEmptyBasic(Type basicClass) {
        return newBasic(basicClass, Unpooled.wrappedBuffer(new byte[findBasicSize(basicClass)]));
    }

    public static int findBasicSize(Type basicClass) {
        return BASIC_SIZE_CACHE.get(basicClass);
    }

    public static int reflectForSize(Type basicClass) {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try {
            return newBasic(basicClass, fillingBuf).getSize();
        } finally {
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
    public static <B extends Basic<?>> B newBasic(Type basicClass, ByteBuf buf) {
        try {
            return (B) BASIC_BYTEBUF_CONSTRUCTOR_CACHE.get(basicClass).apply(buf);
        } catch (Exception instanceError) {
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
            if (structClass instanceof Class)
                return (S) NO_ARGS_CONSTRUCTOR_CACHE.get(structClass).get();
            if (structClass instanceof ParameterizedType parameterizedType)
                return (S) NO_ARGS_CONSTRUCTOR_CACHE.get(parameterizedType.getRawType()).get();

            throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        } catch (Exception instanceError) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", instanceError);
        }
    }

    public static boolean legalStructField(Field field) {
       return  !Modifier.isStatic(field.getModifiers()) && !isIgnore(field);
    }

    public static boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || ModifierUtil.hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }

    public static <T> Supplier<T> constructor(Type clazz) {
        try {
            Lookup       lookup            = MethodHandles.lookup();
            MethodHandle constructorHandle = lookup.findConstructor((Class<T>) clazz, methodType(void.class));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "get",
                    MethodType.methodType(Supplier.class),
                    constructorHandle.type().generic(),
                    constructorHandle,
                    constructorHandle.type());

            return (Supplier<T>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new LambdasException("can not generate lambda constructor for class [" + clazz + "]");
        }
    }

    public static <P, T> Function<P, T> constructor(Type clazz, Class<P> paramType) {
        try {
            Lookup       lookup            = MethodHandles.lookup();
            MethodHandle constructorHandle = lookup.findConstructor((Class<T>) clazz, methodType(void.class, paramType));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    constructorHandle.type().generic(),
                    constructorHandle,
                    constructorHandle.type());

            return (Function<P, T>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new LambdasException("can not generate lambda constructor for class [" + clazz + "], param type: [" + paramType + "]");
        }
    }

    public static <T, R> Function<T, R> getter(Type clazz, Class<R> returnType, String methodName) {
        try {
            Lookup       lookup       = MethodHandles.lookup();
            MethodHandle getterHandle = lookup.findVirtual((Class<T>) clazz, methodName, methodType(returnType));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    methodType(Function.class),
                    methodType(Object.class, Object.class),
                    getterHandle,
                    getterHandle.type());

            return (Function<T, R>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("can not generate lambda getter, class [" + clazz + "], method: [" + methodName + "], return type: [" + returnType + "]");
        }
    }

    public static <A, P> BiConsumer<A, P> setter(Class<A> clazz, Class<P> paramType, String methodName) {
        try {
            Lookup       lookup       = MethodHandles.lookup();
            MethodHandle setterHandle = lookup.findVirtual(clazz, methodName, methodType(void.class, paramType));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    methodType(BiConsumer.class),
                    methodType(void.class, Object.class, Object.class),
                    setterHandle,
                    setterHandle.type());

            return (BiConsumer<A, P>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("can not generate lambda setter, class [" + clazz + "], method: [" + methodName + "], param type: [" + paramType + "]");
        }
    }

    public static <A, R> Function<A, R> lambdaGetter(Field field) {
        return (Function<A, R>) getter(field.getDeclaringClass(), field.getType(), upperFirstAndAddPre(field.getName(), "get"));
    }

    public static <A, P> BiConsumer<A, P> lambdaSetter(Field field) {
        return (BiConsumer<A, P>) setter(field.getDeclaringClass(), field.getType(), upperFirstAndAddPre(field.getName(), "set"));
    }

    public static StructDefinition getStructDefinition(Class<?> clazz) {
        return STRUCT_DEFINITION_CACHE.get(clazz);
    }
}
