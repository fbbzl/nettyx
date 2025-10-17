package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.lambda.LambdaMetas;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ArrayUtil.*;
import static cn.hutool.core.util.ReflectUtil.getFields;
import static org.fz.erwin.lambda.LambdaMetas.lambdaConstructor;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.DEFAULT_STRUCT_FIELD_HANDLER;

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

    static final Map<Type, Integer>                                   BASIC_SIZE_CACHE                = new HashMap<>(64);
    static final Map<Class<? extends Basic<?>>, Function<ByteBuf, ?>> BASIC_BYTEBUF_CONSTRUCTOR_CACHE = new HashMap<>(64);
    static final Map<Class<?>, StructDefinition>                      STRUCT_DEFINITION_CACHE         = new HashMap<>(256);

    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new HashMap<>(32);

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructSerializerContext.class);

    public StructSerializerContext(String... basePackages)
    {
        // will scan all packages if user do not assign
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
    protected void scan()
    {
        Set<Class<?>> classes = this.classesForScan();

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
    protected Set<Class<?>> classesForScan()
    {
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

    protected void scanHandler(Set<Class<?>> classes)
    {
        for (Class<?> clazz : classes) {
            try {
                boolean isFieldHandler = StructFieldHandler.class.isAssignableFrom(clazz);

                if (isFieldHandler) {
                    Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                    if (annotationType != null) {
                        // cache annotation handler mapping relation
                        ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(annotationType,
                                                                     (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);
                    }
                }
            }
            catch (Throwable throwable) {
                log.error("scan struct field handler failed please check, field handler class: [{}]", clazz, throwable);
            }
        }
    }

    protected void scanBasic(Set<Class<?>> classes)
    {
        for (Class<?> clazz : classes) {
            try {
                boolean isBasic = Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;

                if (isBasic) {
                    // cache basics constructor
                    BASIC_BYTEBUF_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                                lambdaConstructor(clazz, ByteBuf.class));

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

    protected void scanStruct(Set<Class<?>> classes)
    {
        for (Class<?> clazz : classes) {
            try {
                if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                    STRUCT_DEFINITION_CACHE.put(clazz, new StructDefinition(clazz));
                }
            }
            catch (Throwable throwable) {
                log.error("scan struct failed please check, struct class is: [{}]", clazz, throwable);
            }
        }
    }

    static <A extends Annotation, H extends StructFieldHandler<A>> Supplier<H> getHandlerSupplier(Field field)
    {
        Annotation handlerAnnotation = getHandlerAnnotation(field);

        if (handlerAnnotation != null) {
            Supplier<H> handlerSupplier =
                    lambdaConstructor(ANNOTATION_HANDLER_MAPPING_CACHE.get(handlerAnnotation.annotationType()));

            StructFieldHandler handler = (StructFieldHandler) handlerSupplier.get();
            if (handler.isSingleton()) Singleton.put(handler);
            handler.doAnnotationValid(handlerAnnotation, field);

            // if is singleton, return singleton instance
            return handler.isSingleton() ? () -> (H) handler : (Supplier<H>) handlerSupplier;
        }

        return () -> (H) DEFAULT_STRUCT_FIELD_HANDLER;
    }

    static <A extends Annotation> A getHandlerAnnotation(Field field)
    {
        Iterator<Annotation> iterator =
                Stream.of(AnnotationUtil.getAnnotations(field, false))
                      .filter(annotation -> ANNOTATION_HANDLER_MAPPING_CACHE.containsKey(annotation.annotationType()))
                      .iterator();
        // means will use handler to handle this field
        return iterator.hasNext() ? (A) iterator.next() : null;
    }

    public static StructDefinition getStructDefinition(Type type)
    {
        return switch (type) {
            case Class<?> clazz -> STRUCT_DEFINITION_CACHE.get(clazz);
            case ParameterizedType parameterizedType -> getStructDefinition(parameterizedType.getRawType());
            default -> throw new TypeJudgmentException("can not find struct definition by: [" + type + "]");
        };
    }


    static <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz)
    {
        if (!ClassUtil.isNormalClass(clazz)) return null;

        Type[] genericInterfaces = clazz.getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    return (Class<A>) actualTypeArguments[0];
                }
            }
        }
        return null;
    }

    @Accessors(fluent = true)
    @SuppressWarnings("unchecked")
    public record StructDefinition(
            Class<?>      type,
            Supplier<?>   constructor,
            StructField[] fields
    ) {
        public StructDefinition(Class<?> clazz)
        {
            this(clazz,
                 lambdaConstructor(clazz),
                 Stream.of(getFields(clazz, StructHelper::legalStructField))
                       .map(StructField::new)
                       .toArray(StructField[]::new));
        }

        @Getter
        @RequiredArgsConstructor
        @SuppressWarnings("unchecked")
        @Accessors(fluent = true)
        @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
        public static class StructField {

            Field               wrapped;
            UnaryOperator<Type> type;
            Function<?, ?>      getter;
            BiConsumer<?, ?>    setter;
            Annotation          annotation;

            @Getter(AccessLevel.NONE)
            Supplier<? extends StructFieldHandler<? extends Annotation>> handleSupplier;

            public StructField(Field field)
            {
                this(field,
                     typeSupplier(field),
                     LambdaMetas.lambdaGetter(field),
                     LambdaMetas.lambdaSetter(field),
                     getHandlerAnnotation(field),
                     getHandlerSupplier(field));
            }

            static UnaryOperator<Type> typeSupplier(Field field) {
                Type fieldType = field.getGenericType();
                return fieldType instanceof Class<?> ? root -> (Class<?>) fieldType :
                       root -> TypeUtil.getActualType(root, fieldType);
            }

            public Type type(Type root) {
                return type.apply(root);
            }

            public <A extends Annotation> A annotation() {
                return (A) annotation;
            }

            public <A extends Annotation, H extends StructFieldHandler<A>> H handler() {
                return (H) handleSupplier.get();
            }

            public <O, R> Function<O, R> getter() {
                return (Function<O, R>) getter;
            }

            public <O, P> BiConsumer<O, P> setter() {
                return (BiConsumer<O, P>) setter;
            }

            @Override
            public String toString() {
                return wrapped.toString();
            }
        }
    }
}
