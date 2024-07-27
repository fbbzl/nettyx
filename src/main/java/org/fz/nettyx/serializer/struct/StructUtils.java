package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Try;

import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.hutool.core.lang.reflect.MethodHandleUtil.findMethod;
import static cn.hutool.core.text.CharSequenceUtil.upperFirstAndAddPre;
import static java.lang.invoke.MethodType.methodType;
import static org.fz.nettyx.serializer.struct.StructPropHandler.isReadHandler;
import static org.fz.nettyx.serializer.struct.StructPropHandler.isWriteHandler;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.*;
import static org.fz.nettyx.serializer.struct.annotation.Struct.STRUCT_FIELD_CACHE;


/**
 * The type Struct utils.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/26 9:28
 */
@SuppressWarnings("unchecked")
@UtilityClass
public class StructUtils {

    /**
     * Is read handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useReadHandler(Field field) {
        Annotation propHandlerAnnotation = findPropHandlerAnnotation(field);
        if (propHandlerAnnotation != null) {
            Class<? extends StructPropHandler<? extends Annotation>>
                    handlerClass = StructPropHandler.ANNOTATION_HANDLER_MAPPING.get(propHandlerAnnotation.annotationType());
            if (handlerClass != null) return isReadHandler(handlerClass);
        }
        return false;
    }

    /**
     * Is write handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useWriteHandler(Field field) {
        Annotation propHandlerAnnotation = findPropHandlerAnnotation(field);
        if (propHandlerAnnotation != null) {
            Class<? extends StructPropHandler<? extends Annotation>>
                    handlerClass = StructPropHandler.ANNOTATION_HANDLER_MAPPING.get(propHandlerAnnotation.annotationType());
            if (handlerClass != null) return isWriteHandler(handlerClass);
        }
        return false;
    }

    /**
     * Find handler annotation a.
     *
     * @param <A>     the type parameter
     * @param field the field
     * @return the a
     */
    public <A extends Annotation> A findPropHandlerAnnotation(Field field) {
        return (A) FIELD_PROP_HANDLER_ANNOTATION_CACHE.get(field);
    }

    /**
     * Gets serializer handler.
     *
     * @param <H>   the type parameter
     * @param field the element
     * @return the serializer handler
     */
    public <H extends StructPropHandler<?>> H getPropHandler(Field field) {
        Annotation handlerAnnotation = findPropHandlerAnnotation(field);

        if (handlerAnnotation != null) {
            Class<? extends StructPropHandler<? extends Annotation>>
                    handlerClass = StructPropHandler.ANNOTATION_HANDLER_MAPPING.get(handlerAnnotation.annotationType());
            boolean isSingleton = Singleton.exists(handlerClass);

            if (isSingleton) return (H) Singleton.get(handlerClass);
            else             return (H) newPropHandler(handlerClass);
        }

        return null;
    }

    /**
     * New handler instance t.
     *
     * @param <H>   the type parameter
     * @param clazz the struct class
     * @return the t
     */
    static <H extends StructPropHandler<? extends Annotation>> H newPropHandler(Class<H> clazz) {
        try {
            return (H) CONSTRUCTOR_SUPPLIER_CACHE.computeIfAbsent(clazz, StructUtils::constructorSupplier).get();
        } catch (Throwable exception) {
            throw new SerializeException("serializer handler [" + clazz + "] instantiate failed...", exception);
        }
    }

    public static <B extends Basic<?>> B newEmptyBasic(Field basicField) {
        return newEmptyBasic(basicField.getType());
    }

    public static <B extends Basic<?>> B newEmptyBasic(Class<?> basicClass) {
        return newBasic(basicClass, Unpooled.wrappedBuffer(new byte[findBasicSize(basicClass)]));
    }

    public static int findBasicSize(Class<?> basicClass) {
        return Basic.BASIC_BYTES_SIZE_CACHE.computeIfAbsent((Class<? extends Basic<?>>) basicClass, Try.apply(StructUtils::reflectForSize));
    }

    public static <B extends Basic<?>> int reflectForSize(Class<B> basicClass) {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try {
            return newBasic(basicClass, fillingBuf).getSize();
        } finally {
            fillingBuf.skipBytes(fillingBuf.readableBytes());
            fillingBuf.release();
        }
    }

