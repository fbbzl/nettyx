package org.fz.nettyx.serializer.struct.annotation.collection;

import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.annotation.array.ToBasicArray.ToBasicArrayHandler.readArray;
import static org.fz.nettyx.serializer.struct.annotation.array.ToBasicArray.ToBasicArrayHandler.writeArray;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.util.Throws;

/**
 * The interface Set.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:31
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToHashSet {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType() default Object.class;

    /**
     * Type class.
     *
     * @return the class
     */
    Class<? extends java.util.List> type() default ArrayList.class;

    /**
     * set size int.
     *
     * @return the int
     */
    int size();

    /**
     * The type Byte buf set handler.
     */
    class ToHashSetHandler implements PropertyHandler.ReadWriteHandler<ToHashSet> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToHashSet toHashSet) {
            Class<?> elementType =
                (elementType = toHashSet.elementType()) == Object.class ? serializer.getFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            return new HashSet<>(Arrays.asList(readArray(elementType, toHashSet.size(), serializer.getByteBuf())));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToHashSet toHashSet,
            ByteBuf writing) {
            Class<?> elementType =
                (elementType = toHashSet.elementType()) == Object.class ? serializer.getFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            Set<?> set = (HashSet<?>) defaultIfNull(value, () -> newHashSet());
            writeArray(set.toArray(), elementType, toHashSet.size(), writing);
        }

    }

}
