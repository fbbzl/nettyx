package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hutool.core.lang.reflect.MethodHandleUtil.findConstructor;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructUtils.getReaderHandle;
import static org.fz.nettyx.serializer.struct.StructUtils.getWriterHandle;

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
    static final Map<Field, MethodHandle> FIELD_READER_CACHE = new ConcurrentHashMap<>(512);
    static final Map<Field, MethodHandle> FIELD_WRITER_CACHE = new ConcurrentHashMap<>(512);
    static final Map<Class<?>, MethodHandle> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    static final Map<Class<? extends Basic<?>>, Integer> BASIC_BYTES_SIZE_CACHE = new WeakConcurrentMap<>();

    /**
     * mapping handler and annotation
     */
    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING = new SafeConcurrentHashMap<>();

    static {
        doScan(EMPTY);
    }

    /**
     * scan assigned packages
     *
     * @param packageNames the packages with struct or basic
     */
    public synchronized static void doScan(String... packageNames) {
        log.debug("will scan " + Arrays.toString(packageNames) + " packages");
        try {
            for (String packageName : packageNames) {
                Set<Class<?>> classes = ClassScanner.scanPackage(packageName, ClassUtil::isNormalClass);

                scanFieldHandlers(classes);
                scanBasics(classes);
                scanStructs(classes);
            }
        } catch (Exception e) {
            throw new NotInitedException("init struct-serializer context failed please check", e);
        }
    }

    private synchronized static void scanFieldHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = StructFieldHandler.class.isAssignableFrom(clazz);

            if (isPropertyHandler) {
                Class<Annotation> targetAnnotationType = getTargetAnnotationType(clazz);
                if (targetAnnotationType != null) {
                    CONSTRUCTOR_CACHE.putIfAbsent(clazz, findConstructor(clazz));
                    ANNOTATION_HANDLER_MAPPING.putIfAbsent(targetAnnotationType,
                                                           (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);
                }
            }
        }
    }

    private synchronized static void scanBasics(Set<Class<?>> classes)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : classes) {
            boolean isBasic = Basic.class.isAssignableFrom(clazz);

            if (isBasic) {
                CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, findConstructor(clazz, ByteBuf.class));
                BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, StructUtils.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    private synchronized static void scanStructs(Set<Class<?>> classes) throws IntrospectionException {
        for (Class<?> clazz : classes) {
            if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                CONSTRUCTOR_CACHE.putIfAbsent(clazz, findConstructor(clazz));

                Field[] structFields = StructUtils.getStructFields(clazz);
                for (Field field : structFields) {

                    FIELD_READER_CACHE.putIfAbsent(field, getReaderHandle(clazz, field));
                    FIELD_WRITER_CACHE.putIfAbsent(field, getWriterHandle(clazz, field));

                    AnnotationUtil.getAnnotations(field, false);
                }
            }
        }
    }

}
