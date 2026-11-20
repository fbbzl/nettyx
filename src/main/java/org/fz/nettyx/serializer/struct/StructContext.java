package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ModifierUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.struct.accessor.StructAccessorFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.hutool.core.annotation.AnnotationUtil.hasAnnotation;
import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.util.ArrayUtil.*;
import static cn.hutool.core.util.ModifierUtil.hasModifier;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.hutool.core.util.ReflectUtil.getFields;
import static org.fz.erwin.lambda.LambdaMetas.lambdaConstructor;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.DEFAULT_STRUCT_FIELD_HANDLER;

/**
 * Initializes struct metadata in registration caches shared by all contexts.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("all")
public class StructContext {

    static final Map<Type, Integer>                                                BASIC_SIZE_CACHE        = new ConcurrentHashMap<>(64);
    static final Map<Class<? extends Basic<?>>, BiFunction<ByteBuf, ByteOrder, ?>> BASIC_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(64);
    static final Map<Class<?>, StructDefinition>                                   STRUCT_DEFINITION_CACHE = new ConcurrentHashMap<>(256);

    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new ConcurrentHashMap<>(32);

    private static final Cache<ResolutionKey, Type> TYPE_RESOLUTION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(4096)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructContext.class);

    public StructContext(String... basePackages)
    {
        new StructScanner(basePackages).scan();
    }

    public static int getBasicSize(Type basicClass) {
        Type raw = basicClass instanceof ParameterizedType parameterizedtype ? parameterizedtype.getRawType() : basicClass;
        Integer size = BASIC_SIZE_CACHE.get(raw);
        if (size == null) throw new SerializeException("uncached basic type: " + basicClass);
        return size;
    }

    static BiFunction<ByteBuf, ByteOrder, ?> getBasicConstructor(Class<?> basicClass) {
        return BASIC_CONSTRUCTOR_CACHE.get(basicClass);
    }

    public static Type resolveType(Type root, Type type) {
        if (type instanceof Class<?>) return type;

        // The same field can resolve differently for each generic root type.
        ResolutionKey key = new ResolutionKey(root, type);
        Type resolved = TYPE_RESOLUTION_CACHE.getIfPresent(key);
        if (resolved != null) return resolved;

        Type computed = TypeToken.of(root).resolveType(type).getType();
        Type previous = TYPE_RESOLUTION_CACHE.asMap().putIfAbsent(key, computed);
        return previous == null ? computed : previous;
    }

    /**
     * Keys the type resolution cache by the root type and the type being resolved.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    private record ResolutionKey(Type root, Type type) {
    }

    static <A extends Annotation, H extends StructFieldHandler<A>> Supplier<H> getHandlerSupplier(
            A handlerAnnotation,
            Field field)
    {
        if (handlerAnnotation != null)
        {
            Supplier<H> handlerSupplier =
                    (Supplier<H>) lambdaConstructor(ANNOTATION_HANDLER_MAPPING_CACHE.get(handlerAnnotation.annotationType()));

            StructFieldHandler handler = (StructFieldHandler) handlerSupplier.get();
            handler.doValid(handlerAnnotation, field);

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
        return switch (type)
        {
            case Class<?>          clazz             -> STRUCT_DEFINITION_CACHE.get(clazz);
            case ParameterizedType parameterizedType -> getStructDefinition(parameterizedType.getRawType());
            case GenericArrayType  genericArrayType  -> getStructDefinition(genericArrayType.getGenericComponentType());
            case WildcardType      wildcardType      -> getStructDefinition(wildcardType.getUpperBounds()[0]);
            default                                  -> throw new TypeJudgmentException("can not find struct definition by: [" + type + "]");
        };
    }

    /**
     * Discovers types and generates accessors during context initialization.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2024-01-29
     */
    private static final class StructScanner
    {
        private static final String[] ALL_PACKAGES = { EMPTY };

        private final String[] basePackages;

        private StructScanner(String... basePackages)
        {
            // Keep full-classpath scanning when no package is specified.
            this.basePackages = defaultIfEmpty(removeNull(basePackages), ALL_PACKAGES);
        }

        private void scan()
        {
            // Serialize scans so rollback cannot overwrite another context's registrations.
            synchronized (StructContext.class) {
                Map<Type, Integer>                                                sizeSnapshot        = new HashMap<>(BASIC_SIZE_CACHE);
                Map<Class<? extends Basic<?>>, BiFunction<ByteBuf, ByteOrder, ?>> constructorSnapshot = new HashMap<>(BASIC_CONSTRUCTOR_CACHE);
                Map<Class<?>, StructDefinition>                                   definitionSnapshot  = new HashMap<>(STRUCT_DEFINITION_CACHE);

                Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> handlerSnapshot = new HashMap<>(ANNOTATION_HANDLER_MAPPING_CACHE);

                try {
                    Set<Class<?>> classes = classesForScan();
                    // Field metadata requires handlers and basic types to be registered first.
                    scanHandler(classes);
                    scanBasic(classes);
                    scanStruct(classes);
                }
                catch (RuntimeException | Error exception) {
                    // Restore registrations only; already loaded ASM classes cannot be unloaded here.
                    restoreCache(BASIC_SIZE_CACHE, sizeSnapshot);
                    restoreCache(BASIC_CONSTRUCTOR_CACHE, constructorSnapshot);
                    restoreCache(STRUCT_DEFINITION_CACHE, definitionSnapshot);
                    restoreCache(ANNOTATION_HANDLER_MAPPING_CACHE, handlerSnapshot);
                    log.error("exception occur while scanning classes", exception);
                    throw exception;
                }
            }
        }

        private <K, V> void restoreCache(Map<K, V> cache, Map<K, V> snapshot)
        {
            cache.keySet().removeIf(key -> !snapshot.containsKey(key));
        }

        private Set<Class<?>> classesForScan()
        {
            Set<Class<?>> classesForScan = new HashSet<>(256);

            Filter<Class<?>> scanCondition = this::isScannableClass;

            for (String pack : append(basePackages, ClassUtil.getPackage(StructContext.class))) {
                classesForScan.addAll(ClassScanner.scanAllPackage(pack, scanCondition));
            }
            // Schema and Struct share basic types, even when only model packages are requested.
            classesForScan.addAll(ClassScanner.scanAllPackage("org.fz.nettyx.serializer.basic", scanCondition));

            log.debug("serializer context finished scanning, base-packages: {}", Arrays.toString(basePackages));

            return classesForScan;
        }

        private boolean isScannableClass(Class<?> clazz)
        {
            if (!ClassUtil.isNormalClass(clazz)) return false;
            return !ClassUtil.isJdkClass(clazz);
        }

        private void scanHandler(Set<Class<?>> classes)
        {
            for (Class<?> clazz : classes) {
                boolean isFieldHandler = StructFieldHandler.class.isAssignableFrom(clazz);

                if (isFieldHandler) {
                    Class<? extends Annotation> annotationType = getTargetAnnotationType(clazz);
                    if (annotationType != null) {
                        ANNOTATION_HANDLER_MAPPING_CACHE.putIfAbsent(annotationType,
                                                                     (Class<? extends StructFieldHandler<? extends Annotation>>) clazz);
                    }
                }
            }
        }

        private void scanStruct(Set<Class<?>> classes)
        {
            Map<Class<?>, StructDefinition> definitions = new HashMap<>();
            for (Class<?> clazz : classes) {
                try {
                    if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                        if (ClassUtil.isAbstractOrInterface(clazz))   throw new SerializeException("struct class can not be interface or abstract: [" + clazz + "]");
                        if (Modifier.isPrivate(clazz.getModifiers())) throw new SerializeException("struct class can not be private: [" + clazz + "]");

                        Field[] structFields = getFields(clazz, StructDefinition.StructField::legalStructField);
                        if (structFields.length > 0 && !BeanUtil.isBean(clazz)) throw new SerializeException("struct class must be a JavaBean: [" + clazz + "]");

                        try {
                            Constructor<?> constructor = clazz.getDeclaredConstructor();
                            if (Modifier.isPrivate(constructor.getModifiers())) throw new SerializeException("struct no-arg constructor can not be private: [" + clazz + "]");
                        } catch (NoSuchMethodException e) {
                            throw new SerializeException("struct class must have a non-private no-arg constructor: [" + clazz + "]", e);
                        }
                        definitions.put(clazz, new StructDefinition(clazz, structFields));
                    }
                }
                catch (RuntimeException | Error throwable) {
                    throw new SerializeException("scan struct failed: [" + clazz + "]", throwable);
                }
            }

            Map<Class<?>, StructDefinition> allDefinitions = new HashMap<>(STRUCT_DEFINITION_CACHE);
            allDefinitions.putAll(definitions);
            validateNoCircularReferences(allDefinitions);

            // Validate the whole batch before generating accessors and publishing definitions.
            StructAccessorFactory.generate(definitions.values());
            STRUCT_DEFINITION_CACHE.putAll(definitions);
        }

        private void validateNoCircularReferences(Map<Class<?>, StructDefinition> definitions)
        {
            Set<Type> visited = new HashSet<>();
            List<Class<?>> typePath = new ArrayList<>();
            List<String> fieldPath = new ArrayList<>();

            definitions.keySet().stream()
                       .sorted(Comparator.comparing(Class::getName))
                       .forEach(type -> detectCircularReference(type, definitions, visited, typePath, fieldPath));
        }

        private void detectCircularReference(
                Type type,
                Map<Class<?>, StructDefinition> definitions,
                Set<Type> visited,
                List<Class<?>> typePath,
                List<String> fieldPath)
        {
            // Track resolved types as visited, but detect recursion by raw classes on the current path.
            if (!visited.add(type)) return;

            Class<?> rawType = rawStructClass(type);
            typePath.add(rawType);
            for (StructDefinition.StructField field : definitions.get(rawType).fields()) {
                Type dependencyType = structDependency(type, field);
                Class<?> dependency = rawStructClass(dependencyType);
                if (dependency == null || !definitions.containsKey(dependency)) continue;

                String edge = rawType.getSimpleName() + "." + field.wrapped().getName();
                if (typePath.contains(dependency)) {
                    int cycleStart = typePath.indexOf(dependency);
                    List<String> cycle = new ArrayList<>(fieldPath.subList(cycleStart, fieldPath.size()));
                    cycle.add(edge);
                    throw new StructDefinitionException("circular struct reference: [" + String.join(" -> ", cycle) + " -> " + dependency.getSimpleName() + "]");
                }

                fieldPath.add(edge);
                detectCircularReference(dependencyType, definitions, visited, typePath, fieldPath);
                fieldPath.removeLast();
            }
            typePath.removeLast();
        }

        private Type structDependency(Type root, StructDefinition.StructField field)
        {
            if (field.annotation() != null && !(field.annotation() instanceof ToArray)) return null;

            Type fieldType;
            try
            {
                fieldType = field.type(root);
            }
            catch (TypeJudgmentException ignored)
            {
                fieldType = field.wrapped().getGenericType();
            }
            return field.annotation() instanceof ToArray ? arrayComponentType(fieldType) : fieldType;
        }

        private Type arrayComponentType(Type type)
        {
            if (type instanceof Class<?> clazz)
                return clazz.getComponentType();
            return
                ((GenericArrayType) type).getGenericComponentType();
        }

        private Class<?> rawStructClass(Type type)
        {
            if (type == null) return null;

            Class<?> rawType = switch (type)
            {
                case Class<?> clazz                  -> clazz;
                case ParameterizedType parameterized -> (Class<?>) parameterized.getRawType();
                default                              -> null;
            };
            return rawType != null && AnnotationUtil.hasAnnotation(rawType, Struct.class) ? rawType : null;
        }

        private void scanBasic(Set<Class<?>> classes)
        {
            for (Class<?> clazz : classes) {
                try
                {
                    boolean isBasic = Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;

                    if (isBasic)
                    {
                        BASIC_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                            lambdaConstructor(clazz, ByteBuf.class, ByteOrder.class));

                        BASIC_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                     Basic.reflectForSize((Class<? extends Basic<?>>) clazz));
                    }
                }
                catch (RuntimeException | Error throwable) {
                    throw new SerializeException("scan basic failed: [" + clazz + "]", throwable);
                }
            }
        }

        private <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz)
        {
            if (!ClassUtil.isNormalClass(clazz)) return null;

            Type annotationType = TypeToken.of(clazz).resolveType(StructFieldHandler.class.getTypeParameters()[0]).getType();
            return annotationType instanceof Class<?> annotationClass ? (Class<A>) annotationClass : null;
        }
    }

    /**
     * Holds the validated fields used to generate a struct accessor.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2025-08-19
     */
    @SuppressWarnings("unchecked")
    public record StructDefinition(
            Class<?>      type,
            StructField[] fields
    ) {
        public StructDefinition(Class<?> clazz, Field[] fields) {
            this(clazz,
                 Stream.of(fields)
                       .map(f -> new StructField(Struct.Endian.getByteOrder(clazz), f))
                       .toArray(StructField[]::new)
                );
        }

        /**
         * Distinguishes basic fields, nested struct fields, and fields with a custom handler.
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2026-06-21
         */
        public enum Category
        { BASIC, STRUCT, HANDLER }

        /**
         * Stores field access, type resolution and serialization metadata.
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2025-08-19
         */
        @Getter
        @RequiredArgsConstructor
        @Accessors(fluent = true)
        @SuppressWarnings("unchecked")
        @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
        public static class StructField {

            public static boolean legalStructField(Field field)
            {
                return !Modifier.isStatic(field.getModifiers()) && !isIgnore(field);
            }

            public static boolean isIgnore(Field field)
            {
                return hasAnnotation(field, Ignore.class) || hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
            }

            ByteOrder           byteOrder;
            Field               wrapped;
            UnaryOperator<Type> type;
            String              getterName;
            String              setterName;
            Annotation          annotation;

            @Getter(AccessLevel.NONE)
            Supplier<? extends StructFieldHandler<? extends Annotation>> handleSupplier;

            StructDefinition.Category category;

            public StructField(ByteOrder byteOrder, Field field)
            {
                this.byteOrder      = byteOrder;
                this.wrapped        = field;
                this.type           = typeSupplier(field);
                PropDesc property   = validateProperty(field);
                this.getterName     = property.getGetter().getName();
                this.setterName     = property.getSetter().getName();
                this.annotation     = getHandlerAnnotation(field);
                this.handleSupplier = getHandlerSupplier(this.annotation, field);

                StructDefinition.Category initCategory = StructDefinition.Category.HANDLER;
                Type                      rawFieldType = field.getGenericType();
                if (this.annotation == null && rawFieldType instanceof Class<?> clazz) {
                    if (Basic.class.isAssignableFrom(clazz) && Basic.class != clazz) {
                        initCategory = StructDefinition.Category.BASIC;
                    }
                    else if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                        initCategory = StructDefinition.Category.STRUCT;
                    }
                }
                this.category = initCategory;
            }

            static UnaryOperator<Type> typeSupplier(Field field) {
                Type fieldType = field.getGenericType();
                if (fieldType instanceof Class<?>) return root -> fieldType;

                return root -> {
                    if (!(root instanceof Class<?> || root instanceof ParameterizedType)) return fieldType;
                    return resolveType(root, fieldType);
                };
            }

            public Type type(Type root) {
                Type actualType = type.apply(root);
                if (actualType == null || actualType instanceof TypeVariable<?>) {
                    throw new TypeJudgmentException(this);
                }
                return actualType;
            }

            public <A extends Annotation> A annotation() {
                return (A) annotation;
            }

            public <A extends Annotation, H extends StructFieldHandler<A>> H handler() {
                return (H) handleSupplier.get();
            }

            private static PropDesc validateProperty(Field field)
            {
                PropDesc property = BeanUtil.getBeanDesc(field.getDeclaringClass()).getProp(field.getName());
                if (property.getGetter() == null || property.getSetter() == null) {
                    throw new SerializeException("struct field must be a readable and writable bean property: [" + field + "]");
                }

                Method getter = property.getGetter();
                Method setter = property.getSetter();
                boolean valid = ClassUtil.isPublic(getter)
                                && !ClassUtil.isStatic(getter)
                                && getter.getReturnType() == field.getType()
                                && ClassUtil.isPublic(setter)
                                && !ClassUtil.isStatic(setter)
                                && setter.getReturnType() == void.class
                                && setter.getParameterTypes()[0] == field.getType();
                if (!valid) throw new SerializeException("invalid bean getter or setter for struct field: [" + field + "]");
                return property;
            }

            @Override
            public String toString() {
                return wrapped.toString();
            }
        }
    }
}
