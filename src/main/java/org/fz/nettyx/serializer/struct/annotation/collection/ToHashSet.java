package org.fz.nettyx.serializer.struct.annotation.collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructUtils.nullDefault;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.readArray;
import static org.fz.nettyx.serializer.struct.annotation.collection.ToArray.ToArrayHandler.writeArray;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;

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
    Class<?> elementType();

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
    int size() default 0;

    /**
     * The type Byte buf set handler.
     */
    class ToHashSetHandler implements PropertyHandler.ReadWriteHandler<ToHashSet> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToHashSet toHashSet) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = toHashSet.elementType();

            return new HashSet<>(Arrays.asList(readArray(elementType, toHashSet.size(), serializer.getByteBuf())));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToHashSet toHashSet,
            ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, Set.class);

            Class<?> elementType = toHashSet.elementType();
            int size = toHashSet.size();

            writeArray(((HashSet<?>) nullDefault(value, HashSet::new)).toArray(), elementType, size, writingBuffer);
        }

    }

}
