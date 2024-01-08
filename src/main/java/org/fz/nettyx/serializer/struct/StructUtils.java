package org.fz.nettyx.serializer.struct;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.fz.nettyx.serializer.struct.PropertyHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.ANNOTATION_HANDLER_MAPPING_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.BASIC_BUF_CONSTRUCTOR_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_READER_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_WRITER_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.STRUCT_CONSTRUCTOR_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.STRUCT_FIELDS_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.TRANSIENT_FIELD_CACHE;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Struct;

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
     * The Is static.
     */
    Predicate<Field> isStatic = f -> Modifier.isStatic(f.getModifiers());

    public boolean isTransient(Field field) {
        return TRANSIENT_FIELD_CACHE.contains(field);
    }

    /**
     * Is annotation present boolean.
     *
     * @param <A> the type parameter
     * @param element the element
     * @param annoType the anno type
     * @return the boolean
     */
    public <A extends Annotation> boolean isAnnotationPresent(AnnotatedElement element, Class<A> annoType) {
        return findAnnotation(element, annoType) != null;
    }

    /**
     * Find handler annotation a.
     *
     * @param <A> the type parameter
     * @param element the element
     * @return the a
     */
    public <A extends Annotation> A findHandlerAnnotation(AnnotatedElement element) {
        for (Annotation annotation : allAnnotations(element)) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (ANNOTATION_HANDLER_MAPPING_CACHE.containsKey(annotationType)) {
                return (A) annotation;
            }
        }
        return null;
    }

    /**
     * All annotations collection.
     *
     * @param <A> the type parameter
     * @param element the element
     * @return the collection
     */
    public <A extends Annotation> Collection<A> allAnnotations(AnnotatedElement element) {
        return (Collection<A>) StructCache.ANNOTATED_ELEMENT_ANNOTATION_CACHE.computeIfAbsent(element,
            e -> newHashSet(e.getAnnotations()));
    }

    /**
     * Find annotation a.
     *
     * @param <A> the type parameter
     * @param element the element
     * @param clazz the clazz
     * @return the a
     */
    public <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> clazz) {
        return filterAnnotation(element, a -> a.annotationType() == clazz);
    }

    /**
     * Find annotations set.
     *
     * @param <A> the type parameter
     * @param element the element
     * @param clazz the clazz
     * @return the set
     */
    public <A extends Annotation> Set<A> findAnnotations(AnnotatedElement element, Class<A> clazz) {
        return filterAnnotations(element, a -> a.annotationType() == clazz);
    }

    /**
     * Filter annotation a.
     *
     * @param <A> the type parameter
     * @param element the element
     * @param filter the filter
     * @return the a
     */
    public <A extends Annotation> A filterAnnotation(AnnotatedElement element, Predicate<Annotation> filter) {
        Set<Annotation> annotations = filterAnnotations(element, filter);
        if (annotations.size() > 1) {
            throw new UnsupportedOperationException(
                "keep [" + element + "] only have one serializer handler annotation, annotation now are ["
                    + annotations + "]");
        }
        if (annotations.isEmpty()) {
            return null;
        }
        return (A) annotations.iterator().next();
    }

    /**
     * Filter annotations a [ ].
     *
     * @param <A> the type parameter
     * @param annotatedElement the annotated element
     * @param filter the filter
     * @return the a [ ]
     */
    public <A extends Annotation> Set<A> filterAnnotations(AnnotatedElement annotatedElement,
        Predicate<Annotation> filter) {
        return (Set<A>) allAnnotations(annotatedElement).stream().filter(filter).collect(toSet());
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
     * Gets handler constructor.
     *
     * @param <H> the type parameter
     * @param handlerClass the handler class
     * @return the handler constructor
     */
    <H extends PropertyHandler<? extends Annotation>> Constructor<H> getHandlerConstructor(Class<H> handlerClass) {
        return (Constructor<H>) StructCache.HANDLER_CONSTRUCTOR_CACHE.computeIfAbsent(handlerClass, c -> {
            try {
                return c.getConstructor();
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find serializer handler [" + handlerClass + "] no-args constructor", exception);
            }
        });
    }

    /**
     * Gets struct constructor.
     *
     * @param <C> the type parameter
     * @param structClass the struct class
     * @return the struct constructor
     */
    <C> Constructor<C> getStructConstructor(Class<C> structClass) {
        return (Constructor<C>) STRUCT_CONSTRUCTOR_CACHE.computeIfAbsent(structClass, c -> {
            try {
                return c.getConstructor();
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find struct [" + structClass + "] no-args constructor", exception);
            }
        });
    }

    /**
     * Gets basic constructor.
     *
     * @param <B> the type parameter
     * @param basicClass the basic class
     * @return the basic constructor
     */
    <B extends Basic<?>> Constructor<B> getBasicConstructor(Class<B> basicClass) {
        return (Constructor<B>) BASIC_BUF_CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, c -> {
            try {
                return c.getConstructor(ByteBuf.class);
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find basic constructor with arg [" + ByteBuf.class + "]", exception);
            }
        });
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
            return getHandlerConstructor(clazz).newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("serializer handler [" + clazz + "] instantiate failed...", exception);
        }
    }

    /**
     * New basic instance t.
     *
     * @param <T> the type parameter
     * @param basicField the basic field
     * @param buf the buf
     * @return the t
     */
    static <T extends Basic<?>> T newBasic(Field basicField, ByteBuf buf) {
        return newBasic((Class<T>) basicField.getType(), buf);
    }

    /**
     * New basic instance t.
     *
     * @param <T> the type parameter
     * @param basicClass the basic class
     * @param buf the buf
     * @return the t
     */
    public static <T extends Basic<?>> T newBasic(Class<T> basicClass, ByteBuf buf) {
        try {
            if (isBasic(basicClass)) return getBasicConstructor(basicClass).newInstance(buf);
            else                     throw new UnsupportedOperationException("can not create instance of basic type [" + basicClass + "], its not a Basic type");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException(
                "basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]", exception);
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
            if (isStruct(structClass)) return getStructConstructor(structClass).newInstance();
            else                       throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("struct [" + structClass + "] instantiate failed...", exception);
        }
    }


    /**
     * The constant EMPTY_FIELD_ARRAY.
     */
    static final Field[] EMPTY_FIELD_ARRAY = {};

    /**
     * Write field.
     *
     * @param object the object
     * @param field the field
     * @param value the length
     */
    public static void writeField(Object object, Field field, Object value) {
        try {
            Method writeMethod = FIELD_WRITER_CACHE.computeIfAbsent(field, f -> {
                try {
                    return new PropertyDescriptor(field.getName(), object.getClass()).getWriteMethod();
                } catch (IntrospectionException exception) {
                    throw new UnsupportedOperationException(
                        "field write failed, field is [" + field + "], length is [" + value
                            + "], check the correct parameter type of field getter/setter", exception);
                }
            });
            writeMethod.invoke(object, value);
        } catch (Exception exception) {
            throw new UnsupportedOperationException("field write failed, field is [" + field + "], length is [" + value
                + "], check the correct parameter type of field getter/setter", exception);
        }
    }

    /**
     * Read field t.
     *
     * @param <T> the type parameter
     * @param object the object
     * @param field the field
     * @return the t
     */
    public static <T> T readField(Object object, Field field) {
        try {
            Method readMethod = FIELD_READER_CACHE.computeIfAbsent(field, f -> {
                try {
                    return new PropertyDescriptor(field.getName(), object.getClass()).getReadMethod();
                } catch (IntrospectionException exception) {
                    throw new UnsupportedOperationException(
                        "field read failed, field is [" + field + "], check parameter type or field getter/setter",
                        exception);
                }
            });
            return (T) readMethod.invoke(object);
        } catch (Exception exception) {
            throw new UnsupportedOperationException(
                "field read failed, field is [" + field + "], check parameter type or field getter/setter", exception);
        }
    }

    /**
     * Gets component type.
     *
     * @param arrayField the array field
     * @return the component type
     */
    public static Class<?> getComponentType(Field arrayField) {
        return arrayField.getType().getComponentType();
    }

    /**
     * Gets all fields list.
     *
     * @param clazz the clazz
     * @return the all fields list
     */
    public static List<Field> getStructFieldList(Class<?> clazz) {
        return Arrays.asList(getStructFields(clazz));
    }

    /**
     * Get all fields.
     *
     * @param clazz the clazz
     * @return the field [ ]
     */
    public static Field[] getStructFields(Class<?> clazz) {
        return STRUCT_FIELDS_CACHE.computeIfAbsent(clazz, c -> {
            final List<Field> allFields = new ArrayList<>(10);
            Class<?> currentClass = clazz;
            while (currentClass != null && isStruct(currentClass)) {
                final Field[] declaredFields = currentClass.getDeclaredFields();
                Collections.addAll(allFields, declaredFields);
                currentClass = currentClass.getSuperclass();
            }
            // remove static fields
            allFields.removeIf(isStatic);

            return allFields.toArray(EMPTY_FIELD_ARRAY);
        });
    }

    /**
     * reverse bytes order
     *
     * @param bytes the bytes
     * @return the byte [ ]
     */
    public static byte[] reverseOrder(byte[] bytes) {
        for (int i = 0, j = bytes.length - 1; i < bytes.length / 2; i++, j--) {
            byte stage = bytes[i];
            bytes[i] = bytes[j];
            bytes[j] = stage;
        }
        return bytes;
    }

    public static <T> T nullDefault(T obj, Supplier<T> defSupplier) {
        if (obj == null) return defSupplier.get();
        else             return obj;
    }

    /**
     * Check assignable.
     *
     * @param field the field
     * @param clazz the clazz
     */
    public static void checkAssignable(Field field, Class<?> clazz) {
        Class<?> type = field.getType();
        if (!clazz.isAssignableFrom(type)) {
            throw new TypeJudgmentException(
                "type of field [" + field + "] is [" + field.getType() + "], it is not assignable from [" + clazz + "]");
        }
    }

    /**
     * The type Struct cache.
     */
    static final class StructCache {

        static final Set<Field> TRANSIENT_FIELD_CACHE = new ConcurrentHashSet<>(512);

        /* reflection cache */
        static final Map<Field, Method> FIELD_READER_CACHE = new ConcurrentHashMap<>(512);
        /**
         * The Field write method cache.
         */
        static final Map<Field, Method> FIELD_WRITER_CACHE = new ConcurrentHashMap<>(512);
        /**
         * The Struct fields cache.
         */
        static final Map<Class<?>, Field[]> STRUCT_FIELDS_CACHE = new ConcurrentHashMap<>(512);
        /**
         * The Annotation cache.
         */
        static final Map<AnnotatedElement, Set<Annotation>> ANNOTATED_ELEMENT_ANNOTATION_CACHE = new ConcurrentHashMap<>(512);

        /**
         * The constant BASIC_CONSTRUCTOR_CACHE.
         */
        /* constructor cache */
        static final Map<Class<? extends Basic<?>>, Constructor<? extends Basic<?>>> BASIC_BUF_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(
            512);
        /**
         * The Struct constructor cache.
         */
        static final Map<Class<?>, Constructor<?>> STRUCT_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(512);
        /**
         * The Handler constructor cache.
         */
        static final Map<Class<? extends PropertyHandler<? extends Annotation>>, Constructor<? extends PropertyHandler<? extends Annotation>>> HANDLER_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(
            512);

        /**
         * The constant ANNOTATION_HANDLER_MAPPING_CACHE.
         */
        /* mapping handler and annotation */
        static final Map<Class<? extends Annotation>, Class<? extends PropertyHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new ConcurrentHashMap<>(32);

        static {
            try {
                scanHandlers();
                scanBasics();
                scanStructs();
            } catch (Exception e) {
                throw new NotInitedException("init nettyx serializer cache failed please check", e);
            }
        }

        private static final String ALL_PACKAGE = "";

        private static synchronized void scanHandlers() {
            Set<Class<?>> handlerClasses = ClassScanner.scanPackageBySuper(ALL_PACKAGE, PropertyHandler.class);

            for (Class<?> handlerClass : handlerClasses) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(handlerClass);
                if (targetAnnotationType != null) {
                    ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(targetAnnotationType,
                        (Class<? extends PropertyHandler<? extends Annotation>>) handlerClass);

                    HANDLER_CONSTRUCTOR_CACHE.putIfAbsent(
                        (Class<? extends PropertyHandler<? extends Annotation>>) handlerClass,
                        StructUtils.getHandlerConstructor(
                            ((Class<? extends PropertyHandler<? extends Annotation>>) handlerClass)));
                }
            }
        }

        private static synchronized void scanBasics() throws NoSuchMethodException {
            Set<Class<?>> basicClasses = ClassScanner.scanPackageBySuper(ALL_PACKAGE, Basic.class);
            for (Class<?> basicClass : basicClasses) {
                if (Modifier.isAbstract(basicClass.getModifiers())) continue;
                Constructor<? extends Basic<?>> basicConstructor = (Constructor<? extends Basic<?>>) basicClass.getConstructor(ByteBuf.class);
                BASIC_BUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) basicClass, basicConstructor);
            }
        }

        private static synchronized void scanStructs() throws NoSuchMethodException, IntrospectionException {
            Set<Class<?>> structClasses = ClassScanner.scanAllPackageByAnnotation(ALL_PACKAGE, Struct.class);
            for (Class<?> structClass : structClasses) {
                STRUCT_CONSTRUCTOR_CACHE.putIfAbsent(structClass, structClass.getConstructor());

                Field[] structFields = StructUtils.getStructFields(structClass);

                for (Field field : structFields) {
                    if (Modifier.isTransient(field.getModifiers())) TRANSIENT_FIELD_CACHE.add(field);

                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), structClass);
                    FIELD_READER_CACHE.putIfAbsent(field, propertyDescriptor .getReadMethod());
                    FIELD_WRITER_CACHE.put(field, propertyDescriptor.getWriteMethod());

                    StructUtils.allAnnotations(field);
                }
            }
        }

        private StructCache() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}
