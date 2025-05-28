package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.lambda.LambdaMetas;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.hutool.core.util.ReflectUtil.getFields;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getHandler;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getHandlerAnnotation;

/**
 * reference Schema
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/22 10:36
 */

@Data
@RequiredArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StructDefinition {
    Class<?> type;
    Supplier<?> constructor;
    StructField[] fields;

    public StructDefinition(Class<?> clazz)
    {
        this(clazz,
             LambdaMetas.lambdaConstructor(clazz),
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

        Field                                                        wrapped;
        UnaryOperator<Type>                                          type;
        Function<?, ?>                                               getter;
        BiConsumer<?, ?>                                             setter;
        Annotation                                                   annotation;
        Supplier<? extends StructFieldHandler<? extends Annotation>> handler;

        public StructField(Field field)
        {
            this(field,
                 typeSupplier(field),
                 LambdaMetas.lambdaGetter(field),
                 LambdaMetas.lambdaSetter(field),
                 getHandlerAnnotation(field),
                 getHandler(field));
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
