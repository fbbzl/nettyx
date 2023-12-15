package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.typed.annotation.FieldHandler;
import org.fz.nettyx.serializer.typed.annotation.Ignore;
import org.fz.nettyx.serializer.typed.annotation.Length;
import org.fz.nettyx.serializer.typed.annotation.Struct;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.fz.nettyx.serializer.typed.ByteBufHandler.isReadHandler;
import static org.fz.nettyx.serializer.typed.ByteBufHandler.isWriteHandler;

/**
 * the util for {@link TypedSerializer}
 *
 * @author fengbinbin
 * @since 2021 -10-20 20:13
 */
@SuppressWarnings("unchecked")
public final class Serializers {

    private Serializers() {
        throw new UnsupportedOperationException();
    }

    //******************************************      public start     ***********************************************//

    public static <T> T nullDefault(T obj, Supplier<T> defSupplier) {
        if (obj == null) return defSupplier.get();
        else             return obj;
    }

    /**
     * New basic instance t.
     *
     * @param <T> the type parameter
     * @param basicField the basic field
     * @param buf the buf
     * @return the t
     */
    public static <T extends Basic<?>> T newBasicInstance(Field basicField, ByteBuf buf) {
        return newBasicInstance((Class<T>) basicField.getType(), buf);
    }

    /**
     * New basic instance t.
     *
     * @param <T> the type parameter
     * @param basicClass the basic class
     * @param buf the buf
     * @return the t
     */
    public static <T extends Basic<?>> T newBasicInstance(Class<T> basicClass, ByteBuf buf) {
        try {
            if (isBasic(basicClass)) return basicClass.getConstructor(ByteBuf.class).newInstance(buf);
            else                     throw new UnsupportedOperationException("can not create instance of basic type [" + basicClass + "], its not a Basic type");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException(
                "basic class [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]");
        }
    }

    /**
     * New struct instance t.
     *
     * @param <T> the type parameter
     * @param structField the struct field
     * @return the t
     */
    public static <T> T newStructInstance(Field structField) {
        return newStructInstance((Class<T>) structField.getType());
    }

    /**
     * New struct instance t.
     *
     * @param <T> the type parameter
     * @param structClass the struct class
     * @return the t
     */
    public static <T> T newStructInstance(Class<T> structClass) {
        try {
            if (isStruct(structClass)) return structClass.getConstructor().newInstance();
            else                       throw new UnsupportedOperationException("can not create instance of type [" + structClass + "], can not find @Struct annotation on class");
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException("struct class [" + structClass + "] instantiate failed...");
        }
    }

    /**
     * New handler instance t.
     *
     * @param <T> the type parameter
     * @param handlerClass the handler class
     * @return the t
     */
    public static <T> T newHandlerInstance(Class<T> handlerClass) {
        try {
            return handlerClass.getConstructor().newInstance();
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
            throw new SerializeException("struct class [" + handlerClass + "] instantiate failed...");
        }
    }

    /**
     * New array instance t [ ].
     *
     * @param <T> the type parameter
     * @param arrayField the array field
     * @return the t [ ]
     */
    public static <T> T[] newArrayInstance(Field arrayField) {
        return (T[]) Array.newInstance(arrayField.getType().getComponentType(), getArrayLength(arrayField));
    }

    public static <T> T[] newArrayInstance(Class<?> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
    }

    /**
     * Gets array length.
     *
     * @param arrayField the array field
     * @return the array length
     */
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
     * @param arrayValue the array value
     * @param elementType the element type
     * @param length the length
     * @return the object [ ]
     */
    public static <T> T[] fillArray(T[] arrayValue, Class<?> elementType, int length) {
        T[] filledArray = (T[]) Array.newInstance(elementType, length);
        System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
        return filledArray;
    }

    /**
     * Is basic boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isBasic(T object) {
        return isBasic(object.getClass());
    }

    /**
     * Is basic boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isBasic(Field field) {
        return isBasic(field.getType());
    }

    /**
     * Is basic boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    /**
     * Is struct boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isStruct(T object) {
        return isStruct(object.getClass());
    }

    /**
     * Is struct boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isStruct(Field field) {
        return isStruct(field.getType());
    }

    /**
     * Is struct boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isStruct(Class<?> clazz) {
        return clazz.isAnnotationPresent(Struct.class);
    }

    /**
     * Is array boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isArray(T object) {
        return isArray(object.getClass());
    }

    /**
     * Is array boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isArray(Field field) {
        return field.getType().isArray();
    }

    /**
     * Is array boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * Is ignore boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isIgnore(Field field) {
        return field.isAnnotationPresent(Ignore.class);
    }

    /**
     * Is read handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useReadHandler(Field field) {
        FieldHandler annotation = field.getAnnotation(FieldHandler.class);
        return annotation != null && isReadHandler(annotation.value());
    }

    /**
     * Is write handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useWriteHandler(Field field) {
        FieldHandler annotation = field.getAnnotation(FieldHandler.class);
        return annotation != null && isWriteHandler(annotation.value());
    }

    //******************************************      public end       ***********************************************//

    private static final Map<Field, Integer> ARRAY_LENGTH_CACHE = new ConcurrentHashMap<>(256);
}
