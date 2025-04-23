package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

        Field            wrapped;
        Function<?, ?>   getter;
        BiConsumer<?, ?> setter;
        /**
         * struct field handler annotation
         */
        Annotation       annotation;
        /**
         * struct field handler supplier
         */
        @Getter(AccessLevel.NONE)
        Supplier<? extends StructFieldHandler<? extends Annotation>> handler;

        public StructField(Field wrapped,
                           Annotation annotation,
                           Supplier<? extends StructFieldHandler<? extends Annotation>> handler) {
            this.wrapped    = wrapped;
            this.getter     = lambdaGetter(wrapped);
            this.setter     = lambdaSetter(wrapped);
            this.annotation = annotation;
            this.handler    = handler;
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

        public static final Pair<Annotation, Supplier<? extends StructFieldHandler<? extends Annotation>>> NO_HANDLER = Pair.of(null, null);

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }

}
