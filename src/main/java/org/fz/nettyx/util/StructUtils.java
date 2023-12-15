package org.fz.nettyx.util;

import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.TypeJudgmentException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.fz.nettyx.serializer.typed.Serializers.isStruct;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/26 9:28
 */
@UtilityClass
public class StructUtils {

    Predicate<Field> isStatic = f -> Modifier.isStatic(f.getModifiers());

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
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), object.getClass());
            propertyDescriptor.getWriteMethod().invoke(object, value);
        }
        catch (Exception exception) {
            throw new UnsupportedOperationException("field write failed, field is [" + field + "], value is [" + value + "], check the correct parameter type of field getter/setter", exception);
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
    @SuppressWarnings("unchecked")
    public static <T> T readField(Object object, Field field) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), object.getClass());
            return (T) propertyDescriptor.getReadMethod().invoke(object);
        }
        catch (Exception exception) {
            throw new UnsupportedOperationException("field read failed, field is [" + field + "], check parameter type or field getter/setter", exception);
        }
    }

    /**
     * Get all fields field [ ].
     *
     * @param clazz the clazz
     * @return the field [ ]
     */
    public static Field[] getStructFields(Class<?> clazz) {
        return getStructFieldList(clazz).toArray(EMPTY_FIELD_ARRAY);
    }

    /**
     * Gets all fields list.
     *
     * @param clazz the clazz
     * @return the all fields list
     */
    public static List<Field> getStructFieldList(Class<?> clazz) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && isStruct(currentClass)) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        // remove static fields
        allFields.removeIf(isStatic);
        return allFields;
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

    public static void checkAssignable(Field field, Class<?> clazz) {
        Class<?> type = field.getType();
        if (clazz.isAssignableFrom(type))
            throw new TypeJudgmentException("field [" + field + "] is not assignable from [" + clazz + "]");
    }

}
