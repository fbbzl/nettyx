package org.fz.nettyx.serializer.struct.annotation.collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fz.nettyx.serializer.struct.SerializerHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.serializer.struct.annotation.PropertyHandler;

/**
 * The interface Set.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:31
 */

@Target(FIELD)
@Retention(RUNTIME)
@PropertyHandler(ToHashSet.HashSetHandler.class)
public @interface ToHashSet {

    /**
     * Element type class.
     *
     * @return the class
     */
    Class<?> elementType();

    /**
     * Type class.
     *
     * @return the class
     */
    Class<? extends java.util.List> type() default ArrayList.class;

    /**
     * Size int.
     *
     * @return the int
     */
    int size() default 0;

    /**
     * Buffer length int.
     *
     * @return the buffer occupied by this char set
     */
    int bufferLength() default 0;

    /**
     * The type Byte buf set handler.
     */
    class HashSetHandler implements SerializerHandler.ReadWriteHandler<StructSerializer> {

        @Override
        public Object doRead(StructSerializer serializer, Field field) {
            StructUtils.checkAssignable(field, Set.class);

            ToHashSet toHashSet = field.getAnnotation(ToHashSet.class);
            Class<?> elementType = toHashSet.elementType();
            int bufferLength = toHashSet.bufferLength();

            return new HashSet<>(Arrays.asList(StructSerializer.readArray(elementType, toHashSet.size(), serializer.readBytes(bufferLength))));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, Set.class);

            ToHashSet toHashSet = field.getAnnotation(ToHashSet.class);
            Class<?> elementType = toHashSet.elementType();
            int size = toHashSet.size();

            StructSerializer.writeArray(((HashSet<?>) value).toArray(), elementType, size, writingBuffer);
        }

    }

}
