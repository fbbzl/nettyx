package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.fz.nettyx.serializer.struct.StructPropHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructUtils.*;
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

    public static final Map<Field, Annotation> FIELD_PROP_HANDLER_ANNOTATION_CACHE = new SafeConcurrentHashMap<>(256);

    static final Map<Field, MethodHandle> FIELD_READER_CACHE = new ConcurrentHashMap<>(512);
    static final Map<Field, MethodHandle> FIELD_WRITER_CACHE = new ConcurrentHashMap<>(512);

    static final Map<Class<?>, Supplier<?>>          NO_ARGS_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(128);
    static final Map<Class<?>, Function<ByteBuf, ?>> BYTEBUF_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(128);

    static {
        // scan classes
        doScan(EMPTY);
    }

    /**
     * scan assigned packages
     *
     * @param packageNames the packages with struct or basic
     */
    public synchronized static void doScan(String... packageNames) {
        log.debug("will scan " + Arrays.toString(packageNames) + " packages");
        for (String packageName : packageNames) {
            Set<Class<?>> classes = ClassScanner.scanPackage(packageName, ClassUtil::isNormalClass);

            try {
                // 1 scan property handler
                scanPropHandlers(classes);
            } catch (Throwable throwable) {
                log.error("scan struct-prop-handler failed please check", throwable);
            }

            // 2 scan basic
            try {
                scanBasics(classes);
            } catch (Throwable throwable) {
                log.error("scan basic failed please check", throwable);
            }

            try {
                // 3 scan struct
                scanStructs(classes);
            } catch (Throwable throwable) {
                log.error("scan struct failed please check", throwable);
            }
        }
    }

    private synchronized static void scanPropHandlers(Set<Class<?>> classes) throws Throwable {
        for (Class<?> clazz : classes) {
            boolean isPropertyHandler = StructPropHandler.class.isAssignableFrom(clazz);

            if (isPropertyHandler) {
                Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                if (annotationType != null) {
                    Supplier<?> constructorSupplier = constructorSupplier(clazz);
                    // 1 cache prop-handler constructor
                    NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructorSupplier);

                    // 2 cache singleton prop-handler
                    StructPropHandler handler = (StructPropHandler) constructorSupplier.get();
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
                BYTEBUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, constructorFunction(clazz, ByteBuf.class));

                //2 cache bytes size
                Basic.BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, StructUtils.reflectForSize((Class<? extends Basic<?>>) clazz));
            }
        }
    }

    private synchronized static void scanStructs(Set<Class<?>> classes) throws Throwable {
        for (Class<?> clazz : classes) {
            if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                // 1 cache struct constructor
                NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructorSupplier(clazz));

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
