package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ModifierUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.util.exception.Throws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ArrayUtil.*;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructUtils.*;

/**
 * The type Struct cache.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("all")
public class StructSerializerContext {

    @Getter
    private final String[] basePackages;

    static final Map<Type, Integer>              BASIC_SIZE_CACHE                      = new SafeConcurrentHashMap<>(64);
    static final Map<Type, Supplier<?>>          NO_ARGS_CONSTRUCTOR_CACHE             = new SafeConcurrentHashMap<>(128);
    static final Map<Type, Function<ByteBuf, ?>> BASIC_BYTEBUF_CONSTRUCTOR_CACHE       = new SafeConcurrentHashMap<>(128);
    static final Map<Class<?>, Field[]>          STRUCT_FIELD_CACHE                    = new ConcurrentHashMap<>(512);
    static final Map<Field, Annotation>          STRUCT_FIELD_HANDLER_ANNOTATION_CACHE = new SafeConcurrentHashMap<>(256);
    static final Map<Field, Function<?, ?>>      STRUCT_FIELD_GETTER_CACHE             = new ConcurrentHashMap<>(512);
    static final Map<Field, BiConsumer<?, ?>>    STRUCT_FIELD_SETTER_CACHE             = new SafeConcurrentHashMap<>(512);

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructSerializerContext.class);

    public StructSerializerContext(String... basePackages) {
        // will scan all packages if user do not assigned
        this.basePackages = defaultIfEmpty(removeNull(basePackages), ALL_PACKAGES);

        try {
            synchronized (StructSerializerContext.class) {
                scan();
            }
        } catch (Exception exception) {
            log.error("exception occur while scanning classes", exception);
        }
    }

    /**
     * start package scan
     */
    protected void scan() {
        Set<Class<?>> classes = this.classForScan();

        // 1 scan field handler
        this.scanFieldHandlers(classes);

        // 2 scan basic
        this.scanBasics(classes);

        // 3 scan struct
        this.scanStructs(classes);
    }

    /**
     * find class for scan
     *
     * @return the classes can be scanned
     */
    protected Set<Class<?>> classForScan() {
        Set<Class<?>> forScan = new HashSet<>(256);

        Filter<Class<?>> scanCondition = clazz ->
                !ClassUtil.isJdkClass(clazz)
                &&
                ClassUtil.isNormalClass(clazz);

        if (log.isDebugEnabled()) log.debug("serializer context started scanning, base-packages: {}", Arrays.toString(getBasePackages()));
        for (String basePackage : append(getBasePackages(), ClassUtil.getPackage(this.getClass()))) forScan.addAll(ClassScanner.scanAllPackage(basePackage, scanCondition));
        if (log.isDebugEnabled()) log.debug("serializer context finished scanning, base-packages: {}", Arrays.toString(getBasePackages()));

        return forScan;
    }

    protected void scanFieldHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isFieldHandler = StructFieldHandler.class.isAssignableFrom(clazz);

                if (isFieldHandler) {
                    Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                    if (annotationType != null) {
                        Supplier<?> constructorSupplier = StructUtils.constructor(clazz);
                        // 1 cache field handler constructor
                        NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructorSupplier);

                        // 2 cache singleton field handler
                        StructFieldHandler handler = (StructFieldHandler) constructorSupplier.get();
                        if (handler.isSingleton()) Singleton.put(handler);

                        // 3 cache annotation -> handler mapping relation
                        StructFieldHandler.ANNOTATION_HANDLER_MAPPING.putIfAbsent(annotationType, (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);
                    }
                }
            } catch (Throwable throwable) {
                log.error("scan struct field handler failed please check, field handler class: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanBasics(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isBasic = Basic.class.isAssignableFrom(clazz);

                if (isBasic) {
                    // 1 cache basics constructor
                    BASIC_BYTEBUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, constructor(clazz, ByteBuf.class));

                    //2 cache bytes size
                    BASIC_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, StructUtils.reflectForSize((Class<? extends Basic<?>>) clazz));
                }
            } catch (Throwable throwable) {
                log.error("scan basic failed please check, basic type is: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanStructs(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                    // 1 cache struct constructor
                    NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, StructUtils.constructor(clazz));

                    Field[] structFields = ReflectUtil.getFields(clazz, f -> !Modifier.isStatic(f.getModifiers()) && !isIgnore(f));
                    // 2 cache the fields
                    STRUCT_FIELD_CACHE.put(clazz, structFields);

                    for (Field field : structFields) {
                        // 3 cache field reader and writer
                        STRUCT_FIELD_GETTER_CACHE.putIfAbsent(field, getGetter(clazz, field));
                        STRUCT_FIELD_SETTER_CACHE.putIfAbsent(field, getSetter(clazz, field));

                        // 4 cache field field handler annotation
                        for (Annotation annotation : AnnotationUtil.getAnnotations(field, false)) {
                            if (StructFieldHandler.ANNOTATION_HANDLER_MAPPING.containsKey(annotation.annotationType())) {
                                Throws.ifContainsKey(STRUCT_FIELD_HANDLER_ANNOTATION_CACHE, field, "don't specify more than one field handler on field [" + field + "]");
                                STRUCT_FIELD_HANDLER_ANNOTATION_CACHE.put(field, annotation);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                log.error("scan struct failed please check, struct class is: [{}]", clazz, throwable);
            }
        }
    }

    protected boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || ModifierUtil.hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }


    private static final String[] ALL_PACKAGES = { EMPTY };
}
