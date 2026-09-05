package org.fz.nettyx.serializer.type.generator;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.type.StructFieldHandler;
import org.fz.nettyx.serializer.type.StructHelper;
import org.fz.nettyx.serializer.type.StructSerializer;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.type.annotation.ToArray;
import org.fz.nettyx.serializer.type.basic.Basic;

import java.lang.reflect.Type;

import static org.fz.nettyx.serializer.type.annotation.ToArray.ToArrayHandler.getComponentType;

/**
 * Generated constructor, reader and writer for a struct type.
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
public interface StructAccessor {
    Object newInstance();

    Object read(StructSerializer serializer, Type root, Type structType, ByteBuf buf);

    void write(StructSerializer serializer, Type root, Type structType, Object struct, ByteBuf buf);

    /** Runtime semantics for fields whose concrete types cannot be emitted directly. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    final class Support {

        private Support() {
        }

        public static Type resolveStructType(Type root, Type structType) {
            return structType instanceof Class<?> ? structType : TypeUtil.getActualType(root, structType);
        }

        public static Object readField(
                StructSerializer serializer,
                Type root,
                Object struct,
                Type actualStructType,
                StructField field,
                StructFieldHandler handler,
                ByteBuf buf) {
            Type fieldType = field.type(actualStructType);
            if (field.annotation() instanceof ToArray toArray) {
                return readArray(serializer, root, field, fieldType, buf, toArray);
            }
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
                ByteBuf buf) {
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
                ToArray toArray) {
            int length = toArray.length();
            boolean flexible = toArray.flexible();
            Type componentType = componentType(root, field, fieldType);
            if (serializer.isBasic(componentType)) {
                return serializer.readBasicArray((Class<?>) componentType, field.byteOrder(), buf, length, flexible);
            }
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
                serializer.writeBasicArray((Basic<?>[]) value, StructHelper.findBasicSize(componentType),
                                           length, buf, flexible, field.byteOrder());
            }
            else {
                serializer.writeStructArray(value, componentType, length, buf, flexible);
            }
        }

        private static Type componentType(Type root, StructField field, Type fieldType) {
            Class<?> componentClass = concreteComponentClass(field);
            return componentClass != null ? componentClass : getComponentType(root, fieldType);
        }

        private static Class<?> concreteComponentClass(StructField field) {
            Type fieldType = field.wrapped().getGenericType();
            return fieldType instanceof Class<?> fieldClass ? fieldClass.getComponentType() : null;
        }
    }
}