    /**
     * New basic instance t.
     *
     * @param <B>        the type parameter
     * @param basicField the basic field
     * @param buf        the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(Field basicField, ByteBuf buf) {
        return newBasic(basicField.getType(), buf);
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
            return (B) BASIC_CONSTRUCTOR_FUNCTION_CACHE.computeIfAbsent(basicClass, bc -> constructorFunction(basicClass, ByteBuf.class)).apply(buf);
        } catch (Throwable instanceError) {
            Throwable cause = instanceError.getCause();
            if (cause instanceof TooLessBytesException) {
                throw new SerializeException(instanceError);
            } else {
                throw new SerializeException(
                        "basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf)
                        + "]", instanceError);
            }
        }
    }

    /**
     * New struct instance t.
     *
     * @param <S>         the type parameter
     * @param structField the struct field
     * @return the t
     */
    public static <S> S newStruct(Field structField) {
        return (S) newStruct(structField.getType());
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
                return (S) CONSTRUCTOR_SUPPLIER_CACHE.computeIfAbsent((Class<S>) structClass, StructUtils::constructorSupplier).get();
            if (structClass instanceof ParameterizedType)
                return (S) CONSTRUCTOR_SUPPLIER_CACHE.computeIfAbsent((Class<S>) ((ParameterizedType) structClass).getRawType(), StructUtils::constructorSupplier).get();

            throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        } catch (Throwable instanceError) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", instanceError);
        }
    }

    public static <T> Supplier<T> constructorSupplier(Class<T> clazz) {
        try {
            Lookup       lookup            = MethodHandles.lookup();
            MethodHandle constructorHandle = lookup.findConstructor(clazz, methodType(void.class));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "get",
                    MethodType.methodType(Supplier.class),
                    constructorHandle.type().generic(),
                    constructorHandle,
                    constructorHandle.type());

            return (Supplier<T>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("can not generate lambda constructor");
        }
    }

    public static <A, T> Function<A, T> constructorFunction(Class<T> clazz, Class<?>... args) {
        try {
            Lookup       lookup            = MethodHandles.lookup();
            MethodHandle constructorHandle = lookup.findConstructor(clazz, methodType(void.class, args));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    constructorHandle.type().generic(),
                    constructorHandle,
                    constructorHandle.type());

            return (Function<A, T>) site.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("can not generate lambda constructor");
        }
    }

    public static void writeField(Object object, Field field, Object value) throws Throwable {
        MethodHandle writeMethod = FIELD_WRITER_CACHE.computeIfAbsent(field, f -> getWriterHandle(object.getClass(), f));
        writeMethod.invoke(object, value);
    }

    public static <T> T readField(Object object, Field field) throws Throwable {
        MethodHandle readMethod = FIELD_READER_CACHE.computeIfAbsent(field, f -> getReaderHandle(object.getClass(), f));
        return (T) readMethod.invoke(object);
    }

    public static MethodHandle getReaderHandle(Class<?> clazz, Field field) {
        return findMethod(clazz, upperFirstAndAddPre(field.getName(), "get"), methodType(field.getType()));
    }

    public static MethodHandle getWriterHandle(Class<?> clazz, Field field) {
        return findMethod(clazz, upperFirstAndAddPre(field.getName(), "set"), methodType(void.class, field.getType()));
    }

    public static <C> Class<C> getComponentType(Field arrayField) {
        return (Class<C>) ArrayUtil.getComponentType(arrayField.getType());
    }

    public static Field[] getStructFields(Class<?> clazz) {
        return STRUCT_FIELD_CACHE.get(clazz);
    }

    public static boolean isNotBasic(Class<?> clazz) {
        return !isBasic(clazz);
    }

    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public static boolean isBasic(Type root, Field field) {
        return isBasic(root, TypeUtil.getType(field));
    }

    public static boolean isBasic(Type root, Type type) {
        if (type instanceof Class)        return isBasic((Class<?>) type);
        if (type instanceof TypeVariable) return isBasic(root, TypeUtil.getActualType(root, type));

        return false;
    }

    public static boolean isNotStruct(Class<?> clazz) {
        return !isStruct(clazz);
    }

    public static boolean isStruct(Class<?> clazz) {
        return STRUCT_FIELD_CACHE.containsKey(clazz);
    }

    public static boolean isStruct(Type root, Field field) {
        return isStruct(root, TypeUtil.getType(field));
    }

    public static boolean isStruct(Type root, Type type) {
        if (type instanceof Class)             return isStruct((Class<?>) type);
        if (type instanceof ParameterizedType) return isStruct((Class<?>) ((ParameterizedType) type).getRawType());
        if (type instanceof TypeVariable)      return isStruct(root, TypeUtil.getActualType(root, type));

        return false;
    }


}
