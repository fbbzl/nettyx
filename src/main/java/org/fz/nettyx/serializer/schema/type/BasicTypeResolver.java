package org.fz.nettyx.serializer.schema.type;

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
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.signed.clong8;
import org.fz.nettyx.serializer.basic.c.signed.clonglong;
import org.fz.nettyx.serializer.basic.c.signed.cshort;
import org.fz.nettyx.serializer.basic.c.stdint.signed.cint16_t;
import org.fz.nettyx.serializer.basic.c.stdint.signed.cint32_t;
import org.fz.nettyx.serializer.basic.c.stdint.signed.cint64_t;
import org.fz.nettyx.serializer.basic.c.stdint.signed.cint8_t;
import org.fz.nettyx.serializer.basic.c.stdint.unsigned.cuint16_t;
import org.fz.nettyx.serializer.basic.c.stdint.unsigned.cuint32_t;
import org.fz.nettyx.serializer.basic.c.stdint.unsigned.cuint64_t;
import org.fz.nettyx.serializer.basic.c.stdint.unsigned.cuint8_t;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.unsigned.cuint;
import org.fz.nettyx.serializer.basic.c.unsigned.culong4;
import org.fz.nettyx.serializer.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.basic.c.unsigned.culonglong;
import org.fz.nettyx.serializer.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.basic.cpp.cppbool;
import org.fz.nettyx.serializer.basic.cpp.signed.cppdouble;
import org.fz.nettyx.serializer.basic.cpp.signed.cppfloat;
import org.fz.nettyx.serializer.basic.cpp.signed.cppint;
import org.fz.nettyx.serializer.basic.cpp.signed.cpplong4;
import org.fz.nettyx.serializer.basic.cpp.signed.cpplong8;
import org.fz.nettyx.serializer.basic.cpp.signed.cpplonglong;
import org.fz.nettyx.serializer.basic.cpp.signed.cppshort;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppchar16_t;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppchar32_t;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppchar8_t;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppuchar;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppuint;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppulong4;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppulong8;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppulonglong;
import org.fz.nettyx.serializer.basic.cpp.unsigned.cppushort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * resolves basic wire types (like cint, cushort) by simple class name,
 * reusing the shared {@link Basic} type system
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@UtilityClass
@SuppressWarnings("unchecked")
public class BasicTypeResolver
{

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BasicTypeResolver.class);

    private static final BigInteger MAX_UNSIGNED_BYTE              = BigInteger.valueOf(0xFF);
    private static final BigInteger MAX_UNSIGNED_SHORT             = BigInteger.valueOf(0xFFFF);
    private static final BigInteger MAX_UNSIGNED_INT               = BigInteger.valueOf(0xFFFF_FFFFL);
    private static final BigDecimal MAX_UNSIGNED_BYTE_EXCLUSIVE    = BigDecimal.valueOf(0x100);
    private static final BigDecimal MAX_UNSIGNED_SHORT_EXCLUSIVE   = BigDecimal.valueOf(0x1_0000);
    private static final BigDecimal MAX_UNSIGNED_INT_EXCLUSIVE     = BigDecimal.valueOf(0x1_0000_0000L);

    private static final Map<String, Class<? extends Basic<?>>>           BASIC_TYPE_CACHE        = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, Integer>          BASIC_SIZE_CACHE        = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, BasicReader>      READING_READER_CACHE    = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, BasicValueReader> VALUE_READER_CACHE      = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, BasicValueWriter> VALUE_WRITER_CACHE      = new ConcurrentHashMap<>(64);
    private static final Map<Class<? extends Basic<?>>, Constructor<?>>   VALUE_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(64);

    static {
        Set<Class<?>> scanned = ClassScanner.scanPackage(
                "org.fz.nettyx.serializer.basic",
                clazz -> Basic.class.isAssignableFrom(clazz)
                         && clazz != Basic.class
                         && !Modifier.isAbstract(clazz.getModifiers()));

        registerBasicTypes(BASIC_TYPE_CACHE, scanned);
    }

    static void registerBasicTypes(Map<String, Class<? extends Basic<?>>> types, Set<Class<?>> scanned)
    {
        for (Class<?> clazz : scanned) {
            Class<? extends Basic<?>> basicClass = (Class<? extends Basic<?>>) clazz;
            types.merge(basicClass.getSimpleName(), basicClass, (existing, incoming) -> {
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
        if (basicClass == null)
            throw new TypeJudgmentException("unknown basic type [" + typeName + "] in struct config");
        return basicClass;
    }

    /**
     * read a basic value from buffer
     */
    public static <B extends Basic<?>> B readBasic(Class<? extends Basic<?>> basicClass, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return (B) readerFor(basicClass).read(byteBuf, byteOrder);
    }

    public static BasicReader readerFor(Class<? extends Basic<?>> basicClass)
    {
        return READING_READER_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::newReadingReader);
    }

    public static BasicValueReader valueReaderFor(Class<? extends Basic<?>> basicClass)
    {
        return VALUE_READER_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::newValueReader);
    }

    public static BasicValueWriter valueWriterFor(Class<? extends Basic<?>> basicClass)
    {
        return VALUE_WRITER_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::newValueWriter);
    }

    /**
     * wrap a plain java value into basic, the value will be converted to the value constructor parameter type
     */
    public static <B extends Basic<?>> B valueBasic(Class<B> basicClass, Object value)
    {
        try {
            Constructor<?> valueConstructor = VALUE_CONSTRUCTOR_CACHE.computeIfAbsent(basicClass, BasicTypeResolver::findValueConstructor);
            Class<?>       parameterType    = valueConstructor.getParameterTypes()[0];

            return basicClass.cast(valueConstructor.newInstance(Convert.convert(parameterType, value)));
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
            Constructor<?> constructor       = basicClass.getConstructor(ByteBuf.class, ByteOrder.class);
            MethodHandle   constructorHandle = MethodHandles.lookup().unreflectConstructor(constructor);
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
        ValueWireType wireType = valueWireTypeFor(basicClass);
        Class<? extends Basic<?>> readerType = wireType == null ? basicClass : wireType.basicClass;
        BasicReader reader = readerFor(readerType);
        BasicValueReader valueReader = (byteBuf, byteOrder) -> reader.read(byteBuf, byteOrder).value();

        if (wireType == null || wireType == ValueWireType.UNSIGNED_LONG) return valueReader;
        return checkedReader(sizeOf(readerType), valueReader);
    }

    private static BasicValueWriter newValueWriter(Class<? extends Basic<?>> basicClass)
    {
        ValueWireType wireType = valueWireTypeFor(basicClass);
        if (wireType != null) return wireType.writer;
        return (byteBuf, byteOrder, value) -> valueBasic(basicClass, value).write(byteBuf, byteOrder);
    }

    // Built-in aliases keep the same conversion and wire format as their representative type.
    enum ValueWireType
    {
        SIGNED_BYTE(cchar.class, (buf, order, value) -> new cchar(intValue(value)).write(buf, order), cint8_t.class),
        UNSIGNED_BYTE(cuchar.class, (buf, order, value) -> {
            int number = (int) unsignedIntegerValue(value, MAX_UNSIGNED_BYTE, MAX_UNSIGNED_BYTE_EXCLUSIVE, "unsigned byte");
            new cuchar(number).write(buf, order);
        }, cuint8_t.class, cppchar8_t.class, cppuchar.class),
        SIGNED_SHORT(cshort.class, (buf, order, value) -> new cshort(intValue(value)).write(buf, order),
                     cint16_t.class, cppshort.class),
        UNSIGNED_SHORT(cushort.class, (buf, order, value) -> {
            int number = (int) unsignedIntegerValue(value, MAX_UNSIGNED_SHORT, MAX_UNSIGNED_SHORT_EXCLUSIVE, "unsigned short");
            new cushort(number).write(buf, order);
        }, cuint16_t.class, cppushort.class),
        SIGNED_INT(cint.class, (buf, order, value) -> new cint(intValue(value)).write(buf, order),
                   clong4.class, cint32_t.class, cppint.class, cpplong4.class),
        SIGNED_LONG(clong8.class, (buf, order, value) -> new clong8(longValue(value)).write(buf, order),
                    clonglong.class, cint64_t.class, cpplong8.class, cpplonglong.class),
        UNSIGNED_INT(cuint.class, (buf, order, value) -> {
            long number = unsignedIntegerValue(value, MAX_UNSIGNED_INT, MAX_UNSIGNED_INT_EXCLUSIVE, "unsigned int");
            new cuint(number).write(buf, order);
        }, culong4.class, cuint32_t.class, cppuint.class, cppulong4.class, cppchar32_t.class),
        UNSIGNED_LONG(culong8.class, (buf, order, value) -> new culong8(unsignedBigIntegerValue(value)).write(buf, order),
                      culonglong.class, cuint64_t.class, cppulong8.class, cppulonglong.class),
        FLOAT(cfloat.class, (buf, order, value) -> new cfloat(floatValue(value)).write(buf, order), cppfloat.class),
        DOUBLE(cdouble.class, (buf, order, value) -> new cdouble(doubleValue(value)).write(buf, order), cppdouble.class),
        BOOLEAN(cppbool.class, (buf, order, value) -> new cppbool(Convert.convert(Boolean.class, value)).write(buf, order)),
        CHAR16(cppchar16_t.class, (buf, order, value) ->
                new cppchar16_t(Convert.convert(Character.class, value)).write(buf, order));

        private static final BigInteger MAX_UNSIGNED_LONG              = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);
        private static final BigDecimal MIN_UNSIGNED_DECIMAL_EXCLUSIVE = BigDecimal.valueOf(-1);
        private static final BigDecimal MAX_UNSIGNED_LONG_EXCLUSIVE    = new BigDecimal("18446744073709551616");

        private static final Map<Class<?>, ValueWireType> BY_CLASS = indexBuiltInTypes();

        private final Class<? extends Basic<?>> basicClass;
        private final BasicValueWriter writer;
        private final Class<?>[] aliases;

        ValueWireType(Class<? extends Basic<?>> basicClass, BasicValueWriter writer, Class<?>... aliases)
        {
            this.basicClass = basicClass;
            this.writer = writer;
            this.aliases = aliases;
        }

        private static Map<Class<?>, ValueWireType> indexBuiltInTypes()
        {
            Map<Class<?>, ValueWireType> types = new HashMap<>();
            for (ValueWireType type : values()) {
                types.put(type.basicClass, type);
                for (Class<?> alias : type.aliases) types.put(alias, type);
            }
            return Map.copyOf(types);
        }

        private static long unsignedIntegerValue(Object value, BigInteger maximum, BigDecimal maximumExclusive, String typeName)
        {
            if (value instanceof BigInteger integer) {
                if (integer.signum() < 0 || integer.compareTo(maximum) > 0)
                    throw unsignedValueOutOfRange(value, maximum, typeName);
                return integer.longValue();
            }
            if (value instanceof BigDecimal decimal)
                return unsignedDecimalValue(decimal, maximumExclusive, maximum.longValue(), typeName);
            if (value instanceof CharSequence)
                return unsignedDecimalValue(Convert.convert(BigDecimal.class, value), maximumExclusive, maximum.longValue(), typeName);

            Long number = value instanceof Number numeric ? numeric.longValue() : Convert.convert(Long.class, value);
            if (number == null || number < 0 || number > maximum.longValue())
                throw unsignedValueOutOfRange(value, maximum, typeName);
            return number;
        }

        private static long unsignedDecimalValue(BigDecimal value, BigDecimal maximumExclusive, long maximum, String typeName)
        {
            if (!isUnsignedDecimalInRange(value, maximumExclusive))
                throw unsignedValueOutOfRange(value, BigInteger.valueOf(maximum), typeName);
            return value.longValue();
        }

        private static BigInteger unsignedBigIntegerValue(Object value)
        {
            if (value instanceof BigInteger integer) {
                if (integer.signum() < 0 || integer.compareTo(MAX_UNSIGNED_LONG) > 0)
                    throw unsignedValueOutOfRange(value, MAX_UNSIGNED_LONG, "unsigned long");
                return integer;
            }

            BigDecimal decimal = switch (value) {
                case BigDecimal number -> number;
                case Number number -> new BigDecimal(number.toString());
                case null, default -> Convert.convert(BigDecimal.class, value);
            };

            if (!isUnsignedDecimalInRange(decimal, MAX_UNSIGNED_LONG_EXCLUSIVE))
                throw unsignedValueOutOfRange(value, MAX_UNSIGNED_LONG, "unsigned long");
            return decimal.toBigInteger();
        }

        private static boolean isUnsignedDecimalInRange(BigDecimal value, BigDecimal maximumExclusive)
        {
            return value != null
                   && value.compareTo(MIN_UNSIGNED_DECIMAL_EXCLUSIVE) > 0
                   && value.compareTo(maximumExclusive) < 0;
        }

        private static IllegalArgumentException unsignedValueOutOfRange(Object value, BigInteger maximum, String typeName)
        {
            return new IllegalArgumentException(typeName + " value out of range [0, " + maximum + "]: " + value);
        }

        private static int intValue(Object value)
        {
            return value instanceof Number number ? number.intValue() : Convert.toInt(value);
        }

        private static long longValue(Object value)
        {
            return value instanceof Number number ? number.longValue() : Convert.toLong(value);
        }

        private static float floatValue(Object value)
        {
            return value instanceof Number number ? number.floatValue() : Convert.toFloat(value);
        }

        private static double doubleValue(Object value)
        {
            return value instanceof Number number ? number.doubleValue() : Convert.toDouble(value);
        }
    }

    static ValueWireType valueWireTypeFor(Class<? extends Basic<?>> basicClass)
    {
        // Custom subclasses may change constructor or value semantics while inheriting read and write.
        return basicClass == null ? null : ValueWireType.BY_CLASS.get(basicClass);
    }

    private static BasicValueReader checkedReader(int expectedBytes, BasicValueReader reader)
    {
        return (byteBuf, byteOrder) -> {
            try {
                return reader.read(byteBuf, byteOrder);
            }
            catch (IndexOutOfBoundsException error) {
                throw new TooLessBytesException(expectedBytes, byteBuf.readableBytes());
            }
        };
    }

    /**
     * Reads a binary value into a basic type wrapper using the specified byte order.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-23
     */
    @FunctionalInterface
    public interface BasicReader
    {
        // The runtime class is selected from the schema and its value type is unknown to callers.
        @SuppressWarnings("java:S1452")
        Basic<?> read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    /**
     * Reads the underlying value of a basic type using the specified byte order.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-23
     */
    @FunctionalInterface
    public interface BasicValueReader
    {
        Object read(ByteBuf byteBuf, ByteOrder byteOrder);
    }

    /**
     * Writes the underlying value of a basic type using the specified byte order.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-23
     */
    @FunctionalInterface
    public interface BasicValueWriter
    {
        void write(ByteBuf byteBuf, ByteOrder byteOrder, Object value);
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
