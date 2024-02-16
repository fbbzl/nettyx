package org.fz.nettyx.serializer.struct;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ClassUtil.isAbstractOrInterface;
import static cn.hutool.core.util.ClassUtil.isEnum;
import static org.fz.nettyx.serializer.struct.PropertyHandler.getTargetAnnotationType;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

/**
 * The type Struct cache.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@UtilityClass
@SuppressWarnings("all")
final class StructContext {

    /**
     * reflection cache
     */
    static final Map<Field, Method> FIELD_READER_CACHE = new WeakConcurrentMap<>();
    static final Map<Field, Method> FIELD_WRITER_CACHE = new WeakConcurrentMap<>();

    static final Map<Class<? extends Basic<?>>, Integer> BASIC_BYTES_SIZE_CACHE = new WeakConcurrentMap<>();

    /**
     * mapping handler and annotation
     */
    static final Map<Class<? extends Annotation>, Class<? extends PropertyHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING = new SafeConcurrentHashMap<>();

    static {
        // TODO 在构造函数中进行扫描, 虽然会增加使用难度, 但是效率高了很多,且使用统一的使用方式
        doScan(EMPTY);
    }

    /**
     * scan assigned packages
     *
     * @param packageNames the packages with struct or basic
     */
    public static synchronized void doScan(String... packageNames) {
        try {
            for (String packageName : packageNames) {
                Set<Class<?>> classes =
                        ClassScanner.scanPackage(packageName, clazz -> !isEnum(clazz) && !isAbstractOrInterface(clazz));

                scanAllHandlers(classes);
                scanAllBasics(classes);
                scanAllStructs(classes);
            }
        } catch (Exception e) {
            throw new NotInitedException("init serializer context failed please check", e);
        }
    }

    private static synchronized void scanAllHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = PropertyHandler.class.isAssignableFrom(clazz);

            if (isPropertyHandler) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(clazz);
                if (targetAnnotationType != null) {
                    ANNOTATION_HANDLER_MAPPING.putIfAbsent(targetAnnotationType,
                            (Class<? extends PropertyHandler<? extends Annotation>>) clazz);

                    ReflectUtil.getConstructor(clazz);
                }
            }
        }
    }

    private static synchronized void scanAllBasics(Set<Class<?>> classes)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : classes) {
            boolean isBasic = Basic.class.isAssignableFrom(clazz);

            if (isBasic) {
                BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                        Basic.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    private static synchronized void scanAllStructs(Set<Class<?>> classes) throws IntrospectionException {
        for (Class<?> clazz : classes) {
            if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                ReflectUtil.getConstructor(clazz);

                Field[] structFields = StructUtils.getStructFields(clazz);

                for (Field field : structFields) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                    FIELD_READER_CACHE.putIfAbsent(field, propertyDescriptor.getReadMethod());
                    FIELD_WRITER_CACHE.putIfAbsent(field, propertyDescriptor.getWriteMethod());

                    AnnotationUtil.getAnnotations(field, false);
                }
            }
        }
    }

}
