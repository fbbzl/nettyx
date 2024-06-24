package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.reflect.MethodHandleUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Try;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import static cn.hutool.core.lang.reflect.MethodHandleUtil.findConstructor;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.isReadHandler;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.isWriteHandler;
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
public class StructUtils {

    /**
     * Is read handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useReadHandler(AnnotatedElement field) {
        return isReadHandler((StructFieldHandler<?>) StructUtils.getHandler(field));
    }

    /**
     * Is write handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useWriteHandler(AnnotatedElement field) {
        return isWriteHandler((StructFieldHandler<?>) StructUtils.getHandler(field));
    }

    /**
     * Find handler annotation a.
     *
     * @param <A>     the type parameter
     * @param element the element
     * @return the a
     */
    public <A extends Annotation> A findHandlerAnnotation(AnnotatedElement element) {
        for (Annotation annotation : AnnotationUtil.getAnnotations(element, false)) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (ANNOTATION_HANDLER_MAPPING.containsKey(annotationType)) {
                return (A) annotation;
            }
        }
        return null;
    }

    /**
     * Gets serializer handler.
     *
     * @param <H>     the type parameter
     * @param element the element
     * @return the serializer handler
     */
    public <H extends StructFieldHandler<?>> H getHandler(AnnotatedElement element) {
        Annotation handlerAnnotation = findHandlerAnnotation(element);
        if (handlerAnnotation != null) {
            Class<? extends StructFieldHandler<? extends Annotation>> handlerClass = ANNOTATION_HANDLER_MAPPING.get(
                    handlerAnnotation.annotationType());
            return (H) newHandler(handlerClass);
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
    public static <H extends StructFieldHandler<?>> H newHandler(Class<H> clazz) {
        try {
            return (H) CONSTRUCTOR_CACHE.computeIfAbsent(clazz, MethodHandleUtil::findConstructor).invoke();
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
        return BASIC_BYTES_SIZE_CACHE.computeIfAbsent((Class<? extends Basic<?>>) basicClass, Try.apply(StructUtils::reflectForSize));
    }

    public static <B extends Basic<?>> int reflectForSize(Class<B> basicClass) {
        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try {
            return ((Basic<?>) CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, bc -> findConstructor(ByteBuf.class)).invokeWithArguments(fillingBuf)).getSize();
        } catch (Throwable e) {
            throw new SerializeException("can not read basic bytes size", e);
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
            return (B) CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, bc -> findConstructor(ByteBuf.class)).invokeWithArguments(buf);
        } catch (Throwable instanceError) {
            Throwable cause = instanceError.getCause();
            if (cause instanceof TooLessBytesException) {
                throw new SerializeException(cause.getMessage());
            } else {
                throw new SerializeException(
                        "basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf)
                        + "]", cause);
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
                return (S) CONSTRUCTOR_CACHE.computeIfAbsent((Class<S>) structClass, MethodHandleUtil::findConstructor).invoke();
            if (structClass instanceof ParameterizedType)
                return (S) CONSTRUCTOR_CACHE.computeIfAbsent((Class<S>) ((ParameterizedType) structClass).getRawType(), MethodHandleUtil::findConstructor).invoke();

            throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        } catch (Throwable instanceError) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", instanceError);
        }
    }

    public static void writeField(Object object, Field field, Object value) {
        Method writeMethod = FIELD_WRITER_CACHE.computeIfAbsent(field, f -> getWriterHandle(object.getClass(), f));
        MethodHandleUtil.invoke(object, writeMethod, value);
    }

    public static <T> T readField(Object object, Field field) {
        Method readMethod = FIELD_READER_CACHE.computeIfAbsent(field, f -> getReaderHandle(object.getClass(), f));
        return MethodHandleUtil.invoke(object, readMethod);
    }

    public static Method getReaderHandle(Class<?> clazz, Field field) {
        return BeanUtil.getPropertyDescriptor(clazz, field.getName()).getReadMethod();
    }

    public static Method getWriterHandle(Class<?> clazz, Field field) {
        return BeanUtil.getPropertyDescriptor(clazz, field.getName()).getReadMethod();
    }

    /**
     * Gets component type.
     *
     * @param arrayField the array field
     * @return the component type
     */
    public static <C> Class<C> getComponentType(Field arrayField) {
        return (Class<C>) ArrayUtil.getComponentType(arrayField.getType());
    }

    /**
     * Get all fields.
     *
     * @param clazz the clazz
     * @return the field [ ]
     */
    public static Field[] getStructFields(Class<?> clazz) {
        return ReflectUtil.getFields(clazz, f -> !Modifier.isStatic(f.getModifiers()));
    }

}
