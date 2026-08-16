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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
        try {
            return (B) basicClass.getConstructor(ByteBuf.class, ByteOrder.class).newInstance(byteBuf, byteOrder);
        }
        catch (Exception instanceError) {
            Throwable cause = instanceError.getCause();
            if (instanceError instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            if (cause instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            throw new SerializeException("basic [" + basicClass.getName() + "] instantiate failed while reading", instanceError);
        }
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
