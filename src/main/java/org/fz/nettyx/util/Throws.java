package org.fz.nettyx.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Exception tool class All static methods are composed of an expression and an exceptionMessage. When the expression
 * is established, an exception will be
 * thrown with the specified exceptionMessage
 *
 * @author fengbinbin
 * @since 2017/4/2/038 11:52
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Throws {

    static final String MAP_NULL        = "map can not be null";
    static final String KEY_NULL        = "key can not be null";
    static final String VALUE_NULL      = "value can not be null";
    static final String COLLECTION_NULL = "collection can not be null";
    static final String ELEMENT_NULL    = "element can not be null";
    static final String ARRAY_NULL      = "array can not be null";
    static final String NUMBER_NULL     = "bigDecimal can not be null";

    public static void ifTrue(Object expression, RuntimeException runtimeException) {
        if (Objects.equals(expression, Boolean.TRUE)) throw runtimeException;
    }

    public static void ifTrue(Object expression, String exceptionMessage) {
        ifTrue(expression, new RuntimeException(exceptionMessage));
    }

    public static void ifFalse(Object expression, RuntimeException runtimeException) {
        if (Objects.equals(expression, Boolean.FALSE)) throw runtimeException;
    }

    public static void ifFalse(Object expression, String exceptionMessage) {
        ifFalse(expression, new RuntimeException(exceptionMessage));
    }

    public static void ifNull(Object object, RuntimeException runtimeException) {
        if (object == null) throw runtimeException;
    }

    public static void ifNull(Object object, String exceptionMessage) {
        ifNull(object, new RuntimeException(exceptionMessage));
    }

    public static void ifNotNull(Object object, RuntimeException runtimeException) {
        if (object != null) throw runtimeException;
    }

    public static void ifNotNull(Object object, String exceptionMessage) {
        ifNotNull(object, new RuntimeException(exceptionMessage));
    }

    public static void ifEmpty(Object[] array, RuntimeException runtimeException) {
        if (array == null || array.length == 0) throw runtimeException;
    }

    public static void ifEmpty(Object[] array, String exceptionMessage) {
        ifEmpty(array, new RuntimeException(exceptionMessage));
    }

    public static void ifNotEmpty(Object[] array, RuntimeException runtimeException) {
        if (array != null && array.length > 0) throw runtimeException;
    }

    public static void ifNotEmpty(Object[] array, String exceptionMessage) {
        ifNotEmpty(array, new RuntimeException(exceptionMessage));
    }

    public static void ifEmpty(Collection<?> collection, RuntimeException runtimeException) {
        if (collection == null || collection.isEmpty()) throw runtimeException;
    }

    public static void ifEmpty(Collection<?> collection, String exceptionMessage) {
        ifEmpty(collection, new RuntimeException(exceptionMessage));
    }

    public static void ifEmpty(Map<?, ?> map, RuntimeException runtimeException) {
        if (map == null || map.isEmpty()) throw runtimeException;
    }

    public static void ifEmpty(Map<?, ?> map, String exceptionMessage) {
        ifEmpty(map, new RuntimeException(exceptionMessage));
    }

    public static void ifNotEmpty(Collection<?> collection, RuntimeException runtimeException) {
        if (collection != null && !collection.isEmpty()) throw runtimeException;
    }

    public static void ifNotEmpty(Collection<?> collection, String exceptionMessage) {
        ifNotEmpty(collection, new RuntimeException(exceptionMessage));
    }

    public static void ifNotEmpty(Map<?, ?> map, RuntimeException runtimeException) {
        if (map != null && !map.isEmpty()) throw runtimeException;
    }

    public static void ifNotEmpty(Map<?, ?> map, String exceptionMessage) {
        ifNotEmpty(map, new RuntimeException(exceptionMessage));
    }

    public static void ifEmpty(String text, RuntimeException runtimeException) {
        if (text == null || text.isEmpty()) throw runtimeException;
    }

    public static void ifEmpty(String text, String exceptionMessage) {
        ifEmpty(text, new RuntimeException(exceptionMessage));
    }

    public static void ifNotEmpty(String text, RuntimeException runtimeException) {
        if (text != null && !text.isEmpty()) throw runtimeException;
    }

    public static void ifNotEmpty(String text, String exceptionMessage) {
        ifNotEmpty(text, new RuntimeException(exceptionMessage));
    }

    public static void ifBlank(String text, RuntimeException runtimeException) {
        if (text == null || text.trim().isEmpty()) throw runtimeException;
    }

    public static void ifBlank(String text, String exceptionMessage) {
        ifBlank(text, new RuntimeException(exceptionMessage));
    }

    public static void ifNotBlank(String text, RuntimeException runtimeException) {
        if (text != null && !text.trim().isEmpty()) throw runtimeException;
    }

    public static void ifNotBlank(String text, String exceptionMessage) {
        ifNotBlank(text, new RuntimeException(exceptionMessage));
    }

    public static void ifEquals(Object l, Object r, RuntimeException runtimeException) {
        if (Objects.equals(l, r)) throw runtimeException;
    }

    public static void ifEquals(Object l, Object r, String exceptionMessage) {
        ifEquals(l, r, new RuntimeException(exceptionMessage));
    }

    public static void ifNotEquals(Object l, Object r, RuntimeException runtimeException) {
        if (!Objects.equals(l, r)) throw runtimeException;
    }

    public static void ifNotEquals(Object l, Object r, String exceptionMessage) {
        ifNotEquals(l, r, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifContains(Collection<T> collection, T element, RuntimeException runtimeException) {
        ifNull(collection, COLLECTION_NULL);
        ifNull(element, ELEMENT_NULL);

        if (collection.contains(element)) throw runtimeException;
    }

    public static <T> void ifContains(Collection<T> collection, T element, String exceptionMessage) {
        ifContains(collection, element, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifNotContains(Collection<T> collection, T element, RuntimeException runtimeException) {
        ifNull(collection, COLLECTION_NULL);
        ifNull(element, ELEMENT_NULL);

        if (!collection.contains(element)) throw runtimeException;
    }

    public static <T> void ifNotContains(Collection<T> collection, T element, String exceptionMessage) {
        ifNotContains(collection, element, new RuntimeException(exceptionMessage));
    }

    public static void ifContains(CharSequence origin, CharSequence target, String exceptionMessage) {
        ifContains(origin, target, new RuntimeException(exceptionMessage));
    }

    public static void ifContains(CharSequence origin, CharSequence target, RuntimeException exception) {
        if (origin == null || target == null || origin.toString().contains(target)) throw exception;
    }

    public static void ifNotContains(CharSequence origin, CharSequence target, String exceptionMessage) {
        ifNotContains(origin, target, new RuntimeException(exceptionMessage));
    }

    public static void ifNotContains(CharSequence origin, CharSequence target, RuntimeException exception) {
        if (origin == null || target == null || !origin.toString().contains(target)) throw exception;
    }

    public static <K, V> void ifContainsKey(Map<K, V> map, K key, RuntimeException runtimeException) {
        ifNull(map, MAP_NULL);
        ifNull(key, KEY_NULL);

        if (map.containsKey(key)) throw runtimeException;
    }

    public static <K, V> void ifContainsKey(Map<K, V> map, K key, String exceptionMessage) {
        ifContainsKey(map, key, new RuntimeException(exceptionMessage));
    }

    public static <K, V> void ifNotContainsKey(Map<K, V> map, K key, RuntimeException runtimeException) {
        ifNull(map, MAP_NULL);
        ifNull(key, KEY_NULL);

        if (!map.containsKey(key)) throw runtimeException;
    }

    public static <K, V> void ifNotContainsKey(Map<K, V> map, K key, String exceptionMessage) {
        ifNotContainsKey(map, key, new RuntimeException(exceptionMessage));
    }

    public static <K, V> void ifContainsValue(Map<K, V> map, V value, RuntimeException runtimeException) {
        ifNull(map, MAP_NULL);
        ifNull(value, VALUE_NULL);

        if (map.containsValue(value)) throw runtimeException;
    }

    public static <K, V> void ifContainsValue(Map<K, V> map, V value, String exceptionMessage) {
        ifContainsValue(map, value, new RuntimeException(exceptionMessage));
    }

    public static <K, V> void ifNotContainsValue(Map<K, V> map, V value, RuntimeException runtimeException) {
        ifNull(map, MAP_NULL);
        ifNull(value, VALUE_NULL);

        if (!map.containsValue(value)) throw runtimeException;
    }

    public static <K, V> void ifNotContainsValue(Map<K, V> map, V value, String exceptionMessage) {
        ifNotContainsValue(map, value, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifInstanceOf(Class<?> type, T object, RuntimeException runtimeException) {
        Throws.ifNull(type, "type can not be null");
        Throws.ifNull(object, "instanced object can not be null");

        if (type.isInstance(object)) throw runtimeException;
    }

    public static <T> void ifInstanceOf(Class<?> type, T object, String exceptionMessage) {
        ifInstanceOf(type, object, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifNotInstanceOf(Class<?> type, T object, RuntimeException runtimeException) {
        Throws.ifNull(type, "type can not be null");
        Throws.ifNull(object, "instanced object can not be null");

        if (!type.isInstance(object)) throw runtimeException;
    }

    public static <T> void ifNotInstanceOf(Class<?> type, T object, String exceptionMessage) {
        ifNotInstanceOf(type, object, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifHasNullElement(Collection<T> collection, RuntimeException runtimeException) {
        Throws.ifNull(collection, COLLECTION_NULL);

        for (T t : collection) Throws.ifNull(t, runtimeException);
    }

    public static <T> void ifHasNullElement(Collection<T> collection, String exceptionMessage) {
        ifHasNullElement(collection, new RuntimeException(exceptionMessage));
    }

    public static <T> void ifHasNullElement(T[] array, RuntimeException runtimeException) {
        Throws.ifNull(array, ARRAY_NULL);

        for (T t : array) Throws.ifNull(t, runtimeException);
    }

    public static <T> void ifHasNullElement(T[] array, String exceptionMessage) {
        ifHasNullElement(array, new RuntimeException(exceptionMessage));
    }

    public static void ifBetweenClose(long current, long min, long max, String exceptionMessage) {
        ifBetweenClose(current, min, max, new RuntimeException(exceptionMessage));
    }

    public static void ifBetweenClose(long current, long min, long max, RuntimeException exception) {
        if (current >= min && current <= max) throw exception;
    }

    public static void ifNotBetweenClose(long current, long min, long max, String exceptionMessage) {
        ifNotBetweenClose(current, min, max, new RuntimeException(exceptionMessage));
    }

    public static void ifNotBetweenClose(long current, long min, long max, RuntimeException exception) {
        if (current <= min || current >= max) throw exception;
    }

    public static void ifBetweenOpen(long current, long min, long max, String exceptionMessage) {
        ifBetweenOpen(current, min, max, new RuntimeException(exceptionMessage));
    }

    public static void ifBetweenOpen(long current, long min, long max, RuntimeException exception) {
        if (current > min && current < max) throw exception;
    }

    public static void ifNotBetweenOpen(long current, long min, long max, String exceptionMessage) {
        ifNotBetweenOpen(current, min, max, new RuntimeException(exceptionMessage));
    }

    public static void ifNotBetweenOpen(long current, long min, long max, RuntimeException exception) {
        if (current < min || current > max) throw exception;
    }

    public static void ifAssignable(Class<?> superType, Class<?> subType, String exceptionMessage) {
        ifAssignable(superType, subType, new RuntimeException(exceptionMessage));
    }

    public static void ifAssignable(Class<?> superType, Class<?> subType, RuntimeException exception) {
        if (superType == null || subType == null || superType.isAssignableFrom(subType)) throw exception;
    }

    public static void ifNotAssignable(Class<?> superType, Class<?> subType, String exceptionMessage) {
        ifNotAssignable(superType, subType, new RuntimeException(exceptionMessage));
    }

    public static void ifNotAssignable(Class<?> superType, Class<?> subType, RuntimeException exception) {
        if (superType == null || subType == null || !superType.isAssignableFrom(subType)) throw exception;
    }

    public static void ifGreater(Number left, Number right, String exceptionMessage) {
        ifGreater(left.toString(), right.toString(), new RuntimeException(exceptionMessage));
    }

    public static void ifGreater(Number left, Number right, RuntimeException exception) {
        ifGreater(left.toString(), right.toString(), exception);
    }

    public static void ifGreater(String left, String right, String exceptionMessage) {
        ifGreater(new BigDecimal(left), new BigDecimal(right), new RuntimeException(exceptionMessage));
    }

    public static void ifGreater(String left, String right, RuntimeException exception) {
        ifGreater(new BigDecimal(left), new BigDecimal(right), exception);
    }

    public static void ifGreater(BigDecimal left, BigDecimal right, String exceptionMessage) {
        ifGreater(left, right, new RuntimeException(exceptionMessage));
    }

    public static void ifGreater(BigDecimal left, BigDecimal right, RuntimeException exception) {
        if (left == null || right == null) throw new IllegalArgumentException(NUMBER_NULL);
        if (left.compareTo(right) > 0) throw exception;
    }

    public static void ifGreaterEquals(Number left, Number right, String exceptionMessage) {
        ifGreaterEquals(left.toString(), right.toString(), new RuntimeException(exceptionMessage));
    }

    public static void ifGreaterEquals(Number left, Number right, RuntimeException exception) {
        ifGreaterEquals(left.toString(), right.toString(), exception);
    }

    public static void ifGreaterEquals(String left, String right, String exceptionMessage) {
        ifGreaterEquals(new BigDecimal(left), new BigDecimal(right), new RuntimeException(exceptionMessage));
    }

    public static void ifGreaterEquals(String left, String right, RuntimeException exception) {
        ifGreaterEquals(new BigDecimal(left), new BigDecimal(right), exception);
    }

    public static void ifGreaterEquals(BigDecimal left, BigDecimal right, String exceptionMessage) {
        ifGreaterEquals(left, right, new RuntimeException(exceptionMessage));
    }

    public static void ifGreaterEquals(BigDecimal left, BigDecimal right, RuntimeException exception) {
        if (left == null || right == null) throw new IllegalArgumentException(NUMBER_NULL);
        if (left.compareTo(right) >= 0) throw exception;
    }

    public static void ifLess(Number left, Number right, String exceptionMessage) {
        ifLess(left.toString(), right.toString(), new RuntimeException(exceptionMessage));
    }

    public static void ifLess(Number left, Number right, RuntimeException exception) {
        ifLess(left.toString(), right.toString(), exception);
    }

    public static void ifLess(String left, String right, String exceptionMessage) {
        ifLess(new BigDecimal(left), new BigDecimal(right), new RuntimeException(exceptionMessage));
    }

    public static void ifLess(String left, String right, RuntimeException exception) {
        ifLess(new BigDecimal(left), new BigDecimal(right), exception);
    }

    public static void ifLess(BigDecimal left, BigDecimal right, String exceptionMessage) {
        ifLess(left, right, new RuntimeException(exceptionMessage));
    }

    public static void ifLess(BigDecimal left, BigDecimal right, RuntimeException exception) {
        if (left == null || right == null) throw new IllegalArgumentException(NUMBER_NULL);
        if (left.compareTo(right) < 0) throw exception;
    }

    public static void ifLessEquals(Number left, Number right, String exceptionMessage) {
        ifLessEquals(left.toString(), right.toString(), new RuntimeException(exceptionMessage));
    }

    public static void ifLessEquals(Number left, Number right, RuntimeException exception) {
        ifLessEquals(left.toString(), right.toString(), exception);
    }

    public static void ifLessEquals(String left, String right, String exceptionMessage) {
        ifLessEquals(new BigDecimal(left), new BigDecimal(right), new RuntimeException(exceptionMessage));
    }

    public static void ifLessEquals(String left, String right, RuntimeException exception) {
        ifLessEquals(new BigDecimal(left), new BigDecimal(right), exception);
    }

    public static void ifLessEquals(BigDecimal left, BigDecimal right, String exceptionMessage) {
        ifLessEquals(left, right, new RuntimeException(exceptionMessage));
    }

    public static void ifLessEquals(BigDecimal left, BigDecimal right, RuntimeException exception) {
        if (left == null || right == null) throw new IllegalArgumentException(NUMBER_NULL);
        if (left.compareTo(right) <= 0) throw exception;
    }

}
