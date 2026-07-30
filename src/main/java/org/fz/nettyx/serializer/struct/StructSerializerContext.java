package org.fz.nettyx.serializer.struct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.lang.Filter;
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
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.serializer.struct.generator.StructAccessorFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
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

    static final Map<Type, Integer>                                                BASIC_SIZE_CACHE        = new ConcurrentHashMap<>(64);
    static final Map<Class<? extends Basic<?>>, BiFunction<ByteBuf, ByteOrder, ?>> BASIC_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(64);
    static final Map<Class<?>, StructDefinition>                                   STRUCT_DEFINITION_CACHE = new ConcurrentHashMap<>(256);

    static final Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> ANNOTATION_HANDLER_MAPPING_CACHE = new ConcurrentHashMap<>(32);

    static final InternalLogger log = InternalLoggerFactory.getInstance(StructSerializerContext.class);

    public StructSerializerContext(String... basePackages)
    {
        // will scan all packages if user do not assign
        this.basePackages = defaultIfEmpty(removeNull(basePackages), ALL_PACKAGES);

        synchronized (StructSerializerContext.class) {
            Map<Type, Integer> sizeSnapshot = new HashMap<>(BASIC_SIZE_CACHE);
            Map<Class<? extends Basic<?>>, BiFunction<ByteBuf, ByteOrder, ?>> constructorSnapshot =
                    new HashMap<>(BASIC_CONSTRUCTOR_CACHE);
            Map<Class<?>, StructDefinition> definitionSnapshot = new HashMap<>(STRUCT_DEFINITION_CACHE);
            Map<Class<? extends Annotation>, Class<? extends StructFieldHandler<? extends Annotation>>> handlerSnapshot =
                    new HashMap<>(ANNOTATION_HANDLER_MAPPING_CACHE);
            try {
                scan();
            }
            catch (RuntimeException | Error exception) {
                restoreCache(BASIC_SIZE_CACHE, sizeSnapshot);
                restoreCache(BASIC_CONSTRUCTOR_CACHE, constructorSnapshot);
                restoreCache(STRUCT_DEFINITION_CACHE, definitionSnapshot);
                restoreCache(ANNOTATION_HANDLER_MAPPING_CACHE, handlerSnapshot);
                log.error("exception occur while scanning classes", exception);
                throw exception;
            }
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

        // 3 validate, generate and publish struct definitions
        this.scanStruct(classes);

    }

    private static <K, V> void restoreCache(Map<K, V> cache, Map<K, V> snapshot)
    {
        cache.clear();
        cache.putAll(snapshot);
    }

    /**
     * find class for scan
     *
     * @return the classes can be scanned
     */
    protected Set<Class<?>> classesForScan()
    {
        Set<Class<?>> classesForScan = new HashSet<>(256);

        Filter<Class<?>> scanCondition = StructSerializerContext::isScannableClass;

        String[] basePackages = getBasePackages();

        for (String pack : append(basePackages, ClassUtil.getPackage(this.getClass()))) {
            classesForScan.addAll(ClassScanner.scanAllPackage(pack, scanCondition));
        }

        log.debug("serializer context finished scanning, base-packages: {}", Arrays.toString(basePackages));

        return classesForScan;
    }

    static boolean isScannableClass(Class<?> clazz)
    {
        if (!ClassUtil.isNormalClass(clazz)) return false;
        return !ClassUtil.isJdkClass(clazz);
    }

    protected void scanHandler(Set<Class<?>> classes)
    {
        for (Class<?> clazz : classes) {
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
    }

    protected void scanStruct(Set<Class<?>> classes)
    {
        Map<Class<?>, StructDefinition> definitions = new HashMap<>();
        for (Class<?> clazz : classes) {
            try {
                if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                    if (ClassUtil.isAbstractOrInterface(clazz))   throw new SerializeException("struct class can not be interface or abstract: [" + clazz + "]");
                    if (Modifier.isPrivate(clazz.getModifiers())) throw new SerializeException("struct class can not be private: [" + clazz + "]");

                    Field[] structFields = getFields(clazz, StructHelper::legalStructField);
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

        StructAccessorFactory.generate(definitions.values());
        STRUCT_DEFINITION_CACHE.putAll(definitions);
    }

    private static void validateNoCircularReferences(Map<Class<?>, StructDefinition> definitions)
    {
        Set<Type> visited = new HashSet<>();
        List<Class<?>> typePath = new ArrayList<>();
        List<String> fieldPath = new ArrayList<>();

        definitions.keySet().stream()
                   .sorted(Comparator.comparing(Class::getName))
                   .forEach(type -> detectCircularReference(type, definitions, visited, typePath, fieldPath));
    }

    private static void detectCircularReference(
            Type type,
            Map<Class<?>, StructDefinition> definitions,
            Set<Type> visited,
            List<Class<?>> typePath,
            List<String> fieldPath)
    {
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
                throw new StructDefinitionException("circular struct reference: ["
                                                    + String.join(" -> ", cycle)
                                                    + " -> " + dependency.getSimpleName() + "]");
            }

            fieldPath.add(edge);
            detectCircularReference(dependencyType, definitions, visited, typePath, fieldPath);
            fieldPath.removeLast();
        }
        typePath.removeLast();
    }

    private static Type structDependency(Type root, StructDefinition.StructField field)
    {
        if (field.annotation() != null && !(field.annotation() instanceof ToArray)) return null;

        Type fieldType;
        try {
            fieldType = field.type(root);
        }
        catch (TypeJudgmentException ignored) {
            fieldType = field.wrapped().getGenericType();
        }
        return field.annotation() instanceof ToArray ? arrayComponentType(fieldType) : fieldType;
    }

    private static Type arrayComponentType(Type type)
    {
        return switch (type) {
            case Class<?> clazz when clazz.isArray() -> clazz.getComponentType();
            case GenericArrayType array              -> array.getGenericComponentType();
            default                                  -> null;
        };
    }

    private static Class<?> rawStructClass(Type type)
    {
        if (type == null) return null;

        Class<?> rawType = switch (type) {
            case Class<?> clazz                  -> clazz;
            case ParameterizedType parameterized -> (Class<?>) parameterized.getRawType();
            default                              -> null;
        };
        return rawType != null && AnnotationUtil.hasAnnotation(rawType, Struct.class) ? rawType : null;
    }

    protected void scanBasic(Set<Class<?>> classes)
    {
        for (Class<?> clazz : classes) {
            try {
                boolean isBasic = Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;

                if (isBasic) {
                    // cache basics constructor
                    BASIC_CONSTRUCTOR_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                        lambdaConstructor(clazz, ByteBuf.class, ByteOrder.class));

                    // cache bytes size
                    BASIC_SIZE_CACHE.putIfAbsent((Class<? extends Basic<?>>) clazz,
                                                 StructHelper.reflectForSize((Class<? extends Basic<?>>) clazz));
                }
            }
            catch (RuntimeException | Error throwable) {
                throw new SerializeException("scan basic failed: [" + clazz + "]", throwable);
            }
        }
    }

    static <A extends Annotation, H extends StructFieldHandler<A>> Supplier<H> getHandlerSupplier(
            A handlerAnnotation,
            Field field)
    {
        if (handlerAnnotation != null) {
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
        return switch (type) {
            case Class<?>          clazz             -> STRUCT_DEFINITION_CACHE.get(clazz);
            case ParameterizedType parameterizedType -> getStructDefinition(parameterizedType.getRawType());
            case GenericArrayType  genericArrayType  -> getStructDefinition(genericArrayType.getGenericComponentType());
            case WildcardType      wildcardType      -> getStructDefinition(wildcardType.getUpperBounds()[0]);
            default                                  -> throw new TypeJudgmentException("can not find struct definition by: [" + type + "]");
        };
    }

    static <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz)
    {
        if (!ClassUtil.isNormalClass(clazz)) return null;

        for (ParameterizedType genericType : TypeUtil.getGenerics(clazz)) {
            Class<?> rawClass = (Class<?>) genericType.getRawType();
            if (!StructFieldHandler.class.isAssignableFrom(rawClass)) continue;

            Type annotationType = TypeUtil.getActualType(clazz, genericType.getActualTypeArguments()[0]);
            if (annotationType instanceof Class<?> annotationClass) {
                return (Class<A>) annotationClass;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public record StructDefinition(
            Class<?>      type,
            StructField[] fields
    ) {
        public StructDefinition(Class<?> clazz, Field[] fields) {
            this(clazz,
                 Stream.of(fields)
                       .map(f -> new StructField(StructHelper.getByteOrder(clazz), f))
                       .toArray(StructField[]::new)
                );
        }

        @Getter
        @RequiredArgsConstructor
        @Accessors(fluent = true)
        @SuppressWarnings("unchecked")
        @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
        public static class StructField {

            public enum Category { BASIC, STRUCT, HANDLER }

            ByteOrder           byteOrder;
            Field               wrapped;
            UnaryOperator<Type> type;
            String              getterName;
            String              setterName;
            Annotation          annotation;

            @Getter(AccessLevel.NONE)
            Supplier<? extends StructFieldHandler<? extends Annotation>> handleSupplier;

            Category category;

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

                Category initCategory = Category.HANDLER;
                Type     rawFieldType = field.getGenericType();
                if (this.annotation == null && rawFieldType instanceof Class<?> clazz) {
                    if (Basic.class.isAssignableFrom(clazz) && Basic.class != clazz) {
                        initCategory = Category.BASIC;
                    }
                    else if (AnnotationUtil.hasAnnotation(clazz, Struct.class)) {
                        initCategory = Category.STRUCT;
                    }
                }
                this.category = initCategory;
            }

            static UnaryOperator<Type> typeSupplier(Field field) {
                Type fieldType = field.getGenericType();
                Class<?> declaringClass = field.getDeclaringClass();
                if (fieldType instanceof Class<?>) return root -> fieldType;

                Map<Type, Type> resolvedTypes = new ConcurrentHashMap<>();
                return root -> resolvedTypes.computeIfAbsent(root, actualRoot -> {
                    Type context = resolveContext(actualRoot, declaringClass);
                    return TypeUtil.getActualType(context != null ? context : actualRoot, fieldType);
                });
            }

            private static Type resolveContext(Type root, Class<?> declaringClass) {
                if (root instanceof ParameterizedType pt) {
                    Class<?> rawType = (Class<?>) pt.getRawType();
                    if (rawType == declaringClass) return root;
                    Type superType = rawType.getGenericSuperclass();
                    if (superType != null) {
                        Type resolved = resolveContext(superType, declaringClass);
                        if (resolved != null) return TypeUtil.getActualType(root, resolved);
                    }
                }
                else if (root instanceof Class<?> clazz) {
                    if (clazz == declaringClass) return root;
                    Type superType = clazz.getGenericSuperclass();
                    if (superType != null) {
                        Type resolved = resolveContext(superType, declaringClass);
                        if (resolved != null) return resolved;
                    }
                }
                return null;
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
