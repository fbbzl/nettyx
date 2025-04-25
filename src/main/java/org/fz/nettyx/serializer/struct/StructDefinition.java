package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.fz.nettyx.serializer.struct.StructHelper.lambdaGetter;
import static org.fz.nettyx.serializer.struct.StructHelper.lambdaSetter;

/**
 * reference Schema
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/22 10:36
 */

public record StructDefinition(
        Supplier<?> constructor,
        StructField[] fields) {

    static final Map<Class<?>, StructDefinition> STRUCT_DEFINITION_CACHE = new ConcurrentHashMap<>(512);

    @Getter
    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class StructField {

        Type                declaringClass;
        Field               wrapped;
        /**
         * struct field type
         */
        @Getter(AccessLevel.NONE)
        UnaryOperator<Type> type;

        Function<?, ?>      getter;
        BiConsumer<?, ?>    setter;
        /**
         * struct field handler annotation
         */
        Annotation          annotation;
        /**
         * struct field handler supplier
         */
        @Getter(AccessLevel.NONE)
        Supplier<? extends StructFieldHandler<? extends Annotation>> handler;

        public StructField(Type declaringClass,
                           Field wrapped,
                           Annotation annotation,
                           Supplier<? extends StructFieldHandler<? extends Annotation>> handler) {
            this.declaringClass = declaringClass;
            this.wrapped        = wrapped;
            this.type           = typeSupplier(wrapped);
            this.getter         = lambdaGetter(wrapped);
            this.setter         = lambdaSetter(wrapped);
            this.annotation     = annotation;
            this.handler        = handler;
        }

        private UnaryOperator<Type> typeSupplier(Field field) {
            Type fieldType = field.getGenericType();
            return fieldType instanceof Class<?> ? root -> (Class<?>) fieldType :
                   root -> TypeUtil.getActualType(root, fieldType);
        }

        public Type getActualType(Type root) {
            return type.apply(root);
        }

        public <A extends Annotation> A getAnnotation() {
            return (A) annotation;
        }

        public <H extends StructFieldHandler<?>> H getStructFieldHandler() {
            return (H) handler.get();
        }

        public <O, R> Function<O, R> getGetter() {
            return (Function<O, R>) getter;
        }

        public <O, P> BiConsumer<O, P> getSetter() {
            return (BiConsumer<O, P>) setter;
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }

}
