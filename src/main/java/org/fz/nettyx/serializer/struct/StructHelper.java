package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.reflect.*;
import java.nio.ByteOrder;

import static cn.hutool.core.annotation.AnnotationUtil.hasAnnotation;
import static cn.hutool.core.util.ModifierUtil.hasModifier;
import static org.fz.nettyx.serializer.struct.StructContext.*;
import static org.fz.nettyx.serializer.struct.generator.StructAccessorFactory.get;


/**
 * The type Struct utils.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/26 9:28
 */
@SuppressWarnings("unchecked")
@UtilityClass
public class StructHelper {

    /**
     * New basic instance t.
     *
     * @param <B>        the type parameter
     * @param basicClass the basic class
     * @param byteOrder  the byte order
     * @param buf        the buf
     * @return the t
     */
    public static <B extends Basic<?>> B newBasic(
            Class<?>  basicClass,
            ByteOrder byteOrder,
            ByteBuf   buf)
    {
        try
        {
            return (B) BASIC_CONSTRUCTOR_CACHE.get(basicClass).apply(buf, byteOrder);
        }
        catch (Exception instanceError)
        {
            Throwable cause = instanceError.getCause();
            if (instanceError instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            if (cause instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            else
                throw new SerializeException("basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]", instanceError);
        }
    }

    /**
     * New struct instance t.
     *
     * @param <S>        the type parameter
     * @param structType the struct type
     * @return the t
     */
    public static <S> S newStruct(Type structType)
    {
        try
        {
            StructContext.StructDefinition definition = getStructDefinition(structType);
            if (definition == null)
                throw new SerializeException("uncached struct type: " + structType);
            return
                (S) get(definition).newInstance();
        }
        catch (Exception instanceError)
        {
            throw new SerializeException("struct [" + structType + "] instantiate failed...", instanceError);
        }
    }

}
