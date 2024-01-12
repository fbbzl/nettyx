package org.fz.nettyx.serializer.struct;

import static org.fz.nettyx.serializer.struct.PropertyHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.ANNOTATION_HANDLER_MAPPING_CACHE;
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
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

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

    public Class<?> getFieldParameterizedType(Field field) {
        Type type = TypeUtil.getType(field);
        // If it's a Class, it means that no generics are specified
        if (type instanceof Class<?>) {
            return Object.class;
        }

        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();

        // will get the first, will not appear index-out-of-bounds exception
        Type actualTypeArgument = actualTypeArguments[0];
        Throws.ifInstanceOf(TypeVariableImpl.class, actualTypeArgument, "please use TypeReference to assign generic type");

        return (Class<?>) actualTypeArgument;
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
     * @param <S> the type parameter
     * @param element the element
     * @return the serializer handler
     */
    public <S extends PropertyHandler<?>> S getHandler(AnnotatedElement element) {
        Annotation handlerAnnotation = findHandlerAnnotation(element);
        if (handlerAnnotation != null) {
            Class<? extends PropertyHandler<? extends Annotation>> handlerClass = ANNOTATION_HANDLER_MAPPING_CACHE.get(
                handlerAnnotation.annotationType());
            return (S) newHandler(handlerClass);
        }
        return null;
    }

    /**
     * New handler instance t.
     *
     * @param <T> the type parameter
     * @param clazz the struct class
     * @return the t
     */
    static <T extends PropertyHandler<?>> T newHandler(Class<T> clazz) {
        try {
            return ReflectUtil.getConstructor(clazz).newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("serializer handler [" + clazz + "] instantiate failed...", exception);
        }
    }

    static <T extends Basic<?>> T newBasic(Field basicField, Object fieldValue) {
        return newBasic((Class<T>) basicField.getType(), fieldValue);
    }

    public static <T extends Basic<?>> T newBasic(Class<T> basicClass, Object fieldValue) {
        try {
            if (isBasic(basicClass)) {
                Constructor<T> constructor = ReflectUtil.getConstructor(basicClass, );
                constructor.
                return constructor.newInstance(fieldValue);
            } else {
                throw new UnsupportedOperationException(
                    "can not create instance of basic type [" + basicClass + "], its not a Basic type");
            }
        } catch (InvocationTargetException invocationException) {
            Throwable cause = invocationException.getCause();
            if (cause instanceof TooLessBytesException) {
                throw new SerializeException(cause.getMessage());
            } else {
                throw new SerializeException(cause);
            }
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new SerializeException(
                "new basic [" + basicClass + "] instantiate by value failed..., value is: [" + fieldValue + "]",
                exception);
        }
    }

    static <T extends Basic<?>> T newBasic(Field basicField, ByteBuf buf) {
        return newBasic((Class<T>) basicField.getType(), buf);
    }

    public static <T extends Basic<?>> T newBasic(Class<T> basicClass, ByteBuf buf) {
        try {
            if (isBasic(basicClass)) {
                return ReflectUtil.getConstructor(basicClass, ByteBuf.class).newInstance(buf);
            } else {
                throw new UnsupportedOperationException(
                    "can not create instance of basic type [" + basicClass + "], its not a Basic type");
            }
        } catch (InvocationTargetException invocationException) {
            Throwable cause = invocationException.getCause();
            if (cause instanceof TooLessBytesException) {
                throw new SerializeException(cause.getMessage());
            } else {
                throw new SerializeException(cause);
            }
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new SerializeException(
                "new basic [" + basicClass + "] instantiate by buffer failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]",
                exception);
        }
    }

    /**
     * New struct instance t.
     *
     * @param <T> the type parameter
     * @param structField the struct field
     * @return the t
     */
    public static <T> T newStruct(Field structField) {
        return (T) newStruct(structField.getType());
    }

    /**
     * New struct instance t.
     *
     * @param <T> the type parameter
     * @param structClass the struct class
     * @return the t
     */
    public static <T> T newStruct(Class<T> structClass) {
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
     * Check assignable.
     *
     * @param field the field
     * @param clazz the clazz
     */
    public static void checkAssignable(Field field, Class<?> clazz) {
        Class<?> type = field.getType();

        Throws.ifNotAssignable(clazz, type, new TypeJudgmentException(
            "type of field [" + field + "] is [" + field.getType() + "], it is not assignable from [" + clazz + "]"));
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

        private static synchronized void scanAllBasics() {
            Set<Class<?>> basicClasses = ClassScanner.scanAllPackageBySuper(ALL_PACKAGE, Basic.class);
            for (Class<?> basicClass : basicClasses) {
                int mod = basicClass.getModifiers();
                if (basicClass.isEnum() || Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
                    continue;
                }
                ReflectUtil.getConstructor(basicClass);
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
