package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ModifierUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ArrayUtil.isEmpty;
import static cn.hutool.core.util.ArrayUtil.removeNull;
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

    private final String[] basePackages;

    static final Map<Type, Integer>              BASIC_BYTES_SIZE_CACHE              = new SafeConcurrentHashMap<>(64);
    static final Map<Class<?>, Field[]>          STRUCT_FIELD_CACHE                  = new ConcurrentHashMap<>(512);
    static final Map<Field, Annotation>          FIELD_PROP_HANDLER_ANNOTATION_CACHE = new SafeConcurrentHashMap<>(256);
    static final Map<Field, Function<?, ?>>      FIELD_GETTER_CACHE                  = new ConcurrentHashMap<>(512);
    static final Map<Field, BiConsumer<?, ?>>    FIELD_SETTER_CACHE                  = new SafeConcurrentHashMap<>(512);
    static final Map<Type, Supplier<?>>          NO_ARGS_CONSTRUCTOR_CACHE           = new SafeConcurrentHashMap<>(128);
    static final Map<Type, Function<ByteBuf, ?>> BYTEBUF_CONSTRUCTOR_CACHE           = new SafeConcurrentHashMap<>(128);

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructSerializerContext.class);

    public StructSerializerContext(String... basePackages) {
        // will scan all packages if user do not assigned
        this.basePackages = removeNull(basePackages);

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
        Set<Class<?>> classes = doScan();

        // 1 scan property handler
        scanPropHandlers(classes);

        // 2 scan basic
        scanBasics(classes);

        // 3 scan struct
        scanStructs(classes);
    }

    /**
     * do class scan
     *
     * @return the classes can be scanned
     */
    protected Set<Class<?>> doScan() {
        Set<Class<?>> forScan = new HashSet<>(256);

        Filter<Class<?>> scanCondition = clazz ->
                !ClassUtil.isJdkClass(clazz)
                &&
                ClassUtil.isNormalClass(clazz);

        if (log.isDebugEnabled()) log.debug("serializer context start scanning, base-packages: [{}]", this.basePackages);
        for (String basePackage : getBasePackages()) forScan.addAll(ClassScanner.scanAllPackage(basePackage, scanCondition));
        if (log.isDebugEnabled()) log.debug("serializer context finished scanning, base-packages: [{}]", this.basePackages);

        return forScan;
    }

    protected void scanPropHandlers(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isPropertyHandler = StructFieldHandler.class.isAssignableFrom(clazz);

                if (isPropertyHandler) {
                    Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                    if (annotationType != null) {
                        Supplier<?> constructorSupplier = StructUtils.constructor(clazz);
                        // 1 cache prop-handler constructor
                        NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructorSupplier);

                        // 2 cache singleton prop-handler
                        StructFieldHandler handler = (StructFieldHandler) constructorSupplier.get();
                        if (handler.isSingleton()) Singleton.put(handler);

                        // 3 cache annotation -> handler mapping relation
                        StructFieldHandler.ANNOTATION_HANDLER_MAPPING.putIfAbsent(annotationType, (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);
                    }
                }
            } catch (Throwable throwable) {
                log.error("scan struct-prop-handler failed please check, prop-handler class: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanBasics(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isBasic = Basic.class.isAssignableFrom(clazz);

                if (isBasic) {
                    // 1 cache basics constructor
                    BYTEBUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, constructor(clazz, ByteBuf.class));

                    //2 cache bytes size
                    BASIC_BYTES_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, StructUtils.reflectForSize((Class<? extends Basic<?>>) clazz));
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
                        FIELD_GETTER_CACHE.putIfAbsent(field, getGetter(clazz, field));
                        FIELD_SETTER_CACHE.putIfAbsent(field, getSetter(clazz, field));

                        // 4 cache field prop handler annotation
                        for (Annotation annotation : AnnotationUtil.getAnnotations(field, false)) {
                            if (StructFieldHandler.ANNOTATION_HANDLER_MAPPING.containsKey(annotation.annotationType())) {
                                Throws.ifContainsKey(FIELD_PROP_HANDLER_ANNOTATION_CACHE, field, "don't specify more than one prop handler on field [" + field + "]");
                                FIELD_PROP_HANDLER_ANNOTATION_CACHE.put(field, annotation);
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

    public String[] getBasePackages() {
        if (isEmpty(this.basePackages)) return ALL_PACKAGES;
        else                            return ArrayUtil.append(this.basePackages, ClassUtil.getPackage(this.getClass()));
    }
}
