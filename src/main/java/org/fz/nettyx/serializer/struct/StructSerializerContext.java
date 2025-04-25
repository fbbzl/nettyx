package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ArrayUtil.*;
import static org.fz.nettyx.serializer.struct.StructDefinition.STRUCT_DEFINITION_CACHE;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.DEFAULT_READ_WRITE_HANDLER;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.getTargetAnnotationType;
import static org.fz.nettyx.serializer.struct.StructHelper.constructor;

/**
 * The type Struct cache.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("all")
public class StructSerializerContext {

    static final String[] ALL_PACKAGES = { EMPTY };

    @Getter
    private final String[] basePackages;

    static final Map<Type, Integer>              BASIC_SIZE_CACHE                = new HashMap<>(64);
    static final Map<Type, Function<ByteBuf, ?>> BASIC_BYTEBUF_CONSTRUCTOR_CACHE = new HashMap<>(64);
    static final Map<Type, Supplier<?>>          NO_ARGS_CONSTRUCTOR_CACHE       = new HashMap<>(128);

    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE =
            new HashMap<>(32);

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructSerializerContext.class);

    public StructSerializerContext(String... basePackages) {
        // will scan all packages if user do not assigned
        this.basePackages = defaultIfEmpty(removeNull(basePackages), ALL_PACKAGES);

        try {
            synchronized (StructSerializerContext.class) {
                scan();
            }
        }
        catch (Exception exception) {
            log.error("exception occur while scanning classes", exception);
        }
    }

    /**
     * start package scan
     */
    protected void scan() {
        Set<Class<?>> classes = this.classForScan();

        // 1 scan field handler
        this.scanHandler(classes);

        // 2 scan basic
        this.scanBasic(classes);

        // 3 scan struct
        this.scanStruct(classes);
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

        if (log.isDebugEnabled()) log.debug("serializer context started scanning, base-packages: {}",
                                            Arrays.toString(getBasePackages()));
        for (String basePackage :
                append(getBasePackages(), ClassUtil.getPackage(this.getClass())))
            forScan.addAll(ClassScanner.scanAllPackage(basePackage, scanCondition));
        if (log.isDebugEnabled()) log.debug("serializer context finished scanning, base-packages: {}",
                                            Arrays.toString(getBasePackages()));

        return forScan;
    }

    protected void scanHandler(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isFieldHandler = StructFieldHandler.class.isAssignableFrom(clazz);

                if (isFieldHandler) {
                    Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                    if (annotationType != null) {
                        Supplier<?> constructorSupplier = constructor(clazz);
                        // cache field handler constructor
                        NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructorSupplier);

                        // cache annotation handler mapping relation
                        ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(annotationType, (Class<?
                                extends StructFieldHandler<? extends Annotation>>) clazz);
                    }
                }
            }
            catch (Throwable throwable) {
                log.error("scan struct field handler failed please check, field handler class: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanBasic(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                boolean isBasic = Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;

                if (isBasic) {
                    // cache basics constructor
                    BASIC_BYTEBUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz, constructor(clazz,
                                                                                                               ByteBuf.class));

                    // cache bytes size
                    BASIC_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                 StructHelper.reflectForSize((Class<? extends Basic<?>>) clazz));
                }
            }
            catch (Throwable throwable) {
                log.error("scan basic failed please check, basic type is: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanStruct(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                    // constructor
                    Supplier<?> constructor = constructor(clazz);
                    NO_ARGS_CONSTRUCTOR_CACHE.putIfAbsent(clazz, constructor);

                    // struct fields
                    final StructField[] structFields =
                            Stream.of(ReflectUtil.getFields(clazz, StructHelper::legalStructField))
                                  .map(field -> {
                                      Pair<Annotation, Supplier<? extends StructFieldHandler<? extends Annotation>>>
                                              handlerMapping = getHandlerMapping(field);

                                      return new StructField(clazz, field, handlerMapping.getKey(), handlerMapping.getValue());
                                  })
                                  .toArray(StructField[]::new);

                    STRUCT_DEFINITION_CACHE.put(clazz, new StructDefinition(constructor, structFields));
                }
            }
            catch (Throwable throwable) {
                log.error("scan struct failed please check, struct class is: [{}]", clazz, throwable);
            }
        }
    }

    private static Pair<Annotation, Supplier<? extends StructFieldHandler<? extends Annotation>>> getHandlerMapping(Field field) {
        Iterator<Annotation> iterator =
                Stream.of(AnnotationUtil.getAnnotations(field, false))
                      .filter(annotation -> ANNOTATION_HANDLER_MAPPING_CACHE.containsKey(annotation.annotationType()))
                      .iterator();
        // means will use handler to handle this field
        boolean useHandler = iterator.hasNext();

        if (useHandler) {
            Annotation annotation = iterator.next();
            Supplier<? extends StructFieldHandler<? extends Annotation>>
                    handlerConstructorSupplier =
                    StructHelper.constructor(ANNOTATION_HANDLER_MAPPING_CACHE.get(annotation.annotationType()));

            StructFieldHandler handler = (StructFieldHandler) handlerConstructorSupplier.get();
            if (handler.isSingleton()) Singleton.put(handler);

            Supplier<? extends StructFieldHandler<? extends Annotation>>
                    handlerSupplier = handler.isSingleton() ? () -> handler : handlerConstructorSupplier;

            return Pair.of(annotation, handlerSupplier);
        }
        else return Pair.of(null, () -> DEFAULT_READ_WRITE_HANDLER);
    }

}
