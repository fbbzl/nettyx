package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * @deprecated use {@link org.fz.nettyx.serializer.configured.type.BasicTypeResolver}
 */
@Deprecated
public final class BasicTypeResolver {

    private BasicTypeResolver()
    {
    }

    public static Class<? extends Basic<?>> resolve(String typeName)
    {
        return org.fz.nettyx.serializer.configured.type.BasicTypeResolver.resolve(typeName);
    }

    public static <B extends Basic<?>> B readBasic(Class<? extends Basic<?>> basicClass, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return org.fz.nettyx.serializer.configured.type.BasicTypeResolver.readBasic(basicClass, byteOrder, byteBuf);
    }

    public static BasicReader readerFor(Class<? extends Basic<?>> basicClass)
    {
        org.fz.nettyx.serializer.configured.type.BasicTypeResolver.BasicReader reader =
                org.fz.nettyx.serializer.configured.type.BasicTypeResolver.readerFor(basicClass);
        return reader::read;
    }

    public static BasicValueReader valueReaderFor(Class<? extends Basic<?>> basicClass)
    {
        org.fz.nettyx.serializer.configured.type.BasicTypeResolver.BasicValueReader reader =
                org.fz.nettyx.serializer.configured.type.BasicTypeResolver.valueReaderFor(basicClass);
        return reader::read;
    }

    public static BasicValueWriter valueWriterFor(Class<? extends Basic<?>> basicClass)
    {
        org.fz.nettyx.serializer.configured.type.BasicTypeResolver.BasicValueWriter writer =
                org.fz.nettyx.serializer.configured.type.BasicTypeResolver.valueWriterFor(basicClass);
        return writer::write;
    }

    public static Basic<?> valueBasic(Class<? extends Basic<?>> basicClass, Object value)
    {
        return org.fz.nettyx.serializer.configured.type.BasicTypeResolver.valueBasic(basicClass, value);
    }

    public static int sizeOf(Class<? extends Basic<?>> basicClass)
    {
        return org.fz.nettyx.serializer.configured.type.BasicTypeResolver.sizeOf(basicClass);
    }

    @FunctionalInterface
    public interface BasicReader {
        Basic<?> read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    @FunctionalInterface
    public interface BasicValueReader {
        Object read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    @FunctionalInterface
    public interface BasicValueWriter {
        void write(ByteBuf byteBuf, ByteOrder byteOrder, Object value);
    }
}
