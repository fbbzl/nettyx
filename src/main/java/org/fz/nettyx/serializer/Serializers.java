package org.fz.nettyx.serializer;

import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.serializer.ByteBufHandler.isReadHandler;
import static org.fz.nettyx.serializer.ByteBufHandler.isWriteHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.fz.nettyx.annotation.FieldHandler;
import org.fz.nettyx.annotation.Ignore;
import org.fz.nettyx.annotation.Length;
import org.fz.nettyx.annotation.Struct;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.type.Basic;

/**
 * the util for {@link TypedSerializer}
 *
 * @author fengbinbin
 * @since 2021-10-20 20:13
 **/

@SuppressWarnings("unchecked")
public final class Serializers {

    private Serializers() {
        throw new UnsupportedOperationException();
    }

    //******************************************      public start     ***********************************************//

    public static final Field[] EMPTY_FIELD_ARRAY = {};

    public static void writeField(Object object, Field field, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), object.getClass());
            propertyDescriptor.getWriteMethod().invoke(object, value);
        }
        catch (Exception exception) {
            throw new UnsupportedOperationException("field write failed, field is [" + field + "], value is [" + value + "], check the correct parameter type of field getter/setter", exception);
        }
    }

    public static <T> T readField(Object object, Field field) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), object.getClass());
            return (T) propertyDescriptor.getReadMethod().invoke(object);
        }
        catch (Exception exception) {
            throw new UnsupportedOperationException("field read failed, field is [" + field + "], check parameter type or field getter/setter", exception);
        }
    }

    public static Field[] getInstantiateFields(Class<?> clazz) {
        Predicate<Field> notStatic = f -> !Modifier.isStatic(f.getModifiers());
        return getAllFieldsList(clazz).stream().filter(notStatic).toArray(Field[]::new);
    }

    public static List<Field> getInstantiateFieldList(Class<?> clazz) {
        Predicate<Field> notStatic = f -> !Modifier.isStatic(f.getModifiers());
        return getAllFieldsList(clazz).stream().filter(notStatic).collect(toList());
    }

    public static Field[] getAllFields(Class<?> clazz) {
        return getAllFieldsList(clazz).toArray(EMPTY_FIELD_ARRAY);
    }

    public static List<Field> getAllFieldsList(Class<?> clazz) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && isStruct(currentClass)) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static <T extends Basic<?>> T newBasicInstance(Field basicField, ByteBuf buf) {
        return newBasicInstance((Class<T>) basicField.getType(), buf);
    }

    public static <T> T newBasicInstance(Class<T> basicClass, ByteBuf buf) {
        try {
            if (isBasic(basicClass)) return basicClass.getConstructor(ByteBuf.class).newInstance(buf);
            else                     throw new UnsupportedOperationException("can not create instance of basic type [" + basicClass + "], its not a Basic type");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException(
                "basic class [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]");
        }
    }

    public static <T> T newStructInstance(Field structField) {
        return newStructInstance((Class<T>) structField.getType());
    }

    public static <T> T newStructInstance(Class<T> structClass) {
        try {
            if (isStruct(structClass)) return structClass.getConstructor().newInstance();
            else                       throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException("struct class [" + structClass + "] instantiate failed...");
        }
    }

    public static <T> T newHandlerInstance(Class<T> handlerClass) {
        try {
            return handlerClass.getConstructor().newInstance();
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException("struct class [" + handlerClass + "] instantiate failed...");
        }
    }

    public static <T> T[] newArrayInstance(Field arrayField) {
        return (T[]) Array.newInstance(arrayField.getType().getComponentType(), getArrayLength(arrayField));
    }

    public static int getArrayLength(Field arrayField) {
        Function<Field, Integer> cacheArrayLength = fieldKey -> {
            Length length = fieldKey.getAnnotation(Length.class);

            if (length == null) throw new SerializeException("read array [" + arrayField + "] error, must use @" + Length.class.getSimpleName() + " to assign array arrayLength");

            return length.value();
        };
        return ARRAY_LENGTH_CACHE.computeIfAbsent(arrayField, cacheArrayLength);
    }

    /**
     * Fill array object [ ].
     *
     * @param arrayValue  the array value
     * @param elementType the element type
     * @param length      the length
     * @return the object [ ]
     */
    public static Object[] fillArray(Object[] arrayValue, Class<?> elementType, int length) {
        Object[] filledArray = (Object[]) Array.newInstance(elementType, length);
        System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
        return filledArray;
    }

    public static <T> boolean isBasic(T object) {
        return isBasic(object.getClass());
    }

    public static boolean isBasic(Field field) {
        return isBasic(field.getType());
    }

    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public static <T> boolean isStruct(T object) {
        return isStruct(object.getClass());
    }

    public static boolean isStruct(Field field) {
        return isStruct(field.getType());
    }

    public static boolean isStruct(Class<?> clazz) {
        return clazz.isAnnotationPresent(Struct.class);
    }

    public static <T> boolean isArray(T object) {
        return isArray(object.getClass());
    }

    public static boolean isArray(Field field) {
        return field.getType().isArray();
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isIgnore(Field field) {
        return field.isAnnotationPresent(Ignore.class);
    }

    public static boolean isReadHandleable(Field field) {
        FieldHandler annotation = field.getAnnotation(FieldHandler.class);
        return annotation != null && isReadHandler(annotation.value());
    }

    public static boolean isWriteHandleable(Field field) {
        FieldHandler annotation = field.getAnnotation(FieldHandler.class);
        return annotation != null && isWriteHandler(annotation.value());
    }

    //******************************************      public end       ***********************************************//

    private static final Map<Field, Integer> ARRAY_LENGTH_CACHE = new ConcurrentHashMap<>(256);
}
