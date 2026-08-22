package org.fz.nettyx.serializer.configured;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.ClassScanner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * resolves basic wire types (like cint, cushort) by simple class name,
 * reusing the {@link Basic} type system of struct package
 *
 * @author fengbinbin
 * @since 2026-08-16
 */
@UtilityClass
@SuppressWarnings("unchecked")
public class BasicTypeResolver {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BasicTypeResolver.class);

    private static final Map<String, Class<? extends Basic<?>>> BASIC_TYPE_CACHE = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, Integer> BASIC_SIZE_CACHE = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, BasicReader> READING_READER_CACHE = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, BasicValueReader> VALUE_READER_CACHE = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, Constructor<?>> VALUE_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(64);

    static {
        Set<Class<?>> scanned = ClassScanner.scanPackage(
                "org.fz.nettyx.serializer.struct.basic",
                clazz -> Basic.class.isAssignableFrom(clazz)
                         && clazz != Basic.class
                         && !Modifier.isAbstract(clazz.getModifiers()));

        for (Class<?> clazz : scanned) {
            Class<? extends Basic<?>> basicClass = (Class<? extends Basic<?>>) clazz;
            BASIC_TYPE_CACHE.merge(basicClass.getSimpleName(), basicClass, (existing, incoming) -> {
                log.warn("duplicated basic type simple name [{}], keep [{}], ignore [{}]",
                         basicClass.getSimpleName(), existing.getName(), incoming.getName());
                return existing;
            });
        }
    }

    /**
     * resolve basic type by simple name like cint, cushort
     */
    public static Class<? extends Basic<?>> resolve(String typeName)
    {
        Class<? extends Basic<?>> basicClass = BASIC_TYPE_CACHE.get(typeName);
        if (basicClass == null) throw new TypeJudgmentException("unknown basic type [" + typeName + "] in struct config");
        return basicClass;
    }

    /**
     * read a basic value from buffer
     */
    public static <B extends Basic<?>> B readBasic(Class<? extends Basic<?>> basicClass, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return (B) readerFor(basicClass).read(byteBuf, byteOrder);
    }

    static BasicReader readerFor(Class<? extends Basic<?>> basicClass)
    {
        return READING_READER_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::newReadingReader);
    }

    static BasicValueReader valueReaderFor(Class<? extends Basic<?>> basicClass)
    {
        return VALUE_READER_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::newValueReader);
    }

    /**
     * wrap a plain java value into basic, the value will be converted to the value constructor parameter type
     */
    public static Basic<?> valueBasic(Class<? extends Basic<?>> basicClass, Object value)
    {
        try {
            Constructor<?> valueConstructor = VALUE_CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::findValueConstructor);
            Class<?>       parameterType    = valueConstructor.getParameterTypes()[0];

            return (Basic<?>) valueConstructor.newInstance(Convert.convert(parameterType, value));
        }
        catch (SerializeException serializeError) {
            throw serializeError;
        }
        catch (Exception instanceError) {
            throw new SerializeException("basic [" + basicClass.getName() + "] instantiate failed with value [" + value + "]", instanceError);
        }
    }

    private static Constructor<?> findValueConstructor(Class<? extends Basic<?>> basicClass)
    {
        for (Constructor<?> constructor : basicClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] != ByteBuf.class)
                return constructor;
        }
        throw new SerializeException("no value constructor found on basic [" + basicClass.getName() + "]");
    }

    private static BasicReader newReadingReader(Class<? extends Basic<?>> basicClass)
    {
        try {
            Constructor<?> constructor = basicClass.getConstructor(ByteBuf.class, ByteOrder.class);
            MethodHandle constructorHandle = MethodHandles.lookup().unreflectConstructor(constructor);
            CallSite callSite = LambdaMetafactory.metafactory(
                    MethodHandles.lookup(),
                    "read",
                    MethodType.methodType(BasicReader.class),
                    MethodType.methodType(Basic.class, ByteBuf.class, ByteOrder.class),
                    constructorHandle,
                    MethodType.methodType(Basic.class, ByteBuf.class, ByteOrder.class));
            return (BasicReader) callSite.getTarget().invokeExact();
        }
        catch (Throwable error) {
            throw new SerializeException("basic [" + basicClass.getName() + "] reader initialization failed", error);
        }
    }

    private static BasicValueReader newValueReader(Class<? extends Basic<?>> basicClass)
    {
        if (basicClass == cchar.class)  return BasicTypeResolver::readByte;
        if (basicClass == cuchar.class) return BasicTypeResolver::readUnsignedByte;
        if (basicClass == cshort.class) return BasicTypeResolver::readShort;
        if (basicClass == cushort.class) return BasicTypeResolver::readUnsignedShort;
        if (basicClass == cint.class || basicClass == clong4.class) return BasicTypeResolver::readInt;
        if (basicClass == clong8.class) return BasicTypeResolver::readLong;
        if (basicClass == cfloat.class) return BasicTypeResolver::readFloat;
        if (basicClass == cdouble.class) return BasicTypeResolver::readDouble;

        BasicReader reader = readerFor(basicClass);
        return (byteBuf, byteOrder) -> reader.read(byteBuf, byteOrder).value();
    }

    private static Byte readByte(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteBuf.readByte();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Byte.BYTES);
        }
    }

    private static Short readUnsignedByte(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteBuf.readUnsignedByte();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Byte.BYTES);
        }
    }

    private static Short readShort(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readShortLE() : byteBuf.readShort();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Short.BYTES);
        }
    }

    private static Integer readUnsignedShort(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readUnsignedShortLE() : byteBuf.readUnsignedShort();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Short.BYTES);
        }
    }

    private static Integer readInt(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readIntLE() : byteBuf.readInt();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Integer.BYTES);
        }
    }

    private static Long readLong(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readLongLE() : byteBuf.readLong();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Long.BYTES);
        }
    }

    private static Float readFloat(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readFloatLE() : byteBuf.readFloat();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Float.BYTES);
        }
    }

    private static Double readDouble(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        try {
            return byteOrder == ByteOrder.LITTLE_ENDIAN ? byteBuf.readDoubleLE() : byteBuf.readDouble();
        }
        catch (IndexOutOfBoundsException error) {
            throw tooLessBytes(byteBuf, Double.BYTES);
        }
    }

    private static TooLessBytesException tooLessBytes(ByteBuf byteBuf, int expectedBytes)
    {
        return new TooLessBytesException(expectedBytes, byteBuf.readableBytes());
    }

    @FunctionalInterface
    interface BasicReader {
        Basic<?> read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    @FunctionalInterface
    interface BasicValueReader {
        Object read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    /**
     * byte size of a basic type
     */
    public static int sizeOf(Class<? extends Basic<?>> basicClass)
    {
        return BASIC_SIZE_CACHE.computeIfAbsent(basicClass, clazz -> {
            ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
            try {
                return readBasic(clazz, ByteOrder.nativeOrder(), fillingBuf).size();
            }
            finally {
                fillingBuf.skipBytes(fillingBuf.readableBytes()).release();
            }
        });
    }
}
