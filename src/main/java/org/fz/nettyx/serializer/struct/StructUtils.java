package org.fz.nettyx.serializer.struct;

import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.ARRAY_LENGTH_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.BASIC_CONSTRUCTOR_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.BYTEBUFFER_HANDLER_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_READ_METHOD_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.FIELD_WRITE_METHOD_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.STRUCT_CONSTRUCTOR_CACHE;
import static org.fz.nettyx.serializer.struct.StructUtils.StructCache.STRUCT_FIELDS_CACHE;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Length;
import org.fz.nettyx.serializer.struct.annotation.PropertyHandler;

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

    public <A extends Annotation> boolean isAnnotationPresent(AnnotatedElement element, Class<A> annoType) {
        return findAnnotation(element, annoType) != null;
    }

    /**
     * will find the annotation {@link PropertyHandler} and will also search the upper 1-level meta annotation
     * @see PropertyHandler
     */
    public PropertyHandler findPropertyHandlerAnnotation(AnnotatedElement element) {
        for (Annotation annotation : allAnnotations(element)) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            PropertyHandler meta;
            // first use handler
            if (annotationType.equals(PropertyHandler.class)) return (PropertyHandler) annotation;
            else
            // and also find in meta annotation 1 upper-level
            if ((meta = findAnnotation(annotationType, PropertyHandler.class)) != null) return meta;
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
        return (Collection<A>) StructCache.ANNOTATION_CACHE.computeIfAbsent(element, e -> Arrays.asList(e.getAnnotations()));
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

    public <A extends Annotation> A[] findAnnotations(AnnotatedElement element, Class<A> clazz) {
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
        Annotation[] annotations = filterAnnotations(element, filter);
        if (annotations.length > 1) {
            throw new UnsupportedOperationException(
                "keep [" + element + "] only have one serializer handler annotation, annotation now are ["
                    + Arrays.toString(annotations) + "]");
        }
        return (A) annotations[0];
    }

    /**
     * Filter annotations a [ ].
     *
     * @param <A> the type parameter
     * @param annotatedElement the annotated element
     * @param filter the filter
     * @return the a [ ]
     */
    public <A extends Annotation> A[] filterAnnotations(AnnotatedElement annotatedElement,
        Predicate<Annotation> filter) {
        return (A[]) allAnnotations(annotatedElement).stream().filter(filter).toArray();
    }

    /**
     * Gets serializer handler.
     *
     * @param element the element
     * @return the serializer handler
     */
    public <S extends SerializerHandler<?>> S getPropertySerializerHandler(AnnotatedElement element) {
        Supplier<SerializerHandler<?>> bufHandlerSupplier = BYTEBUFFER_HANDLER_CACHE.computeIfAbsent(element, e -> {
            PropertyHandler handlerAnnotation = findPropertyHandlerAnnotation(e);
            if (handlerAnnotation != null) {
                // if is singleton
                if (handlerAnnotation.isSingleton()) {
                    SerializerHandler<?> singleton = newSerializerHandler(handlerAnnotation.value());
                    return () -> singleton;
                }
                // if is not singleton will invoke constructor to create
                else return () -> newSerializerHandler(handlerAnnotation.value());
            }
            return null;
        });
        return bufHandlerSupplier != null ? (S) bufHandlerSupplier.get() : null;
    }

    /**
     * Gets handler constructor.
     *
     * @param <H> the type parameter
     * @param handlerClass the handler class
     * @return the handler constructor
     */
    public <H extends SerializerHandler<?>> Constructor<H> getPropertySerializerHandlerConstructor(Class<H> handlerClass) {
        return (Constructor<H>) StructCache.PROPERTY_SERIALIZE_HANDLER_CONSTRUCTOR_CACHE.computeIfAbsent(handlerClass, c -> {
            try {
                return c.getConstructor();
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find serializer handler [" + handlerClass + "] no-args constructor");
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
    public <C> Constructor<C> getStructConstructor(Class<C> structClass) {
        return (Constructor<C>) STRUCT_CONSTRUCTOR_CACHE.computeIfAbsent(structClass, c -> {
            try {
                return c.getConstructor();
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find struct [" + structClass + "] no-args constructor");
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
    public <B extends Basic<?>> Constructor<B> getBasicConstructor(Class<B> basicClass) {
        return (Constructor<B>) BASIC_CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, c -> {
            try {
                return c.getConstructor(ByteBuf.class);
            } catch (NoSuchMethodException exception) {
                throw new UnsupportedOperationException(
                    "can not find basic constructor with arg [" + ByteBuf.class + "]");
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
    public static <T extends SerializerHandler<?>> T newSerializerHandler(Class<T> clazz) {
        try {
            return getPropertySerializerHandlerConstructor(clazz).newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("serializer handler [" + clazz + "] instantiate failed...");
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
    public static <T extends Basic<?>> T newBasic(Field basicField, ByteBuf buf) {
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
                "basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]");
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
            throw new SerializeException("struct [" + structClass + "] instantiate failed...");
        }
    }

    /**
     * New array instance t [ ].
     *
     * @param <T> the type parameter
     * @param arrayField the array field
     * @return the t [ ]
     */
    public static <T> T[] newArray(Field arrayField) {
        return (T[]) Array.newInstance(arrayField.getType().getComponentType(), getArrayLength(arrayField));
    }

    /**
     * New array instance t [ ].
     *
     * @param <T> the type parameter
     * @param componentType the component type
     * @param length the length
     * @return the t [ ]
     */
    public static <T> T[] newArray(Class<?> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
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
     * @param value the value
     */
    public static void writeField(Object object, Field field, Object value) {
        try {
            Method writeMethod = FIELD_WRITE_METHOD_CACHE.computeIfAbsent(field, f -> {
                try {
                    return new PropertyDescriptor(field.getName(), object.getClass()).getWriteMethod();
                } catch (IntrospectionException exception) {
                    throw new UnsupportedOperationException(
                        "field write failed, field is [" + field + "], value is [" + value
                            + "], check the correct parameter type of field getter/setter", exception);
                }
            });
            writeMethod.invoke(object, value);
        } catch (Exception exception) {
            throw new UnsupportedOperationException("field write failed, field is [" + field + "], value is [" + value
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
            Method readMethod = FIELD_READ_METHOD_CACHE.computeIfAbsent(field, f -> {
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
     * Gets array length.
     *
     * @param arrayField the array field
     * @return the array length
     */
    public static int getArrayLength(Field arrayField) {
        Function<Field, Integer> cacheArrayLength = field -> {
            Length length = field.getAnnotation(Length.class);

            if (length == null) throw new SerializeException("read array [" + arrayField + "] error, must use @" + Length.class.getSimpleName() + " to assign array arrayLength");

            return length.value();
        };
        return ARRAY_LENGTH_CACHE.computeIfAbsent(arrayField, cacheArrayLength);
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

    /**
     * Check assignable.
     *
     * @param field the field
     * @param clazz the clazz
     */
    public static void checkAssignable(Field field, Class<?> clazz) {
        Class<?> type = field.getType();
        if (clazz.isAssignableFrom(type))
            throw new TypeJudgmentException("field [" + field + "] is not assignable from [" + clazz + "]");
    }

    static final class StructCache {

        /* reflection cache */
        static final Map<Field, Method> FIELD_READ_METHOD_CACHE = new ConcurrentHashMap<>(512);
        static final Map<Field, Method> FIELD_WRITE_METHOD_CACHE = new ConcurrentHashMap<>(512);
        static final Map<Class<?>, Field[]> STRUCT_FIELDS_CACHE = new ConcurrentHashMap<>(512);
        static final Map<Field, Integer> ARRAY_LENGTH_CACHE = new ConcurrentHashMap<>(512);
        static final Map<AnnotatedElement, List<Annotation>> ANNOTATION_CACHE = new ConcurrentHashMap<>(512);
        static final Map<AnnotatedElement, Supplier<SerializerHandler<?>>> BYTEBUFFER_HANDLER_CACHE = new ConcurrentHashMap<>(
            512);

        /* constructor cache */
        static final Map<Class<? extends Basic<?>>, Constructor<? extends Basic<?>>> BASIC_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(
            512);
        static final Map<Class<?>, Constructor<?>> STRUCT_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(512);
        static final Map<Class<? extends SerializerHandler<?>>, Constructor<? extends SerializerHandler<?>>> PROPERTY_SERIALIZE_HANDLER_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(
            512);

        static {
            final String allPackage = "";
            Set<Class<?>> serializerHandlerClasses = ClassScanner.scanPackageBySuper(allPackage, SerializerHandler.class);
            for (Class<?> handlerClass : serializerHandlerClasses) {
                ParameterizedType[] generics = TypeUtil.getGenerics(handlerClass);

                // skip the handler without generics
                if (generics.length == 0) continue;

                ParameterizedType generic = generics[0];

                Type anno = generic.getActualTypeArguments()[0];

                //TODO
                System.err.println(anno.getClass());
                System.err.println(":::"+anno);
                System.err.println();
            }
        }


        public static void main(String[] args) {
            System.err.println(1);
        }
        private StructCache() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}
