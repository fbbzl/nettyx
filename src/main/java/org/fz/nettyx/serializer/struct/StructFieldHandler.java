package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.fz.nettyx.serializer.struct.StructHelper.basicNullDefault;
import static org.fz.nettyx.serializer.struct.StructHelper.structNullDefault;

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

    default Object doRead(
            StructSerializer serializer,
            Type             root,
            Object           earlyStruct,
            StructField      field,
            Type             fieldType,
            ByteBuf          reading,
            A                annotation)
    {
        if (serializer.isBasic(fieldType))  return serializer.readBasic((Class<? extends Basic<?>>) fieldType, reading);
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
            ByteBuf          writing,
            A                annotation)
    {
        if (serializer.isBasic(fieldType)) {
            serializer.writeBasic((Basic<?>) basicNullDefault(fieldVal, (Class<? extends Basic<?>>) fieldType), writing);
            return;
        }
        if (serializer.isStruct(fieldType)) {
            serializer.writeStruct(fieldType, structNullDefault(fieldVal, fieldType), writing);
            return;
        }
        throw new TypeJudgmentException(field);
    }

}
