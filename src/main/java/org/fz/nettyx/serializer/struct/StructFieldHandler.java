package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.fz.nettyx.serializer.struct.StructSerializer.basicNullDefault;
import static org.fz.nettyx.serializer.struct.StructSerializer.structNullDefault;

/**
 * The top-level parent class of all custom serialization processors default is not singleton
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
@SuppressWarnings("all")
public interface StructFieldHandler<A extends Annotation> {

    StructFieldHandler<? extends Annotation> DEFAULT_READ_WRITE_HANDLER = new StructFieldHandler() {
        @Override
        public boolean isSingleton() {
            return true;
        }
    };

    /**
     * config the handler instance if is singleton
     *
     * @return if is singleton handler
     */
    default boolean isSingleton() {
        return false;
    }

    default Object doRead(StructSerializer serializer, Type fieldType, StructField structField, A annotation, Object earlyObject) {
        Field field = structField.wrapped();

        if (serializer.isBasic(fieldType)) return serializer.readBasic(fieldType);
        if (serializer.isStruct(fieldType)) return serializer.readStruct(fieldType);

        throw new TypeJudgmentException(field);
    }

    default void doWrite(StructSerializer serializer, Type fieldType, StructField structField, A annotation, Object value,
                         ByteBuf writing) {
        Field field = structField.wrapped();

        if (serializer.isBasic(field)) {
            serializer.writeBasic((Basic<?>) basicNullDefault(value, fieldType), writing);
            return;
        }
        if (serializer.isStruct(field)) {
            serializer.writeStruct(fieldType, structNullDefault(value, fieldType), writing);
            return;
        }

        throw new TypeJudgmentException(field);
    }

    static <A extends Annotation> Class<A> getTargetAnnotationType(Class<?> clazz) {
        if (!ClassUtil.isNormalClass(clazz)) {
            return null;
        }

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
}
