package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.util.TypeRefer;
import org.fz.util.exception.Throws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructHelper.getRawType;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getStructDefinition;

/**
 * the basic serializer of byte-work Provides a protocol based on byte offset partitioning fields
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("unchecked")
public final class StructSerializer implements Serializer {
    /**
     * root of struct
     */
    private final Type root;

    public Type getType() {
        return root;
    }

    StructSerializer(Type root) {
        this.root = root;
    }

    public static <T> T toStruct(Type root, ByteBuf byteBuf) {
        if (root instanceof TypeRefer<?> typeRefer) return toStruct(typeRefer.getTypeValue(), byteBuf);
        else return new StructSerializer(root).doDeserialize(byteBuf);
    }

    public static <T> T toStruct(Type type, byte[] bytes) {
        return toStruct(type, Unpooled.wrappedBuffer(bytes));
    }

    public static <T> T toStruct(Type root, ByteBuffer byteBuffer) {
        return toStruct(root, Unpooled.wrappedBuffer(byteBuffer));
    }

    public static <T> T toStruct(Type root, InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return toStruct(root, baos.toByteArray());
    }

    //*************************************       read write splitter     ********************************************//

    public static <T> ByteBuf toByteBuf(T struct) {
        return toByteBuf(struct.getClass(), struct);
    }

    public static <T> ByteBuf toByteBuf(Type root, T struct) {
        Throws.ifNull(struct, () -> "struct can not be null when write, root type: [" + root + "]");

        if (root instanceof Class<?> || root instanceof ParameterizedType)
            return new StructSerializer(root).doSerialize(struct);
        else if (root instanceof TypeRefer<?> typeRefer) return toByteBuf(typeRefer.getTypeValue(), struct);
        else if (root instanceof TypeReference<?> typeReference) return toByteBuf(typeReference.getType(), struct);
        else
            throw new TypeJudgmentException(root);
    }

    public static <T> byte[] toBytes(T struct) {
        return toBytes(struct.getClass(), struct);
    }

    public static <T> byte[] toBytes(Type root, T struct) {
        ByteBuf writeBuf = toByteBuf(root, struct);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        }
        finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer toNioBuffer(T struct) {
        return toNioBuffer(struct.getClass(), struct);
    }

    public static <T> ByteBuffer toNioBuffer(Type root, T struct) {
        return ByteBuffer.wrap(toBytes(root, struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        outputStream.write(toBytes(struct.getClass(), struct));
    }

    public static <T> void writeStream(Type root, T struct, OutputStream outputStream) throws IOException {
        outputStream.write(toBytes(root, struct));
    }

    //*************************************      working code splitter      ******************************************//

    @Override
    public <T> T doDeserialize(ByteBuf byteBuf) {
        StructDefinition structDef = getStructDefinition(getRawType(root));
        Object           struct    = structDef.constructor().get();

        for (StructField structField : structDef.fields()) {
            Field                 field   = structField.wrapped();
            StructFieldHandler<?> handler = structField.handler();
            try {
                Object fieldVal = handler.doRead(root, struct, structField, byteBuf, structField.annotation());
                structField.setter().accept(struct, fieldVal);
            }
            catch (Exception exception) {
                throw new SerializeException("read exception occur, field is [" + field + "]", exception);
            }
        }

        return (T) struct;
    }

    @Override
    public ByteBuf doSerialize(Object struct) {
        ByteBuf          writing   = buffer();
        StructDefinition structDef = getStructDefinition(getRawType(root));

        for (StructField structField : structDef.fields()) {
            Field                 field      = structField.wrapped();
            StructFieldHandler<?> handler  = structField.handler();
            Object                fieldVal = structField.getter().apply(struct);

            try {
                handler.doWrite(root, struct, structField, fieldVal, writing, structField.annotation());
            }
            catch (Exception exception) {
                throw new SerializeException("write exception occur, field [" + field + "]", exception);
            }
        }
        return writing;
    }

    //******************************************      public end       ***********************************************//
}
