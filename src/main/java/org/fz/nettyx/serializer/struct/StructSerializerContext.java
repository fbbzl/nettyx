package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.fz.nettyx.serializer.struct.StructFieldHandler.getTargetAnnotationType;

/**
 * The type Struct cache.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@Slf4j
@SuppressWarnings("all")
public final class StructSerializerContext {

    /**
     * reflection cache
     */
    static final Map<Field, Method> FIELD_READER_CACHE = new WeakConcurrentMap<>();
    static final Map<Field, Method> FIELD_WRITER_CACHE = new WeakConcurrentMap<>();

    static final Map<Class<? extends Basic<?>>, Integer> BASIC_BYTES_SIZE_CACHE = new WeakConcurrentMap<>();

    /**
     * mapping handler and annotation
     */
    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING = new SafeConcurrentHashMap<>();

    @Getter
    private final String[] packageNames;

    public StructSerializerContext(String... packageNames) {
        this.packageNames = packageNames;
        doScan(packageNames);
    }

    /**
     * scan assigned packages
     *
     * @param packageNames the packages with struct or basic
     */
    public synchronized void doScan(String... packageNames) {
        log.info("will scan " + Arrays.toString(packageNames) + " packages");
        try {
            for (String packageName : packageNames) {
                Set<Class<?>> classes = ClassScanner.scanPackage(packageName, ClassUtil::isNormalClass);

                scanHandlers(classes);
                scanBasics(classes);
                scanStructs(classes);
            }
        } catch (Exception e) {
            throw new NotInitedException("init serializer context failed please check", e);
        }
    }

    private synchronized void scanHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = StructFieldHandler.class.isAssignableFrom(clazz);

            if (isPropertyHandler) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(clazz);
                if (targetAnnotationType != null) {
                    ANNOTATION_HANDLER_MAPPING.putIfAbsent(targetAnnotationType,
                            (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);

                    ReflectUtil.getConstructor(clazz);
                }
            }
        }
    }

    private synchronized void scanBasics(Set<Class<?>> classes)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : classes) {
            boolean isBasic = Basic.class.isAssignableFrom(clazz);

            if (isBasic) {
                BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                        Basic.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    private synchronized void scanStructs(Set<Class<?>> classes) throws IntrospectionException {
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
