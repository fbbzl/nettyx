package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.ByteOrder;

/**
 * The top-level parent class of all custom serialization processors default is not singleton
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
@SuppressWarnings("all")
public interface StructFieldHandler<A extends Annotation> {

    StructFieldHandler<? extends Annotation> DEFAULT_STRUCT_FIELD_HANDLER = new StructFieldHandler() {
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

    default void doAnnotationValid(A annotation, Field field) {}

    default Object doRead(
            StructSerializer serializer,
            Type             root,
            Object           earlyStruct,
            StructField      field,
            Type             fieldType,
            ByteOrder        byteOrder,
            ByteBuf          reading,
            A                annotation)
    {
        if (serializer.isBasic(fieldType))  return serializer.readBasic((Class<? extends Basic<?>>) fieldType, byteOrder, reading);
        if (serializer.isStruct(fieldType)) return serializer.readStruct(fieldType, reading);

        throw new TypeJudgmentException(field);
    }

    default void doWrite(
            StructSerializer serializer,
            Type             root,
            Object           struct,
            StructField      field,
            Type             fieldType,
            Object           fieldVal,
            ByteOrder        byteOrder,
            ByteBuf          writing,
            A                annotation)
    {
        if (serializer.isBasic(fieldType)) {
            serializer.writeBasic((Basic<?>) StructHelper.defaultBasic(fieldVal, byteOrder, (Class<? extends Basic<?>>) fieldType), writing);
            return;
        }
        if (serializer.isStruct(fieldType)) {
            serializer.writeStruct(fieldType, StructHelper.defaultStruct(fieldVal, fieldType), writing);
            return;
        }
        throw new TypeJudgmentException(field);
    }

}
