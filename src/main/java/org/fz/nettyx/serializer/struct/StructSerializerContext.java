package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.exceptions.NotInitedException;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ModifierUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hutool.core.lang.reflect.MethodHandleUtil.findConstructor;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.fz.nettyx.serializer.struct.StructPropHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructUtils.getReaderHandle;
import static org.fz.nettyx.serializer.struct.StructUtils.getWriterHandle;
import static org.fz.nettyx.serializer.struct.annotation.Struct.STRUCT_FIELD_CACHE;

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
     * cache field handler-annotation
     */
    public static final Map<Field, Annotation> FIELD_PROP_HANDLER_ANNOTATION_CACHE = new SafeConcurrentHashMap<>(256);
    /**
     * cache getter/setter
     */
    static final Map<Field, MethodHandle> FIELD_READER_CACHE = new ConcurrentHashMap<>(512);
    static final Map<Field, MethodHandle> FIELD_WRITER_CACHE = new ConcurrentHashMap<>(512);
    /**
     * cache constructor
     */
    static final Map<Class<?>, MethodHandle> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();


    static {
        // scan classes
        try {
            doScan(EMPTY);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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

                // 1 scan property handler
                scanPropHandlers(classes);
                // 2 scan basic
                scanBasics(classes);
                // 3 scan struct
                scanStructs(classes);
            }
        } catch (Throwable t) {
            throw new NotInitedException("init struct-serializer context failed please check", t);
        }
    }

    private synchronized static void scanPropHandlers(Set<Class<?>> classes) throws Throwable {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = StructPropHandler.class.isAssignableFrom(clazz);

            if (isPropertyHandler) {
                Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                if (annotationType != null) {
                    MethodHandle handlerConstructor = findConstructor(clazz);
                    // 1 cache prop-handler constructor
                    CONSTRUCTOR_CACHE.putIfAbsent(clazz, handlerConstructor);

                    // 2 cache singleton prop-handler
                    StructPropHandler handler = (StructPropHandler) handlerConstructor.invoke();
                    if (handler.isSingleton()) Singleton.put(handler);

                    // 3 cache annotation -> handler mapping relation
                    StructPropHandler.ANNOTATION_HANDLER_MAPPING.putIfAbsent(annotationType, (Class<? extends StructPropHandler<? extends Annotation>>) clazz);
                }
            }
        }
    }

    private synchronized static void scanBasics(Set<Class<?>> classes)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : classes) {
            boolean isBasic = Basic.class.isAssignableFrom(clazz);

            if (isBasic) {
                // 1 cache basics constructor
                CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, findConstructor(clazz, ByteBuf.class));

                //2 cache bytes size
                Basic.BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, StructUtils.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    private synchronized static void scanStructs(Set<Class<?>> classes) throws IntrospectionException {
        for (Class<?> clazz : classes) {
            if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                // 1 cache struct constructor
                CONSTRUCTOR_CACHE.putIfAbsent(clazz, findConstructor(clazz));

                Field[] structFields = ReflectUtil.getFields(clazz, f -> !Modifier.isStatic(f.getModifiers()) && !isIgnore(f));
                // 2 cache the fields
                STRUCT_FIELD_CACHE.put(clazz, structFields);

                for (Field field : structFields) {
                    // 3 cache field reader and writer
                    FIELD_READER_CACHE.putIfAbsent(field, getReaderHandle(clazz, field));
                    FIELD_WRITER_CACHE.putIfAbsent(field, getWriterHandle(clazz, field));

                    // 4 cache field prop handler annotation
                    for (Annotation annotation : AnnotationUtil.getAnnotations(field, false)) {
                        if (StructPropHandler.ANNOTATION_HANDLER_MAPPING.containsKey(annotation.annotationType())) {
                            Throws.ifContainsKey(FIELD_PROP_HANDLER_ANNOTATION_CACHE, field, "don't specify more than one prop handler on field [" + field + "]");
                            FIELD_PROP_HANDLER_ANNOTATION_CACHE.put(field, annotation);
                        }
                    }
                }
            }
        }
    }

    static boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || ModifierUtil.hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }


}
