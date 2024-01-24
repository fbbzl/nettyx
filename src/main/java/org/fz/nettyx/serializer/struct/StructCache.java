package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.fz.nettyx.serializer.struct.PropertyHandler.getTargetAnnotationType;

/**
 * The Struct cache.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/26 9:28
 */
@SuppressWarnings("all")
final class StructCache {

    /* reflection cache */
    static final Map<Field, Method> FIELD_READER_CACHE = new WeakConcurrentMap<>();
    static final Map<Field, Method> FIELD_WRITER_CACHE = new WeakConcurrentMap<>();

    static final Map<Class<? extends Basic<?>>, Integer> BASIC_BYTES_SIZE_CACHE = new WeakConcurrentMap<>();

    /* mapping handler and annotation */
    static final Map<Class<? extends Annotation>, Class<? extends PropertyHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new ConcurrentHashMap<>();

    static void doScan(String packageName) {
        try {
            Set<Class<?>> classes = ClassScanner.scanPackage(packageName);

            scanAllHandlers(classes);
            scanAllBasics(classes);
            scanAllStructs(classes);
        } catch (Exception e) {
            throw new NotInitedException("init serializer context failed please check", e);
        }
    }

    static synchronized void scanAllHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = PropertyHandler.class.isAssignableFrom(clazz) && !PropertyHandler.class.equals(clazz);
            if (isPropertyHandler) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(clazz);
                if (targetAnnotationType != null) {
                    ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(targetAnnotationType,
                            (Class<? extends PropertyHandler<? extends Annotation>>) clazz);

                    ReflectUtil.getConstructor(clazz);
                }
            }
        }
    }

    static synchronized void scanAllBasics(Set<Class<?>> classes)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : classes) {
            int mod;
            boolean isBasic =
                    Basic.class.isAssignableFrom(clazz)
                            && !Basic.class.equals(clazz)
                            && !clazz.isEnum()
                            && !Modifier.isAbstract((mod = clazz.getModifiers()))
                            && !Modifier.isInterface(mod);
            if (isBasic) {
                BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                        Basic.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    static synchronized void scanAllStructs(Set<Class<?>> classes) throws IntrospectionException {
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
