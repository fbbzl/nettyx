package org.fz.nettyx.serializer;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import org.fz.nettyx.annotation.FieldHandler;
import org.fz.nettyx.annotation.Ignore;
import org.fz.nettyx.annotation.Length;
import org.fz.nettyx.annotation.Struct;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.typed.Basic;
import org.fz.nettyx.serializer.typed.TypedByteBufSerializer;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.fz.nettyx.handler.ByteBufHandler.isReadHandler;
import static org.fz.nettyx.handler.ByteBufHandler.isWriteHandler;
import static org.fz.nettyx.serializer.typed.Basic.BasicTypeFeature.BASIC_FEATURE_CACHE;
import static org.fz.nettyx.serializer.typed.Basic.BasicTypeFeature.STRUCT_FEATURE_CACHE;


/**
 * the util for {@link TypedByteBufSerializer}
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

    public static <T extends Basic<?>> T createBasic(Field basicField) {
        return createBasic((Class<T>) basicField.getType());
    }

    public static <T extends Basic<?>> T createBasic(Class<T> basicType) {
        if (isBasic(basicType)) {
            return newInstance(basicType);
        }
        else throw new UnsupportedOperationException("can not create instance of type [" + basicType + "], its not Basic type");
    }

    public static <T> T createStruct(Field objectField) {
        return createStruct((Class<T>) objectField.getType());
    }

    public static <T> T createStruct(Class<T> objectType) {
        if (isStruct(objectType)) {
            return newInstance(objectType);
        }
        else throw new UnsupportedOperationException("can not create instance of type [" + objectType + "], its not Object type");
    }

    public static <T> T[] createArray(Field arrayField) {
        return (T[]) Array.newInstance(arrayField.getType().getComponentType(), getLength(arrayField));
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        }
        catch (NoSuchMethodException exception) {
            // will use unsafe to force create instance
            return unsafeInstantiate(clazz);
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            throw new SerializeException("class [" + clazz + "] instantiate failed...");
        }
    }

    public static <T> T unsafeInstantiate(Class<T> clazz) {
        try {
            return (T) UnsafeAccess.UNSAFE.allocateInstance(clazz);
        }
        catch (InstantiationException instantiationException) {
            throw new SerializeException("class [" + clazz + "] instantiate failed...");
        }
    }

    public static int getLength(Field arrayField) {
        Length length = arrayField.getAnnotation(Length.class);
        if (length == null) {
            throw new SerializeException("read array [" + arrayField + "] error, must use @" + Length.class.getSimpleName() + " to assign array arrayLength");
        }

        return length.value();
    }

    public static int sizeOf(Field field) {
        if (isBasic(field))  { return basicSize(field);  }
        if (isStruct(field)) { return structSize(field); }
        if (isArray(field))  { return arraySize(field);  }
        throw new UnsupportedOperationException("un-support field [" + field + "]");
    }

    public static <B extends Basic<?>> int basicSize(B basic) {
        return basicSize(basic.getClass());
    }
    public static int basicSize(Field field) {
        return basicSize((Class<? extends Basic<?>>) field.getType());
    }
    public static <B extends Basic<?>> int basicSize(Class<B> clazz) {
        Function<Class<?>, Basic.BasicTypeFeature> newFeature = key -> new Basic.BasicTypeFeature((newInstance(clazz)).size());
        return BASIC_FEATURE_CACHE.computeIfAbsent(clazz, newFeature).getSize();
    }

    public static <T> int structSize(T object) {
        if (object == null) throw new IllegalArgumentException("can not read struct size by null value");
        return structSize(object.getClass());
    }
    public static int structSize(Field field) { return structSize(field.getType()); }
    public static int structSize(Class<?> clazz) {
        Function<Class<?>, Basic.BasicTypeFeature> newFeature = key -> {
            int size = 0;
            for (Field field : getInstantiateFields(clazz)) {
                if (isBasic(field))  { size += basicSize(field);  }
                else
                if (isStruct(field)) { size += structSize(field); }
                else
                if (isArray(field))  { size += arraySize(field);  }
            }
            return new Basic.BasicTypeFeature(size);
        };

        return STRUCT_FEATURE_CACHE.computeIfAbsent(clazz, newFeature).getSize();
    }

    public static int arraySize(Field arrayField) {
        final Class<?> elementType = arrayField.getType().getComponentType();

        int elementSize = 0;
        if (isBasic(elementType))  { elementSize = basicSize((Class<? extends Basic<?>>) elementType);  }
        else
        if (isStruct(elementType)) { elementSize = structSize(elementType);                             }

        return getLength(arrayField) * elementSize;
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
}
