package org.fz.nettyx.serializer.struct;

import static org.fz.nettyx.serializer.struct.PropertyHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.ANNOTATION_HANDLER_MAPPING_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.BASIC_BYTES_SIZE_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_READER_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_WRITER_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.TRANSIENT_FIELD_CACHE;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.reflect.MethodHandleUtil;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Try;


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

    public boolean isTransient(Field field) {
        return TRANSIENT_FIELD_CACHE.contains(field);
    }

    public Class<?> getFieldParameterizedType(Type type, Field field) {
        Type fieldType = TypeUtil.getType(field);
        // If it's a Class, it means that no generics are specified
        if (fieldType instanceof Class<?>) {
            return Object.class;
        }
        else
        if (type instanceof ParameterizedType) {
            Type actualType = TypeUtil.getActualType(type, field);
            Type[] actualTypeArguments = ((ParameterizedType) actualType).getActualTypeArguments();
            if (actualTypeArguments.length == 0) return Object.class;

            return (Class<?>) actualTypeArguments[0];
        }
        return Object.class;
    }

    /**
     * Find handler annotation a.
     *
     * @param <A> the type parameter
     * @param element the element
     * @return the a
     */
    public <A extends Annotation> A findHandlerAnnotation(AnnotatedElement element) {
        for (Annotation annotation : AnnotationUtil.getAnnotations(element, false)) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (ANNOTATION_HANDLER_MAPPING_CACHE.containsKey(annotationType)) {
                return (A) annotation;
            }
        }
        return null;
    }

    /**
     * Gets serializer handler.
     *
     * @param <H> the type parameter
     * @param element the element
     * @return the serializer handler
     */
    public <H extends PropertyHandler<?>> H getHandler(AnnotatedElement element) {
        Annotation handlerAnnotation = findHandlerAnnotation(element);
        if (handlerAnnotation != null) {
            Class<? extends PropertyHandler<? extends Annotation>> handlerClass = ANNOTATION_HANDLER_MAPPING_CACHE.get(
                handlerAnnotation.annotationType());
            return (H) newHandler(handlerClass);
        }
        return null;
    }

    /**
     * New handler instance t.
     *
     * @param <H> the type parameter
     * @param clazz the struct class
     * @return the t
     */
    public static <H extends PropertyHandler<?>> H newHandler(Class<H> clazz) {
        try {
            return ReflectUtil.getConstructor(clazz).newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("serializer handler [" + clazz + "] instantiate failed...", exception);
        }
    }

    public static <B extends Basic<?>> B newEmptyBasic(Field basicField) {
        return newEmptyBasic((Class<B>) basicField.getType());
    }

    public static <B extends Basic<?>> B newEmptyBasic(Class<B> basicClass) {
        int basicBytesSize = BASIC_BYTES_SIZE_CACHE.computeIfAbsent(basicClass, Try.apply(Basic::reflectForSize));
        byte[] zeroedBytes = new byte[basicBytesSize];
        return newBasic(basicClass, Unpooled.wrappedBuffer(zeroedBytes));
    }

    /**
     * New basic instance t.
     *
     * @param <B> the type parameter
     * @param basicField the basic field
     * @param buf the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(Field basicField, ByteBuf buf) {
        return newBasic((Class<B>) basicField.getType(), buf);
    }

    /**
     * New basic instance t.
     *
     * @param <B> the type parameter
     * @param basicClass the basic class
     * @param buf the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(Class<B> basicClass, ByteBuf buf) {
        try {
            return ReflectUtil.getConstructor(basicClass, ByteBuf.class).newInstance(buf);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException invocationException) {
            Throwable cause = invocationException.getCause();
            if (cause instanceof TooLessBytesException) {
                throw new SerializeException(cause.getMessage());
            } else {
                throw new SerializeException(
                    "basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf)
                        + "]", cause);
            }
        }
    }

    public static <B extends Basic<?>> Constructor<B> filterConstructor(Class<B> basicClass,
        Predicate<Constructor<B>> filter) {
        Constructor<B>[] constructors = ReflectUtil.getConstructors(basicClass);
        return Arrays.stream(constructors).filter(filter).findFirst().orElse(null);
    }

    /**
     * New struct instance t.
     *
     * @param <S> the type parameter
     * @param structField the struct field
     * @return the t
     */
    public static <S> S newStruct(Field structField) {
        return (S) newStruct(structField.getType());
    }

    /**
     * New struct instance t.
     *
     * @param <S> the type parameter
     * @param structClass the struct class
     * @return the t
     */
    public static <S> S newStruct(Class<S> structClass) {
        try {
            if (isStruct(structClass)) return ReflectUtil.getConstructor(structClass).newInstance();
            else                       throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", exception);
        }
    }

    public static void writeField(Object object, Field field, Object value) {
        Method writeMethod = FIELD_WRITER_CACHE.computeIfAbsent(field,
            f -> BeanUtil.getPropertyDescriptor(object.getClass(), f.getName()).getWriteMethod());
        MethodHandleUtil.invoke(object, writeMethod, value);
    }

    public static <T> T readField(Object object, Field field) {
        Method readMethod = FIELD_READER_CACHE.computeIfAbsent(field,
            f -> BeanUtil.getPropertyDescriptor(object.getClass(), f.getName()).getReadMethod());
        return MethodHandleUtil.invoke(object, readMethod);
    }

    /**
     * Gets component type.
     *
     * @param arrayField the array field
     * @return the component type
     */
    public static Class<?> getComponentType(Field arrayField) {
        return ArrayUtil.getComponentType(arrayField.getType());
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

    /**
     * The type Struct cache.
     */
    static final class StructCache {

        static final Set<Field> TRANSIENT_FIELD_CACHE = new ConcurrentHashSet<>(512);

        /* reflection cache */
        static final Map<Field, Method> FIELD_READER_CACHE = new WeakConcurrentMap<>();
        /**
         * The Field write method cache.
         */
        static final Map<Field, Method> FIELD_WRITER_CACHE = new WeakConcurrentMap<>();

        static final Map<Class<? extends Basic<?>>, Integer> BASIC_BYTES_SIZE_CACHE = new WeakConcurrentMap<>();

        /**
         * The constant ANNOTATION_HANDLER_MAPPING_CACHE.
         */
        /* mapping handler and annotation */
        static final Map<Class<? extends Annotation>, Class<? extends PropertyHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new WeakConcurrentMap<>();

        static {
            try {
                scanAllHandlers();
                scanAllBasics();
                scanAllStructs();
            } catch (Exception e) {
                throw new NotInitedException("init serializer context failed please check", e);
            }
        }

        private static final String ALL_PACKAGE = "";

        private static synchronized void scanAllHandlers() {
            Set<Class<?>> handlerClasses = ClassScanner.scanAllPackageBySuper(ALL_PACKAGE, PropertyHandler.class);

            for (Class<?> handlerClass : handlerClasses) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(handlerClass);
                if (targetAnnotationType != null) {
                    ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(targetAnnotationType,
                        (Class<? extends PropertyHandler<? extends Annotation>>) handlerClass);

                    ReflectUtil.getConstructor(handlerClass);
                }
            }
        }

        private static synchronized void scanAllBasics()
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
            Set<Class<?>> basicClasses = ClassScanner.scanAllPackageBySuper(ALL_PACKAGE, Basic.class);
            for (Class<?> basicClass : basicClasses) {
                int mod = basicClass.getModifiers();
                if (basicClass.isEnum() || Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
                    continue;
                }

                BASIC_BYTES_SIZE_CACHE.put((Class<? extends Basic<?>>) basicClass,
                    Basic.reflectForSize((Class<? extends Basic<?>>) basicClass));
            }
        }

        private static synchronized void scanAllStructs() throws IntrospectionException {
            Set<Class<?>> structClasses = ClassScanner.scanAllPackageByAnnotation(ALL_PACKAGE, Struct.class);
            for (Class<?> structClass : structClasses) {
                ReflectUtil.getConstructor(structClass);

                Field[] structFields = StructUtils.getStructFields(structClass);

                for (Field field : structFields) {
                    if (Modifier.isTransient(field.getModifiers())) {
                        TRANSIENT_FIELD_CACHE.add(field);
                    }

                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), structClass);
                    FIELD_READER_CACHE.putIfAbsent(field, propertyDescriptor.getReadMethod());
                    FIELD_WRITER_CACHE.put(field, propertyDescriptor.getWriteMethod());

                    AnnotationUtil.getAnnotations(field, false);
                }
            }
        }

        private StructCache() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}
