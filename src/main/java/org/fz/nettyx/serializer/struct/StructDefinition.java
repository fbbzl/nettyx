package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.util.lambda.LambdaMetas;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.hutool.core.util.ReflectUtil.getFields;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.*;

/**
 * reference Schema
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/22 10:36
 */

@SuppressWarnings("unchecked")
public record StructDefinition(
        Class<?> type,
        Supplier<?> constructor,
        StructField[] fields
) {

    public StructDefinition(Class<?> clazz) {
        this(clazz, LambdaMetas.lambdaConstructor(clazz), Stream.of(getFields(clazz, StructHelper::legalStructField))
                                                                .map(field -> new StructField(clazz, field))
                                                                .toArray(StructField[]::new));
    }

    @Getter
    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    @Accessors(fluent = true)
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public static class StructField {
        Class<?>                                                     declaringClass;
        Field                                                        wrapped;
        UnaryOperator<Type>                                          type;
        Function<?, ?>                                               getter;
        BiConsumer<?, ?>                                             setter;
        Annotation                                                   annotation;
        Supplier<? extends StructFieldHandler<? extends Annotation>> handler;

        public StructField(Class<?> declaringClass, Field field) {
            this(declaringClass,
                 field,
                 typeSupplier(field),
                 LambdaMetas.lambdaGetter(field),
                 LambdaMetas.lambdaSetter(field),
                 getHandlerAnnotation(field),
                 getHandler(field));
        }

        static UnaryOperator<Type> typeSupplier(Field field) {
            Type fieldType = field.getGenericType();
            return fieldType instanceof Class<?> clazz ? root -> clazz :
                   root -> TypeUtil.getActualType(root, fieldType);
        }

        public boolean isBasic(Type root) {
            return isBasic(root, wrapped.getGenericType());
        }

        public boolean isBasic(Type root, Type type) {
            if (type instanceof Class<?> clazz) return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
            if (type instanceof TypeVariable<?> typeVariable)
                return isBasic(TypeUtil.getActualType(root, typeVariable));

            return false;
        }

        public boolean isStruct(Type root) {
            return isStruct(root, wrapped.getGenericType());
        }

        public boolean isStruct(Type root, Type type) {
            if (type instanceof Class<?> clazz) return STRUCT_DEFINITION_CACHE.containsKey(clazz);
            if (type instanceof ParameterizedType parameterizedType)
                return isStruct(parameterizedType.getRawType());
            if (type instanceof TypeVariable<?> typeVariable)
                return isStruct(TypeUtil.getActualType(root, typeVariable));

            return false;
        }

        public Type actualType(Type root) {
            return type.apply(root);
        }

        public <A extends Annotation> A annotation() {
            return (A) annotation;
        }

        public <A extends Annotation, H extends StructFieldHandler<A>> H handler() {
            return (H) handler.get();
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
