package org.fz.nettyx.beanmodel.primitive;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;

import java.lang.reflect.Type;

public class PrimitiveFieldHandler implements StructFieldHandler<PrimitiveField> {

    @Override
    public Object doRead(
            StructSerializer serializer,
            Type root,
            Object earlyStruct,
            StructField field,
            Type fieldType,
            ByteBuf reading,
            PrimitiveField annotation) {
        if (fieldType == boolean.class) return reading.readBoolean();
        if (fieldType == byte.class) return reading.readByte();
        if (fieldType == char.class) return reading.readChar();
        if (fieldType == short.class) return reading.readShort();
        if (fieldType == int.class) return reading.readInt();
        if (fieldType == long.class) return reading.readLong();
        if (fieldType == float.class) return reading.readFloat();
        if (fieldType == double.class) return reading.readDouble();
        throw new IllegalArgumentException("unsupported primitive type: " + fieldType);
    }

    @Override
    public void doWrite(
            StructSerializer serializer,
            Type root,
            Object struct,
            StructField field,
            Type fieldType,
            Object fieldVal,
            ByteBuf writing,
            PrimitiveField annotation) {
        if (fieldType == boolean.class) writing.writeBoolean((Boolean) fieldVal);
        else if (fieldType == byte.class) writing.writeByte((Byte) fieldVal);
        else if (fieldType == char.class) writing.writeChar((Character) fieldVal);
        else if (fieldType == short.class) writing.writeShort((Short) fieldVal);
        else if (fieldType == int.class) writing.writeInt((Integer) fieldVal);
        else if (fieldType == long.class) writing.writeLong((Long) fieldVal);
        else if (fieldType == float.class) writing.writeFloat((Float) fieldVal);
        else if (fieldType == double.class) writing.writeDouble((Double) fieldVal);
        else throw new IllegalArgumentException("unsupported primitive type: " + fieldType);
    }
}
