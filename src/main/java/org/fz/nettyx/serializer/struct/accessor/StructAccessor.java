package org.fz.nettyx.serializer.struct.accessor;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.Basic;

import java.lang.reflect.Type;

import static org.fz.nettyx.serializer.struct.annotation.ToArray.ToArrayHandler.getComponentType;

/**
 * Generated constructor, reader and writer for a struct type.
 * @author fengbinbin
 * @version 1.0
 * @since 2022 -01-16 16:39
 */
public interface StructAccessor
{

    Object newInstance();

    Object read(StructSerializer serializer, Type root, Type structType, ByteBuf buf);

    void write(StructSerializer serializer, Type root, Type structType, Object struct, ByteBuf buf);

    /**
     * Runtime semantics for fields whose concrete types cannot be emitted directly.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings({"rawtypes", "unchecked"})
    final class FieldAccessor
    {

        public static Object readField(
                StructSerializer serializer,
                Type root,
                Object struct,
                Type actualStructType,
                StructField field,
                StructFieldHandler handler,
                ByteBuf buf)
        {
            Type fieldType = field.type(actualStructType);
            if (field.annotation() instanceof ToArray toArray)
                return readArray(serializer, root, field, fieldType, buf, toArray);
            else
                return handler.doRead(serializer, root, struct, field, fieldType, buf, field.annotation());
        }

        public static void writeField(
                StructSerializer serializer,
                Type root,
                Object struct,
                Type actualStructType,
                StructField field,
                StructFieldHandler handler,
                Object value,
                ByteBuf buf)
        {
            Type fieldType = field.type(actualStructType);
            if (field.annotation() instanceof ToArray toArray) {
                writeArray(serializer, root, field, fieldType, value, buf, toArray);
                return;
            }
            handler.doWrite(serializer, root, struct, field, fieldType, value, buf, field.annotation());
        }

        private static Object readArray(
                StructSerializer serializer,
                Type root,
                StructField field,
                Type fieldType,
                ByteBuf buf,
                ToArray toArray)
        {
            int length = toArray.length();
            boolean flexible = toArray.flexible();
            Type componentType = componentType(root, field, fieldType);
            if (serializer.isBasic(componentType))
                return serializer.readBasicArray((Class<?>) componentType, field.byteOrder(), buf, length, flexible);
            else
                return serializer.readStructArray(componentType, buf, length, flexible);
        }

        private static void writeArray(
                StructSerializer serializer,
                Type root,
                StructField field,
                Type fieldType,
                Object value,
                ByteBuf buf,
                ToArray toArray) {
            int length = toArray.length();
            boolean flexible = toArray.flexible();
            Type componentType = componentType(root, field, fieldType);
            if (serializer.isBasic(componentType)) {
                serializer.writeBasicArray((Basic<?>[]) value, StructContext.getBasicSize(componentType),
                                           length, buf, flexible, field.byteOrder());
            }
            else serializer.writeStructArray(value, componentType, length, buf, flexible);
        }

        private static Type componentType(Type root, StructField field, Type fieldType) {
            Class<?> componentClass = concreteComponentClass(field);
            return componentClass != null ? componentClass : getComponentType(root, fieldType);
        }

        private static Class<?> concreteComponentClass(StructField field) {
            return field.wrapped().getGenericType() instanceof Class<?> fieldClass ? fieldClass.getComponentType() : null;
        }
    }
}
